package com.yizhcr.my_blog.repository;

import com.yizhcr.my_blog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    // 根据slug查找标签
    Optional<Tag> findBySlug(String slug);
    
    // 根据名称查找标签
    Optional<Tag> findByName(String name);
    
    // 根据名称列表查找标签
    List<Tag> findByNameIn(List<String> names);
    
    // 查找热门标签（按文章数量降序）
    List<Tag> findTop10ByOrderByArticleCountDesc();
    
    // 增加标签文章数量
    @Modifying
    @Query("UPDATE Tag t SET t.articleCount = t.articleCount + 1 WHERE t.id IN :tagIds")
    void incrementArticleCount(@Param("tagIds") Set<Long> tagIds);
    
    // 减少标签文章数量
    @Modifying
    @Query("UPDATE Tag t SET t.articleCount = t.articleCount - 1 WHERE t.id IN :tagIds AND t.articleCount > 0")
    void decrementArticleCount(@Param("tagIds") Set<Long> tagIds);
}