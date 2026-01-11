package com.yizhcr.my_blog.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private Long articleId;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Long parentId;
    private Integer status;
    private Integer depth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}