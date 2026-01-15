package com.yizhcr.my_blog.service;

import com.yizhcr.my_blog.entity.Article;
import com.yizhcr.my_blog.entity.Category;
import com.yizhcr.my_blog.entity.Tag;
import com.yizhcr.my_blog.dto.ArticleDTO;
import com.yizhcr.my_blog.repository.ArticleRepository;
import com.yizhcr.my_blog.repository.CategoryRepository;
import com.yizhcr.my_blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private RedisService redisService;

    private static final String ARTICLE_CACHE_PREFIX = "article:";
    private static final String ARTICLE_LIST_CACHE_KEY = "article_list";
    private static final Long CACHE_TTL_SECONDS = 3600L; // 1小时缓存

    public List<ArticleDTO> getAllArticles() {
        // 尝试从缓存获取文章列表
        String cacheKey = ARTICLE_LIST_CACHE_KEY;
        List<ArticleDTO> cachedArticles = redisService.getList(cacheKey, ArticleDTO.class);
        
        if (cachedArticles != null && !cachedArticles.isEmpty()) {
            return cachedArticles;
        }

        // 缓存未命中，从数据库获取
        List<Article> articles = articleRepository.findAll();
        List<ArticleDTO> articleDTOs = articles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 存入缓存
        redisService.set(cacheKey, articleDTOs, CACHE_TTL_SECONDS);

        return articleDTOs;
    }

    public ArticleDTO getArticleById(Long id) {
        // 尝试从缓存获取文章
        String cacheKey = ARTICLE_CACHE_PREFIX + id;
        ArticleDTO cachedArticle = redisService.get(cacheKey, ArticleDTO.class);
        
        if (cachedArticle != null) {
            return cachedArticle;
        }

        // 缓存未命中，从数据库获取
        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isPresent()) {
            Article article = articleOpt.get();
            ArticleDTO dto = convertToDTO(article);
            
            // 存入缓存
            redisService.set(cacheKey, dto, CACHE_TTL_SECONDS);
            
            return dto;
        }

        return null;
    }

    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        Article article = new Article();
        org.springframework.beans.BeanUtils.copyProperties(articleDTO, article, "id", "author", "category", "tags");

        // 设置分类
        if (articleDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(articleDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            article.setCategory(category);
        }

        // 设置标签并增加标签的文章计数
        if (articleDTO.getTagNames() != null && !articleDTO.getTagNames().isEmpty()) {
            List<Tag> tags = tagRepository.findByNameIn(articleDTO.getTagNames());
            Set<Tag> tagSet = tags.stream().collect(Collectors.toSet()); // 转换为Set
            article.setTags(tagSet);
            
            // 更新标签的文章计数
            for (Tag tag : tags) {
                tag.setArticleCount(tag.getArticleCount() + 1);
            }
            tagRepository.saveAll(tags);
        }

        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        article.setViewCount(0); // 新文章浏览量从0开始

        Article savedArticle = articleRepository.save(article);

        // 清除相关的缓存
        clearArticleCache();

        return convertToDTO(savedArticle);
    }

    public ArticleDTO updateArticle(Long id, ArticleDTO articleDTO) {
        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isPresent()) {
            Article article = articleOpt.get();
            org.springframework.beans.BeanUtils.copyProperties(articleDTO, article, "id", "createdAt", "viewCount", "author", "category", "tags");

            // 更新分类
            if (articleDTO.getCategoryId() != null) {
                Category category = categoryRepository.findById(articleDTO.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                article.setCategory(category);
            }

            // 处理标签更新
            updateArticleTags(article, articleDTO);

            article.setUpdatedAt(LocalDateTime.now());

            Article updatedArticle = articleRepository.save(article);

            // 清除相关的缓存
            clearArticleCache();
            clearSingleArticleCache(id);

            return convertToDTO(updatedArticle);
        }

        return null;
    }

    public void deleteArticle(Long id) {
        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isPresent()) {
            Article article = articleOpt.get();
            
            // 更新标签的文章计数
            if (article.getTags() != null) {
                for (Tag tag : article.getTags()) {
                    tag.setArticleCount(Math.max(0, tag.getArticleCount() - 1));
                }
                tagRepository.saveAll(article.getTags());
            }
            
            articleRepository.deleteById(id);
            
            // 清除相关的缓存
            clearArticleCache();
            clearSingleArticleCache(id);
        }
    }

    // 增加文章浏览量
    public void incrementViewCount(Long id) {
        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isPresent()) {
            Article article = articleOpt.get();
            article.setViewCount(article.getViewCount() + 1);
            articleRepository.save(article);
            
            // 清除单个文章缓存，以便下次访问时刷新
            clearSingleArticleCache(id);
        }
    }

    private ArticleDTO convertToDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        org.springframework.beans.BeanUtils.copyProperties(article, dto);

        if (article.getAuthor() != null) {
            dto.setAuthor(article.getAuthor());
        }

        if (article.getCategory() != null) {
            dto.setCategory(article.getCategory());
        }

        if (article.getTags() != null) {
            dto.setTags(article.getTags());
        }

        return dto;
    }

    private void updateArticleTags(Article article, ArticleDTO articleDTO) {
        // 获取旧标签列表
        List<Tag> oldTags = article.getTags() != null ? 
            article.getTags().stream().collect(Collectors.toList()) : List.of();
        
        // 获取新标签列表
        List<Tag> newTags = articleDTO.getTagNames() != null && !articleDTO.getTagNames().isEmpty()
                ? tagRepository.findByNameIn(articleDTO.getTagNames())
                : List.of();

        // 更新旧标签计数
        for (Tag tag : oldTags) {
            if (!newTags.contains(tag)) {
                tag.setArticleCount(Math.max(0, tag.getArticleCount() - 1));
            }
        }

        // 更新新标签计数
        for (Tag tag : newTags) {
            if (!oldTags.contains(tag)) {
                tag.setArticleCount(tag.getArticleCount() + 1);
            }
        }

        // 保存标签变更
        tagRepository.saveAll(oldTags);
        tagRepository.saveAll(newTags);

        // 更新文章的标签
        Set<Tag> newTagSet = newTags.stream().collect(Collectors.toSet());
        article.setTags(newTagSet);
    }

    // 清除文章列表缓存
    private void clearArticleCache() {
        redisService.del(ARTICLE_LIST_CACHE_KEY);
    }

    // 清除单个文章缓存
    private void clearSingleArticleCache(Long id) {
        redisService.del(ARTICLE_CACHE_PREFIX + id);
    }
}