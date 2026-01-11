package com.yizhcr.my_blog.repository;

import com.yizhcr.my_blog.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 根据文章ID查找已发布的评论（按时间排序）
    List<Comment> findByArticleIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(Long articleId, Integer status);
    
    // 查找指定父评论下的子评论
    List<Comment> findByParentIdAndStatusOrderByCreatedAtDesc(Long parentId, Integer status);
    
    // 分页查询文章下所有评论（包括回复）
    @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findByArticleIdWithPagination(@Param("articleId") Long articleId, 
                                               @Param("status") Integer status, 
                                               Pageable pageable);
                                               
    // 统计文章的评论数量
    long countByArticleIdAndStatusAndParentIdIsNull(Long articleId, Integer status);
    
    // 根据用户ID查找评论
    List<Comment> findByAuthorIdAndStatus(Long authorId, Integer status);
    
    // 统计用户发表的评论数
    long countByAuthorIdAndStatus(Long authorId, Integer status);
}