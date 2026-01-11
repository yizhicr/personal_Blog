package com.yizhcr.my_blog.repository;

import com.yizhcr.my_blog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // 根据slug查找分类
    Optional<Category> findBySlug(String slug);
    
    // 根据名称查找分类
    Optional<Category> findByName(String name);
    
    // 查找所有分类（按排序号升序）
    List<Category> findAllByOrderByOrderNumAsc();
    
    // 增加分类文章数量
    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount + 1 WHERE c.id = :categoryId")
    void incrementArticleCount(@Param("categoryId") Long categoryId);
    
    // 减少分类文章数量
    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount - 1 WHERE c.id = :categoryId AND c.articleCount > 0")
    void decrementArticleCount(@Param("categoryId") Long categoryId);
}