package com.yizhcr.my_blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tags")
@Data
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 30)
    private String name;
    
    @Column(nullable = false, unique = true, length = 50)
    private String slug;
    
    @Column(length = 7)
    private String color = "#666666";
    
    @Column(name = "article_count")
    private Integer articleCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}