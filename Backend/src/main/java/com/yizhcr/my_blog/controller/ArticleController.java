package com.yizhcr.my_blog.controller;

import com.yizhcr.my_blog.dto.ArticleDTO;
import com.yizhcr.my_blog.dto.SearchRequest;
import com.yizhcr.my_blog.entity.Article;
import com.yizhcr.my_blog.entity.Comment;
import com.yizhcr.my_blog.service.ArticleService;
import com.yizhcr.my_blog.service.CommentService;
import com.yizhcr.my_blog.service.FileService;
import com.yizhcr.my_blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody ArticleDTO articleDTO) {
        try {
            // 从安全上下文获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 通过用户名获取用户ID
            Long currentUserId = userService.findByUsername(username)
                .map(com.yizhcr.my_blog.entity.User::getId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 设置文章作者ID
            articleDTO.setAuthorId(currentUserId);
            
            // 调用服务层创建文章
            ArticleDTO createdArticle = articleService.createArticle(articleDTO);
            return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ... 其他方法保持不变 ...
    
    @GetMapping
    public ResponseEntity<List<ArticleDTO>> getArticles() {
        try {
            List<ArticleDTO> articles = articleService.getAllArticles();
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/published")
    public ResponseEntity<List<ArticleDTO>> getPublishedArticles() {
        try {
            List<ArticleDTO> articles = articleService.getAllArticles();
            // 过滤掉未发布的文章
            articles.removeIf(article -> article.getStatus() != 1);
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    
    
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticle(@PathVariable Long id) {
        try {
            ArticleDTO article = articleService.getArticleById(id);
            if (article != null) {
                // 更新浏览次数
                articleService.incrementViewCount(id);
                
                return ResponseEntity.ok(article);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<ArticleDTO> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO articleDTO) {
        try {
            // 从安全上下文获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 通过用户名获取用户ID
            Long currentUserId = userService.findByUsername(username)
                .map(com.yizhcr.my_blog.entity.User::getId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 验证当前用户是否有权限更新此文章
            ArticleDTO existingArticle = articleService.getArticleById(id);
            if (existingArticle == null || !existingArticle.getAuthorId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 设置文章作者ID
            articleDTO.setAuthorId(currentUserId);
            
            // 调用服务层更新文章
            ArticleDTO updatedArticle = articleService.updateArticle(id, articleDTO);
            if (updatedArticle != null) {
                return ResponseEntity.ok(updatedArticle);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        try {
            // 从安全上下文获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 通过用户名获取用户ID
            Long currentUserId = userService.findByUsername(username)
                .map(com.yizhcr.my_blog.entity.User::getId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 验证当前用户是否有权限删除此文章
            ArticleDTO existingArticle = articleService.getArticleById(id);
            if (existingArticle == null || !existingArticle.getAuthorId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            articleService.deleteArticle(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 添加一个获取最新文章的API
    @GetMapping("/latest")
    public ResponseEntity<List<ArticleDTO>> getLatestArticles(@RequestParam(defaultValue = "10") int count) {
        try {
            List<ArticleDTO> articles = articleService.getAllArticles();
            // 按创建日期排序并限制数量
            articles.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            if (articles.size() > count) {
                articles = articles.subList(0, count);
            }
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}