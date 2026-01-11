package com.yizhcr.my_blog.repository;

import com.yizhcr.my_blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    // 根据分类ID和状态查询文章（分页）
    Page<Article> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);
    
    // 根据标签ID和状态查询文章（分页）- 修复：使用tags.id而不是tagId
    Page<Article> findByTagsIdAndStatus(Long tagId, Integer status, Pageable pageable);
    
    // 根据作者和状态查询文章（分页）
    Page<Article> findByAuthorIdAndStatus(Long authorId, Integer status, Pageable pageable);
    
    // 根据状态查询文章，按创建时间倒序排列（分页）
    Page<Article> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);
    
    // 根据状态查询文章，按创建时间倒序排列（列表）
    List<Article> findByStatusOrderByCreatedAtDesc(Integer status);
    
    // 根据状态查询文章，按阅读量倒序排列（分页）
    Page<Article> findByStatusOrderByViewCountDesc(Integer status, Pageable pageable);
    
    // 根据状态查询文章，按阅读量倒序排列（列表）
    List<Article> findByStatusOrderByViewCountDesc(Integer status);
    
    // 根据作者ID查询文章，按创建时间倒序排列
    List<Article> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    // 根据分类ID查询文章
    List<Article> findByCategoryId(Long categoryId);
    
    // 根据标签ID查询文章 - 修复：使用tags.id而不是tagId
    List<Article> findByTagsId(Long tagId);
    
    // 根据状态查询文章
    List<Article> findByStatus(Integer status);
    
    // 使用原生SQL进行安全的全文搜索
    @Query(value = "SELECT * FROM articles WHERE status = 1 AND " +
            "(title LIKE %:keyword% OR content LIKE %:keyword% OR summary LIKE %:keyword%)", 
           nativeQuery = true)
    List<Article> searchArticles(@Param("keyword") String keyword);
    
    @Query(value = "SELECT * FROM articles WHERE status = 1 AND " +
            "(title LIKE %:keyword% OR content LIKE %:keyword% OR summary LIKE %:keyword%)", 
           nativeQuery = true)
    Page<Article> searchArticles(@Param("keyword") String keyword, Pageable pageable);
}