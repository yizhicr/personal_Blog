package com.yizhcr.my_blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// 显式导入相关实体类
import com.yizhcr.my_blog.entity.User;
import com.yizhcr.my_blog.entity.Category;
import com.yizhcr.my_blog.entity.Tag;
import com.yizhcr.my_blog.entity.Comment;

@Entity
@Table(name = "articles")
@Data
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    
    @Column(length = 500)
    private String summary;
    
    @Column(name = "cover_image", length = 255)
    private String coverImage;
    
    // 外键关联用户表
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    // 外键关联分类表（多对一）
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(nullable = false)
    private Integer status = 1; // 0:草稿, 1:发布
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "like_count")
    private Integer likeCount = 0;
    
    // 文章-标签多对多关联
    @ManyToMany
    @JoinTable(
        name = "article_tag",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    
    // 一篇文章可以有多个评论
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}