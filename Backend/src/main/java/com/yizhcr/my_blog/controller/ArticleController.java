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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createArticle(@RequestBody ArticleDTO articleDTO) {
        try {
            // 从JWT令牌或Security上下文中获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 从JWT令牌中获取用户ID（如果JWT中包含用户ID信息）
            // 或者通过用户名从数据库获取用户ID
            // 这里假设我们可以通过用户名获取用户ID
            Long currentUserId = getCurrentUserId(username);
            
            // 将ArticleDTO转换为Article实体
            Article article = new Article();
            article.setTitle(articleDTO.getTitle());
            article.setContent(articleDTO.getContent());
            article.setSummary(articleDTO.getSummary());
            article.setStatus(articleDTO.getStatus());
            article.setCoverImage(articleDTO.getCoverImage());

            // 使用当前用户ID，而不是依赖前端传递的authorId
            Article createdArticle = articleService.createArticle(article, currentUserId, articleDTO.getCategoryId(), articleDTO.getTagNames());
            return ResponseEntity.ok(Map.of("success", true, "data", createdArticle, "message", "文章创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文章创建失败：" + e.getMessage()));
        }
    }
    
    // 获取当前用户ID的方法
    private Long getCurrentUserId(String username) {
        return userService.findByUsername(username).map(com.yizhcr.my_blog.entity.User::getId).orElse(null);
    }
    
    // 从请求头中提取JWT令牌
    private String extractToken() {
        jakarta.servlet.http.HttpServletRequest request = ((org.springframework.web.context.request.ServletWebRequest)
            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // ... 其他方法保持不变 ...
    
    @GetMapping
    public ResponseEntity<?> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        try {
            // 使用Pageable进行分页查询
            Pageable pageable = PageRequest.of(page, size);
            List<Article> articles = articleService.getAllArticles(pageable).getContent();
            int total = (int) articleService.getAllArticles(pageable).getTotalElements();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", articles,
                    "pagination", Map.of("page", page, "size", size, "total", total),
                    "message", "获取文章列表成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取文章列表失败：" + e.getMessage()));
        }
    }
    
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        try {
            // 使用Pageable进行分页查询
            Pageable pageable = PageRequest.of(page, size);
            Page<Article> result = articleService.getPublishedArticles(pageable); // 在服务层执行分页
            List<Article> articles = result.getContent();
            int total = (int) result.getTotalElements();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", articles,
                    "pagination", Map.of("page", page, "size", size, "total", total),
                    "message", "获取已发布文章列表成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取已发布文章列表失败：" + e.getMessage()));
        }
    }
    
    
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getArticle(@PathVariable Long id) {
        try {
            Article article = articleService.getArticleById(id).orElse(null);
            if (article != null) {
                // 更新浏览次数
                articleService.increaseViewCount(id);
                
                // 添加评论数量信息
                long commentCount = commentService.countCommentsByArticleId(id, 1);
                
                return ResponseEntity.ok(Map.of("success", true, "data", article, "commentCount", commentCount, "message", "获取文章成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文章不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取文章失败：" + e.getMessage()));
        }
    }

    
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO articleDTO) {
        try {
            // 从Security上下文获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 通过用户名获取用户实体，从中提取ID
            Optional<com.yizhcr.my_blog.entity.User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户不存在"));
            }
            
            Long currentUserId = userOpt.get().getId();
            
            // 验证当前用户是否有权限更新此文章
            Optional<Article> existingArticleOpt = articleService.getArticleById(id);
            if (!existingArticleOpt.isPresent() || 
                !existingArticleOpt.get().getAuthor().getId().equals(currentUserId)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "无权更新此文章"));
            }
            
            // 将ArticleDTO转换为Article实体
            Article article = new Article();
            article.setTitle(articleDTO.getTitle());
            article.setContent(articleDTO.getContent());
            article.setSummary(articleDTO.getSummary());
            article.setStatus(articleDTO.getStatus());
            article.setCoverImage(articleDTO.getCoverImage());

            Article updatedArticle = articleService.updateArticle(id, article, articleDTO.getCategoryId(), articleDTO.getTagNames());
            if (updatedArticle != null) {
                return ResponseEntity.ok(Map.of("success", true, "data", updatedArticle, "message", "文章更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文章不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文章更新失败：" + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        try {
            // 验证当前用户是否有权限删除此文章
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<com.yizhcr.my_blog.entity.User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户不存在"));
            }
            
            Long currentUserId = userOpt.get().getId();
            
            Optional<Article> existingArticleOpt = articleService.getArticleById(id);
            if (!existingArticleOpt.isPresent() || 
                !existingArticleOpt.get().getAuthor().getId().equals(currentUserId)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "无权删除此文章"));
            }
            
            articleService.deleteArticle(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "文章删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "文章删除失败：" + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 使用Pageable进行分页查询
            Pageable pageable = PageRequest.of(page, size);
            Page<Article> result = articleService.searchArticles(keyword, pageable); // 在服务层执行分页
            List<Article> articles = result.getContent();
            int total = (int) result.getTotalElements();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", articles,
                    "pagination", Map.of("page", page, "size", size, "total", total),
                    "message", "搜索文章成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "搜索文章失败：" + e.getMessage()));
        }
    }
    
    @PostMapping("/advanced-search")
    public ResponseEntity<?> advancedSearch(@RequestBody SearchRequest searchRequest) {
        try {
            var result = articleService.searchArticlesAdvanced(searchRequest);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result.getContent(),
                    "pagination", Map.of(
                            "page", searchRequest.getPage(),
                            "size", searchRequest.getSize(),
                            "total", result.getTotalElements()
                    ),
                    "message", "高级搜索成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "高级搜索失败：" + e.getMessage()));
        }
    }
    
    // 添加一个获取最新文章的API
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestArticles() {
        try {
            List<Article> articles = articleService.getLatestArticles(10); // 获取最新的10篇文章
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", articles,
                    "message", "获取最新文章成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "获取最新文章失败：" + e.getMessage()));
        }
    }
}