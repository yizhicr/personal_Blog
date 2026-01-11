package com.yizhcr.my_blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Data
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(nullable = false, unique = true, length = 50)
    private String slug;
    
    @Column(length = 255)
    private String description;
    
    @Column(name = "order_num")
    private Integer orderNum = 0;
    
    @Column(name = "article_count")
    private Integer articleCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}