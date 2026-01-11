package com.yizhcr.my_blog.dto;

import lombok.Data;

import java.util.Date;

@Data
public class FileDTO {
    private Long id;
    private String originalName;
    private String fileName;
    private String contentType;
    private Long size;
    private String url;
    private Date createdAt;
}