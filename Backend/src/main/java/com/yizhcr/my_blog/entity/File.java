package com.yizhcr.my_blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@Table(name = "files")
@Data
@EntityListeners(AuditingEntityListener.class)
public class File {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String originalName;
    
    @Column(nullable = false, unique = true)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private Long size;
    
    @Column
    private String url;
    
    @CreatedDate
    private Date createdAt;
    
    @LastModifiedDate
    private Date updatedAt;
}