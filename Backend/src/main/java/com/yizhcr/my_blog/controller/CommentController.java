package com.yizhcr.my_blog.controller;

import com.yizhcr.my_blog.dto.CommentDTO;
import com.yizhcr.my_blog.dto.CreateCommentRequest;
import com.yizhcr.my_blog.dto.UpdateCommentRequest;
import com.yizhcr.my_blog.entity.User;
import com.yizhcr.my_blog.repository.UserRepository;
import com.yizhcr.my_blog.service.CommentService;
import com.yizhcr.my_blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;

    /**
     * 发布评论或回复
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CreateCommentRequest request) {
        try {
            // 从Security Context获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 通过用户名获取用户ID
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            Long currentUserId = userOpt.get().getId();

            CommentDTO comment = commentService.createComment(request, currentUserId);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            // 记录错误日志
            System.out.println("Error creating comment: " + e.getMessage());
            e.printStackTrace();
            // 返回500错误
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取文章的所有评论
     */
    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByArticleId(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Integer status) {
        List<CommentDTO> comments = commentService.getCommentsByArticleId(articleId, status);
        return ResponseEntity.ok(comments);
    }

    /**
     * 分页获取文章评论
     */
    @GetMapping("/article/{articleId}/page")
    public ResponseEntity<Page<CommentDTO>> getCommentsByArticleIdWithPagination(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") Integer status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommentDTO> comments = commentService.getCommentsByArticleIdWithPagination(articleId, status, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * 更新评论
     */
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        try {
            // 从Security Context获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 通过用户名获取用户ID
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            Long currentUserId = userOpt.get().getId();

            CommentDTO updatedComment = commentService.updateComment(commentId, request, currentUserId);
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            System.out.println("Error updating comment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> deleteComment(@PathVariable Long commentId) {
        try {
            // 从Security Context获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 通过用户名获取用户ID
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            Long currentUserId = userOpt.get().getId();

            // 调用服务层进行删除，服务层会处理权限检查（用户只能删除自己的评论）
            Boolean result = commentService.deleteComment(commentId, currentUserId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Error deleting comment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取用户评论列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer status) {
        List<CommentDTO> comments = commentService.getCommentsByUserId(userId, status);
        return ResponseEntity.ok(comments);
    }

    /**
     * 统计文章评论数量
     */
    @GetMapping("/count/article/{articleId}")
    public ResponseEntity<Long> countCommentsByArticleId(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Integer status) {
        Long count = commentService.countCommentsByArticleId(articleId, status);
        return ResponseEntity.ok(count);
    }

}