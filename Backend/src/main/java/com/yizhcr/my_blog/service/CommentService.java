package com.yizhcr.my_blog.service;

import com.yizhcr.my_blog.dto.CommentDTO;
import com.yizhcr.my_blog.dto.CreateCommentRequest;
import com.yizhcr.my_blog.dto.UpdateCommentRequest;
import com.yizhcr.my_blog.entity.Article;
import com.yizhcr.my_blog.entity.Comment;
import com.yizhcr.my_blog.entity.User;
import com.yizhcr.my_blog.repository.ArticleRepository;
import com.yizhcr.my_blog.repository.CommentRepository;
import com.yizhcr.my_blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 发布评论或回复
     */
    public CommentDTO createComment(CreateCommentRequest request, Long userId) {
        // 获取文章
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new RuntimeException("文章不存在: " + request.getArticleId()));

        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        System.out.println("Debug: Creating comment for article: " + request.getArticleId() + 
                          ", by user: " + userId + ", content: " + request.getContent());

        // 创建评论
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setArticle(article);
        comment.setAuthor(user);

        // 如果是回复，则设置父评论
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论不存在: " + request.getParentId()));
            
            // 设置层级，防止嵌套太深
            int depth = parent.getDepth() + 1;
            if (depth > 5) {
                throw new RuntimeException("评论嵌套层级不能超过5层");
            }
            comment.setParent(parent);
            comment.setDepth(depth);
        }

        // 设置状态为已发布
        comment.setStatus(1);
        System.out.println("Debug: Setting comment status to: " + comment.getStatus());

        // 保存评论
        Comment savedComment = commentRepository.save(comment);
        System.out.println("Debug: Saved comment with ID: " + savedComment.getId());

        // 验证评论是否成功保存
        Comment verification = commentRepository.findById(savedComment.getId()).orElse(null);
        if (verification != null) {
            System.out.println("Debug: Verification - Comment exists in DB with ID: " + verification.getId() + 
                              ", content: " + verification.getContent() + 
                              ", articleId: " + verification.getArticle().getId());
        } else {
            System.out.println("Debug: ERROR - Could not find saved comment in DB!");
        }

        // 转换为DTO返回
        return convertToDTO(savedComment);
    }

    /**
     * 根据文章ID获取评论列表
     */
    public List<CommentDTO> getCommentsByArticleId(Long articleId, Integer status) {
        if (status == null) {
            status = 1; // 默认查询已发布的评论
        }

        System.out.println("Debug: Querying comments for articleId: " + articleId + ", with status: " + status);

        // 获取一级评论
        List<Comment> topLevelComments = commentRepository.findByArticleIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(
                articleId, status);

        System.out.println("Debug: Found " + topLevelComments.size() + " top-level comments");

        List<CommentDTO> result = new ArrayList<>();
        for (Comment comment : topLevelComments) {
            System.out.println("Debug: Processing top-level comment ID: " + comment.getId() + ", Content: " + comment.getContent());
            result.add(convertToDTOWithChildren(comment));
        }

        System.out.println("Debug: Returning " + result.size() + " comments with children processed");

        return result;
    }

    /**
     * 分页获取文章评论
     */
    public Page<CommentDTO> getCommentsByArticleIdWithPagination(Long articleId, Integer status, Pageable pageable) {
        if (status == null) {
            status = 1; // 默认查询已发布的评论
        }

        Page<Comment> commentsPage = commentRepository.findByArticleIdWithPagination(articleId, status, pageable);

        return commentsPage.map(this::convertToDTO);
    }

    /**
     * 更新评论
     */
    public CommentDTO updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 检查权限：只有评论作者才能编辑自己的评论
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("没有权限编辑此评论");
        }

        // 更新内容
        if (request.getContent() != null) {
            comment.setContent(request.getContent());
        }

        // 更新状态
        if (request.getStatus() != null) {
            comment.setStatus(request.getStatus());
        }

        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);

        return convertToDTO(updatedComment);
    }

    /**
     * 删除评论（软删除，改为删除状态）
     */
    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 检查权限：评论作者或文章作者才能删除评论
        boolean isCommentAuthor = comment.getAuthor().getId().equals(userId);
        boolean isArticleAuthor = comment.getArticle().getAuthor().getId().equals(userId);
        
        if (!isCommentAuthor && !isArticleAuthor) {
            throw new RuntimeException("没有权限删除此评论");
        }

        // 设置为已删除状态
        comment.setStatus(2);
        commentRepository.save(comment);

        return true;
    }

    /**
     * 获取用户评论列表
     */
    public List<CommentDTO> getCommentsByUserId(Long userId, Integer status) {
        if (status == null) {
            status = 1; // 默认查询已发布的评论
        }

        List<Comment> comments = commentRepository.findByAuthorIdAndStatus(userId, status);

        List<CommentDTO> result = new ArrayList<>();
        for (Comment comment : comments) {
            result.add(convertToDTO(comment));
        }

        return result;
    }

    /**
     * 统计文章评论数量
     */
    public long countCommentsByArticleId(Long articleId, Integer status) {
        if (status == null) {
            status = 1; // 默认统计已发布的评论
        }

        return commentRepository.countByArticleIdAndStatusAndParentIdIsNull(articleId, status);
    }

    /**
     * 将实体转换为DTO，并包含子评论
     */
    private CommentDTO convertToDTOWithChildren(Comment comment) {
        System.out.println("Debug: Converting comment to DTO with children, ID: " + comment.getId());
        
        CommentDTO dto = convertToDTO(comment);

        // 获取子评论
        List<Comment> children = commentRepository.findByParentIdAndStatusOrderByCreatedAtDesc(
                comment.getId(), 1);
        
        System.out.println("Debug: Found " + children.size() + " child comments for comment ID: " + comment.getId());

        // 注意：这里只是简单的展示思路，实际实现可能需要递归处理子评论
        // 为了简单起见，我们只加载一级子评论

        return dto;
    }
    
    /**
     * 将实体转换为DTO
     */
    private CommentDTO convertToDTO(Comment comment) {
        System.out.println("Debug: Converting comment entity to DTO, ID: " + comment.getId());
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        System.out.println("Debug: Setting content: " + comment.getContent());
        dto.setArticleId(comment.getArticle().getId());
        System.out.println("Debug: Setting article ID: " + comment.getArticle().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        System.out.println("Debug: Setting author ID: " + comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getNickname() != null ? 
                         comment.getAuthor().getNickname() : comment.getAuthor().getUsername());
        dto.setAuthorAvatar(comment.getAuthor().getAvatar());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setStatus(comment.getStatus());
        dto.setDepth(comment.getDepth());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        System.out.println("Debug: Finished converting comment to DTO, ID: " + comment.getId());
        return dto;
    }

}