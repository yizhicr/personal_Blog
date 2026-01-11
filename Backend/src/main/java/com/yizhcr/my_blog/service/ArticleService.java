package com.yizhcr.my_blog.service;

import com.yizhcr.my_blog.dto.SearchRequest;
import com.yizhcr.my_blog.entity.Article;
import com.yizhcr.my_blog.entity.Category;
import com.yizhcr.my_blog.entity.Tag;
import com.yizhcr.my_blog.entity.User;
import com.yizhcr.my_blog.entity.PageImpl;
import com.yizhcr.my_blog.repository.ArticleRepository;
import com.yizhcr.my_blog.repository.CategoryRepository;
import com.yizhcr.my_blog.repository.TagRepository;
import com.yizhcr.my_blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ArticleService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // 缓存key前缀
    private static final String ARTICLE_CACHE_KEY = "article:";
    private static final String ARTICLE_VIEW_COUNT_KEY = "article:view:";
    private static final String ARTICLES_HOT_KEY = "articles:hot";
    private static final String ARTICLES_LATEST_KEY = "articles:latest";
    private static final String ARTICLES_BY_CATEGORY_KEY = "articles:category:";
    private static final String ARTICLES_BY_TAG_KEY = "articles:tag:";
    private static final String ARTICLES_PUBLISHED_KEY = "articles:published:all";
    private static final String ARTICLES_SEARCH_KEY = "articles:search:";
    private static final String ARTICLES_USER_KEY = "articles:user:";
    
    // 缓存过期时间（秒）
    private static final long ARTICLE_CACHE_EXPIRE = 30 * 60;  // 30分钟
    private static final long HOT_ARTICLES_EXPIRE = 10 * 60;   // 10分钟
    private static final long CATEGORY_TAG_ARTICLES_EXPIRE = 15 * 60; // 15分钟
    private static final long SEARCH_CACHE_EXPIRE = 5 * 60;    // 5分钟
    
    /**
     * 创建文章（支持分类和标签）
     */
    @Transactional
    public Article createArticle(Article article, Long authorId, Long categoryId, List<String> tagNames) {
        // 设置作者
        User author = userService.findById(authorId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        article.setAuthor(author);
        
        // 设置分类
        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId)
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            article.setCategory(category);
            categoryService.incrementArticleCount(categoryId);
        }
        
        // 设置标签
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = tagService.createOrGetTags(tagNames);
            article.setTags(tags);
            tagService.incrementArticleCount(tags);
        }
        
        // 如果没提供摘要，从内容生成摘要
        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            String content = article.getContent();
            String summary = content.length() > 200 ? content.substring(0, 200) + "..." : content;
            article.setSummary(summary);
        }
        
        Article savedArticle = articleRepository.save(article);
        
        // 清除相关缓存
        clearArticleCaches();
        if (categoryId != null) {
            redisService.del(ARTICLES_BY_CATEGORY_KEY + categoryId + ":*");
        }
        
        return savedArticle;
    }
    
    /**
     * 获取文章详情（带缓存）
     */
    @Transactional
    public Optional<Article> getArticleById(Long id) {
        String cacheKey = ARTICLE_CACHE_KEY + id;
        
        // 1. 先尝试从缓存获取
        Article article = (Article) redisService.get(cacheKey);
        if (article != null) {
            // 缓存命中，增加阅读计数
            incrementViewCount(id);
            return Optional.of(article);
        }
        
        // 2. 缓存未命中，查询数据库
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            article = articleOptional.get();
            
            // 3. 存入缓存
            redisService.set(cacheKey, article, ARTICLE_CACHE_EXPIRE);
            
            // 4. 增加阅读计数
            incrementViewCount(id);
            
            // 5. 更新热门文章缓存（如果这篇文章变得热门）
            updateHotArticlesCacheIfNeeded(article);
        }
        
        return articleOptional;
    }
    
    /**
     * 增加文章阅读量
     */
    public void increaseViewCount(Long articleId) {
        incrementViewCount(articleId);
    }

    /**
     * 增加文章阅读量（使用Redis计数，避免频繁写数据库）
     */
    private void incrementViewCount(Long articleId) {
        String viewKey = ARTICLE_VIEW_COUNT_KEY + articleId;
        
        // 使用Redis原子递增
        Long viewCount = redisService.incr(viewKey, 1);
        
        // 每50次阅读同步到数据库一次（减少数据库压力）
        if (viewCount % 50 == 0) {
            articleRepository.findById(articleId).ifPresent(article -> {
                article.setViewCount(viewCount.intValue());
                articleRepository.save(article);
                
                // 更新文章缓存中的阅读数
                String articleKey = ARTICLE_CACHE_KEY + articleId;
                Article cachedArticle = (Article) redisService.get(articleKey);
                if (cachedArticle != null) {
                    cachedArticle.setViewCount(viewCount.intValue());
                    redisService.set(articleKey, cachedArticle, ARTICLE_CACHE_EXPIRE);
                }
            });
        }
        
        // 设置过期时间（1天）
        if (viewCount == 1) {
            redisService.expire(viewKey, 24 * 60 * 60);
        }
    }
    
    /**
     * 更新文章（支持修改分类和标签）
     */
    @Transactional
    public Article updateArticle(Long id, Article articleDetails, Long categoryId, List<String> tagNames) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        // 记录旧的分类和标签，用于更新计数
        Category oldCategory = article.getCategory();
        Set<Tag> oldTags = new HashSet<>(article.getTags());
        
        // 更新基本信息
        article.setTitle(articleDetails.getTitle());
        article.setContent(articleDetails.getContent());
        article.setSummary(articleDetails.getSummary());
        article.setCoverImage(articleDetails.getCoverImage());
        article.setStatus(articleDetails.getStatus());
        
        // 更新分类
        if (categoryId != null) {
            Category newCategory = categoryService.getCategoryById(categoryId)
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            
            if (oldCategory != null && !oldCategory.getId().equals(categoryId)) {
                categoryService.decrementArticleCount(oldCategory.getId());
                categoryService.incrementArticleCount(categoryId);
            } else if (oldCategory == null) {
                categoryService.incrementArticleCount(categoryId);
            }
            
            article.setCategory(newCategory);
        } else if (oldCategory != null) {
            categoryService.decrementArticleCount(oldCategory.getId());
            article.setCategory(null);
        }
        
        // 更新标签
        if (tagNames != null) {
            Set<Tag> newTags = tagService.createOrGetTags(tagNames);
            
            Set<Tag> tagsToRemove = new HashSet<>(oldTags);
            tagsToRemove.removeAll(newTags);
            if (!tagsToRemove.isEmpty()) {
                tagService.decrementArticleCount(tagsToRemove);
            }
            
            Set<Tag> tagsToAdd = new HashSet<>(newTags);
            tagsToAdd.removeAll(oldTags);
            if (!tagsToAdd.isEmpty()) {
                tagService.incrementArticleCount(tagsToAdd);
            }
            
            article.setTags(newTags);
        } else {
            // 如果tagNames为null，表示不清空标签
            // 如果需要清空标签，传递空列表 []
        }
        
        Article updatedArticle = articleRepository.save(article);
        
        // 清除相关缓存
        clearArticleCaches();
        redisService.del(ARTICLE_CACHE_KEY + id);
        
        if (oldCategory != null) {
            redisService.del(ARTICLES_BY_CATEGORY_KEY + oldCategory.getId() + ":*");
        }
        if (categoryId != null) {
            redisService.del(ARTICLES_BY_CATEGORY_KEY + categoryId + ":*");
        }
        
        // 清除标签相关缓存
        if (oldTags != null && !oldTags.isEmpty()) {
            for (Tag tag : oldTags) {
                redisService.del(ARTICLES_BY_TAG_KEY + tag.getId() + ":*");
            }
        }
        
        return updatedArticle;
    }
    
    /**
     * 根据分类获取文章（带缓存，返回Page）
     */
    public Page<Article> getArticlesByCategory(Long categoryId, Integer status, Pageable pageable) {
        String cacheKey = ARTICLES_BY_CATEGORY_KEY + categoryId + ":status:" + status + 
                          ":page:" + pageable.getPageNumber() + ":size:" + pageable.getPageSize();
        
        Page<Article> articles = (Page<Article>) redisService.get(cacheKey);
        if (articles != null) {
            return articles;
        }
        
        articles = articleRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
        redisService.set(cacheKey, articles, CATEGORY_TAG_ARTICLES_EXPIRE);
        return articles;
    }
    
    /**
     * 根据标签获取文章（带缓存，返回Page）
     */
    public Page<Article> getArticlesByTag(Long tagId, Integer status, Pageable pageable) {
        String cacheKey = ARTICLES_BY_TAG_KEY + tagId + ":status:" + status + 
                          ":page:" + pageable.getPageNumber() + ":size:" + pageable.getPageSize();
        
        Page<Article> articles = (Page<Article>) redisService.get(cacheKey);
        if (articles != null) {
            return articles;
        }
        
        articles = articleRepository.findByTagsIdAndStatus(tagId, status, pageable);
        redisService.set(cacheKey, articles, CATEGORY_TAG_ARTICLES_EXPIRE);
        return articles;
    }
    
    /**
     * 获取热门文章（带缓存，返回List）
     */
    public List<Article> getHotArticles(int limit) {
        String cacheKey = ARTICLES_HOT_KEY + ":" + limit;
        
        List<Article> articles = (List<Article>) redisService.get(cacheKey);
        if (articles != null) {
            return articles;
        }
        
        // 直接使用Repository方法获取按阅读数排序的文章列表
        List<Article> allArticles = articleRepository.findByStatusOrderByViewCountDesc(1);
        
        // 取前limit条
        articles = allArticles.stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        // 存入缓存
        if (!articles.isEmpty()) {
            redisService.set(cacheKey, articles, HOT_ARTICLES_EXPIRE);
        }
        
        return articles;
    }
    
    /**
     * 获取最新文章（带缓存，返回List）
     */
    public List<Article> getLatestArticles(int limit) {
        String cacheKey = ARTICLES_LATEST_KEY + ":" + limit;
        
        List<Article> articles = (List<Article>) redisService.get(cacheKey);
        if (articles != null) {
            return articles;
        }
        
        // 直接使用Repository方法获取按创建时间排序的文章列表
        List<Article> allArticles = articleRepository.findByStatusOrderByCreatedAtDesc(1);
        
        // 取前limit条
        articles = allArticles.stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        if (!articles.isEmpty()) {
            redisService.set(cacheKey, articles, HOT_ARTICLES_EXPIRE);
        }
        
        return articles;
    }
    
    /**
     * 获取已发布的文章（带缓存，返回List）
     */
    public List<Article> getPublishedArticles() {
        List<Article> articles = (List<Article>) redisService.get(ARTICLES_PUBLISHED_KEY);
        if (articles != null) {
            return articles;
        }
        
        // 直接使用Repository方法获取已发布的文章列表
        articles = articleRepository.findByStatusOrderByCreatedAtDesc(1);
        
        if (!articles.isEmpty()) {
            redisService.set(ARTICLES_PUBLISHED_KEY, articles, HOT_ARTICLES_EXPIRE);
        }
        
        return articles;
    }
    
    /**
     * 获取已发布的文章（分页）
     */
    public Page<Article> getPublishedArticles(Pageable pageable) {
        return articleRepository.findByStatusOrderByCreatedAtDesc(1, pageable);
    }
    
    /**
     * 搜索文章（带缓存，返回List）
     */
    public List<Article> searchArticles(String keyword) {
        // 验证关键词，防止潜在的注入攻击
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPublishedArticles(); // 返回全部已发布文章
        }

        // 对关键词进行清理，移除潜在危险字符
        keyword = keyword.replaceAll("[<>\"'%;\\-]", "");
        if (keyword.length() > 50) {
            keyword = keyword.substring(0, 50); // 限制关键词长度
        }

        // 为搜索创建缓存key（包含关键词的hash）
        String searchHash = String.valueOf(keyword.hashCode());
        String cacheKey = ARTICLES_SEARCH_KEY + searchHash;
        
        List<Article> articles = (List<Article>) redisService.get(cacheKey);
        if (articles != null) {
            return articles;
        }
        
        // 使用Repository的安全搜索方法
        articles = articleRepository.searchArticles(keyword);
        
        // 只缓存非空结果，缓存5分钟
        if (!articles.isEmpty()) {
            redisService.set(cacheKey, articles, SEARCH_CACHE_EXPIRE);
        }
        
        return articles;
    }
    
    /**
     * 搜索文章（分页）
     */
    public Page<Article> searchArticles(String keyword, Pageable pageable) {
        // 验证关键词，防止潜在的注入攻击
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPublishedArticles(pageable); // 返回全部已发布文章
        }

        // 对关键词进行清理，移除潜在危险字符
        keyword = keyword.replaceAll("[<>\"'%;\\-]", "");
        if (keyword.length() > 50) {
            keyword = keyword.substring(0, 50); // 限制关键词长度
        }

        // 使用Repository的安全搜索方法
        return articleRepository.searchArticles(keyword, pageable);
    }
    
    /**
     * 删除文章
     */
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        // 减少分类计数
        if (article.getCategory() != null) {
            categoryService.decrementArticleCount(article.getCategory().getId());
        }
        
        // 减少标签计数
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            tagService.decrementArticleCount(article.getTags());
        }
        
        articleRepository.deleteById(id);
        
        // 清除相关缓存
        clearArticleCaches();
        redisService.del(ARTICLE_CACHE_KEY + id);
        redisService.del(ARTICLE_VIEW_COUNT_KEY + id);
        
        // 清除分类相关缓存
        if (article.getCategory() != null) {
            redisService.del(ARTICLES_BY_CATEGORY_KEY + article.getCategory().getId() + ":*");
        }
        
        // 清除标签相关缓存
        if (article.getTags() != null) {
            for (Tag tag : article.getTags()) {
                redisService.del(ARTICLES_BY_TAG_KEY + tag.getId() + ":*");
            }
        }
    }
    
    /**
     * 点赞文章（带缓存）
     */
    @Transactional
    public Article likeArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        article.setLikeCount(article.getLikeCount() + 1);
        Article likedArticle = articleRepository.save(article);
        
        // 更新缓存
        String articleKey = ARTICLE_CACHE_KEY + id;
        Article cachedArticle = (Article) redisService.get(articleKey);
        if (cachedArticle != null) {
            cachedArticle.setLikeCount(likedArticle.getLikeCount());
            redisService.set(articleKey, cachedArticle, ARTICLE_CACHE_EXPIRE);
        }
        
        // 清除热门文章缓存（点赞可能影响热门排行）
        redisService.del(ARTICLES_HOT_KEY + ":*");
        
        return likedArticle;
    }
    
    /**
     * 获取所有文章（分页）- 这个方法不用缓存，因为分页数据变化频繁
     */
    public Page<Article> getAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }
    
    /**
     * 获取用户的所有文章（带缓存，返回List）
     */
    public List<Article> getArticlesByUser(Long userId) {
        String cacheKey = ARTICLES_USER_KEY + userId;
        
        List<Article> articles = (List<Article>) redisService.get(cacheKey);
        if (articles != null) {
            return articles;
        }
        
        // 使用Repository方法获取用户的所有文章
        articles = articleRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        
        // 缓存用户文章列表10分钟
        if (!articles.isEmpty()) {
            redisService.set(cacheKey, articles, 10 * 60);
        }
        
        return articles;
    }
    
    /**
     * 获取用户的已发布文章（带缓存，返回List）
     */
    public List<Article> getPublishedArticlesByUser(Long userId) {
        String cacheKey = ARTICLES_USER_KEY + userId + ":published";
        
        List<Article> articles = (List<Article>) redisService.get(cacheKey);
        if (articles != null) {
            return articles;
        }
        
        // 先获取用户的所有文章，然后过滤出已发布的
        List<Article> allArticles = articleRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        articles = allArticles.stream()
                .filter(article -> article.getStatus() == 1)
                .collect(Collectors.toList());
        
        if (!articles.isEmpty()) {
            redisService.set(cacheKey, articles, 10 * 60);
        }
        
        return articles;
    }
    
    /**
     * 更新热门文章缓存（如果文章变得热门）
     */
    private void updateHotArticlesCacheIfNeeded(Article article) {
        // 如果文章阅读量超过500，强制更新热门文章缓存
        if (article.getViewCount() > 500) {
            redisService.del(ARTICLES_HOT_KEY + ":*");
        }
    }
    
    /**
     * 清除文章列表相关缓存
     */
    private void clearArticleCaches() {
        // 使用模糊删除清除所有相关缓存
        redisService.del(ARTICLES_HOT_KEY + ":*");
        redisService.del(ARTICLES_LATEST_KEY + ":*");
        redisService.del(ARTICLES_PUBLISHED_KEY);
        redisService.del(ARTICLES_SEARCH_KEY + "*");
        redisService.del(ARTICLES_USER_KEY + "*");
        redisService.del("articles:advanced:*");
    }
    
    /**
     * 高级搜索文章（带缓存）
     */
    public Page<Article> searchArticlesAdvanced(SearchRequest searchRequest) {
        // 生成缓存key
        String cacheKey = generateSearchCacheKey(searchRequest);
        String countCacheKey = cacheKey + ":count";
        
        // 尝试从缓存获取结果
        Object cachedResult = redisService.get(cacheKey);
        Object cachedCount = redisService.get(countCacheKey);
        
        if (cachedResult != null && cachedCount != null) {
            List<Article> articles = (List<Article>) cachedResult;
            Long totalCount = (Long) cachedCount;
            
            return new PageImpl<>(articles, 
                PageRequest.of(searchRequest.getPage(), searchRequest.getSize()), 
                totalCount);
        }
        
        // 执行数据库查询
        List<Article> articles = searchArticles(searchRequest);
        int totalCount = searchArticleCount(searchRequest);
        
        // 保存到缓存（5分钟过期）
        if (!articles.isEmpty()) {
            redisService.set(cacheKey, articles, SEARCH_CACHE_EXPIRE);
        }
        redisService.set(countCacheKey, (long) totalCount, SEARCH_CACHE_EXPIRE);
        
        return new PageImpl<>(articles, 
            PageRequest.of(searchRequest.getPage(), searchRequest.getSize()), 
            totalCount);
    }
    
    /**
     * 生成搜索缓存key
     */
    private String generateSearchCacheKey(SearchRequest searchRequest) {
        StringBuilder keyBuilder = new StringBuilder("articles:advanced:");
        
        if (searchRequest.getKeyword() != null) {
            keyBuilder.append("keyword:").append(searchRequest.getKeyword().hashCode()).append(":");
        }
        
        if (searchRequest.getCategoryId() != null) {
            keyBuilder.append("category:").append(searchRequest.getCategoryId()).append(":");
        }
        
        if (searchRequest.getTagIds() != null && !searchRequest.getTagIds().isEmpty()) {
            List<Long> sortedTags = new ArrayList<>(searchRequest.getTagIds());
            Collections.sort(sortedTags);
            keyBuilder.append("tags:").append(sortedTags.hashCode()).append(":");
        }
        
        if (searchRequest.getAuthor() != null) {
            keyBuilder.append("author:").append(searchRequest.getAuthor().hashCode()).append(":");
        }
        
        if (searchRequest.getStatus() != null) {
            keyBuilder.append("status:").append(searchRequest.getStatus()).append(":");
        }
        
        if (searchRequest.getStartDate() != null) {
            keyBuilder.append("start:").append(searchRequest.getStartDate().hashCode()).append(":");
        }
        
        if (searchRequest.getEndDate() != null) {
            keyBuilder.append("end:").append(searchRequest.getEndDate().hashCode()).append(":");
        }
        
        keyBuilder.append("sort:").append(searchRequest.getSortBy())
                  .append(":").append(searchRequest.getSortOrder())
                  .append(":page:").append(searchRequest.getPage())
                  .append(":size:").append(searchRequest.getSize());
        
        return keyBuilder.toString();
    }
    
    /**
     * 高级搜索文章
     */
    public List<Article> searchArticles(SearchRequest searchRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Article> query = cb.createQuery(Article.class);
        Root<Article> root = query.from(Article.class);
        
        // Join关联
        Join<Article, User> authorJoin = root.join("author", jakarta.persistence.criteria.JoinType.LEFT);
        Join<Article, Category> categoryJoin = root.join("category", jakarta.persistence.criteria.JoinType.LEFT);
        Join<Article, Tag> tagJoin = root.join("tags", jakarta.persistence.criteria.JoinType.LEFT);
        
        // 构建条件
        java.util.List<Predicate> predicates = new java.util.ArrayList<>();
        
        // 关键词搜索（标题、摘要、内容）
        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
            String keyword = "%" + searchRequest.getKeyword().trim() + "%";
            Predicate keywordPredicate = cb.or(
                cb.like(cb.lower(root.get("title")), cb.lower(cb.literal(keyword))),
                cb.like(cb.lower(root.get("summary")), cb.lower(cb.literal(keyword))),
                cb.like(cb.lower(root.get("content")), cb.lower(cb.literal(keyword)))
            );
            predicates.add(keywordPredicate);
        }
        
        // 标签筛选
        if (searchRequest.getTagIds() != null && !searchRequest.getTagIds().isEmpty()) {
            predicates.add(tagJoin.get("id").in(searchRequest.getTagIds()));
        }
        
        // 分类筛选
        if (searchRequest.getCategoryId() != null) {
            predicates.add(cb.equal(categoryJoin.get("id"), searchRequest.getCategoryId()));
        }
        
        // 作者筛选
        if (searchRequest.getAuthor() != null && !searchRequest.getAuthor().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(authorJoin.get("username")), 
                                 "%" + searchRequest.getAuthor().trim().toLowerCase() + "%"));
        }
        
        // 状态筛选
        if (searchRequest.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), searchRequest.getStatus()));
        }
        
        // 日期范围筛选
        if (searchRequest.getStartDate() != null && !searchRequest.getStartDate().trim().isEmpty()) {
            try {
                LocalDateTime startDateTime = LocalDateTime.parse(searchRequest.getStartDate().trim(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            } catch (Exception e) {
                // 日期格式错误，忽略该条件
            }
        }
        
        if (searchRequest.getEndDate() != null && !searchRequest.getEndDate().trim().isEmpty()) {
            try {
                LocalDateTime endDateTime = LocalDateTime.parse(searchRequest.getEndDate().trim(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1);
                predicates.add(cb.lessThan(root.get("createdAt"), endDateTime));
            } catch (Exception e) {
                // 日期格式错误，忽略该条件
            }
        }
        
        // 添加查询条件
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        
        // 排序
        String sortBy = searchRequest.getSortBy();
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdAt"; // 默认按创建时间排序
        }
        
        // 确保sortBy是有效的字段
        if (!isValidSortField(sortBy)) {
            sortBy = "createdAt";
        }
        
        String sortOrder = searchRequest.getSortOrder();
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            query.orderBy(cb.desc(root.get(sortBy)));
        } else {
            query.orderBy(cb.asc(root.get(sortBy)));
        }
        
        // 分页
        Pageable pageable = PageRequest.of(
            searchRequest.getPage(), 
            searchRequest.getSize()
        );
        
        TypedQuery<Article> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        return typedQuery.getResultList();
    }
    
    /**
     * 检查是否为有效的排序字段
     */
    private boolean isValidSortField(String field) {
        Set<String> validFields = Set.of("id", "title", "viewCount", "likeCount", "createdAt", "updatedAt");
        return validFields.contains(field);
    }
    
    /**
     * 高级搜索文章数量统计
     */
    public int searchArticleCount(SearchRequest searchRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Article> root = query.from(Article.class);
        
        // Join关联
        Join<Article, User> authorJoin = root.join("author", jakarta.persistence.criteria.JoinType.LEFT);
        Join<Article, Category> categoryJoin = root.join("category", jakarta.persistence.criteria.JoinType.LEFT);
        Join<Article, Tag> tagJoin = root.join("tags", jakarta.persistence.criteria.JoinType.LEFT);
        
        // 构建条件
        java.util.List<Predicate> predicates = new java.util.ArrayList<>();
        
        // 关键词搜索（标题、摘要、内容）
        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
            String keyword = "%" + searchRequest.getKeyword().trim() + "%";
            Predicate keywordPredicate = cb.or(
                cb.like(cb.lower(root.get("title")), cb.lower(cb.literal(keyword))),
                cb.like(cb.lower(root.get("summary")), cb.lower(cb.literal(keyword))),
                cb.like(cb.lower(root.get("content")), cb.lower(cb.literal(keyword)))
            );
            predicates.add(keywordPredicate);
        }
        
        // 标签筛选
        if (searchRequest.getTagIds() != null && !searchRequest.getTagIds().isEmpty()) {
            predicates.add(tagJoin.get("id").in(searchRequest.getTagIds()));
        }
        
        // 分类筛选
        if (searchRequest.getCategoryId() != null) {
            predicates.add(cb.equal(categoryJoin.get("id"), searchRequest.getCategoryId()));
        }
        
        // 作者筛选
        if (searchRequest.getAuthor() != null && !searchRequest.getAuthor().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(authorJoin.get("username")), 
                                 "%" + searchRequest.getAuthor().trim().toLowerCase() + "%"));
        }
        
        // 状态筛选
        if (searchRequest.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), searchRequest.getStatus()));
        }
        
        // 日期范围筛选
        if (searchRequest.getStartDate() != null && !searchRequest.getStartDate().trim().isEmpty()) {
            try {
                LocalDateTime startDateTime = LocalDateTime.parse(searchRequest.getStartDate().trim(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            } catch (Exception e) {
                // 日期格式错误，忽略该条件
            }
        }
        
        if (searchRequest.getEndDate() != null && !searchRequest.getEndDate().trim().isEmpty()) {
            try {
                LocalDateTime endDateTime = LocalDateTime.parse(searchRequest.getEndDate().trim(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(1);
                predicates.add(cb.lessThan(root.get("createdAt"), endDateTime));
            } catch (Exception e) {
                // 日期格式错误，忽略该条件
            }
        }
        
        // 添加查询条件
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        
        query.select(cb.count(root));
        
        return Math.toIntExact(entityManager.createQuery(query).getSingleResult());
    }
    
    /**
     * 更新文章的封面图片
     */
    @Transactional
    public Article updateArticleCoverImage(Long id, String coverImageUrl) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        
        // 更新封面图片
        article.setCoverImage(coverImageUrl);
        
        Article updatedArticle = articleRepository.save(article);
        
        // 清除相关缓存
        redisService.del(ARTICLE_CACHE_KEY + id);
        clearArticleCaches();
        
        return updatedArticle;
    }
}