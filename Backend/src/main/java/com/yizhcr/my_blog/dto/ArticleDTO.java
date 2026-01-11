package com.yizhcr.my_blog.dto;

import com.yizhcr.my_blog.entity.Category;
import com.yizhcr.my_blog.entity.Tag;
import com.yizhcr.my_blog.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

public class ArticleDTO {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private String summary;
    
    private String coverImage;
    
    private User author;
    
    private Category category;
    
    private Integer status = 1; // 0:草稿, 1:发布
    
    private Integer viewCount = 0;
    
    private Integer likeCount = 0;
    
    private Set<Tag> tags;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // 用于创建和更新的字段
    private Long authorId;
    
    private Long categoryId;
    
    private List<String> tagNames;
    
    // 构造函数
    public ArticleDTO() {}
    
    public ArticleDTO(Long id, String title, String content, String summary, String coverImage,
                      User author, Category category, Integer status, Integer viewCount, 
                      Integer likeCount, Set<Tag> tags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.coverImage = coverImage;
        this.author = author;
        this.category = category;
        this.status = status;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Set<Tag> getTags() {
        return tags;
    }
    
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // 新增字段的getter和setter方法
    public Long getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public List<String> getTagNames() {
        return tagNames;
    }
    
    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }
}