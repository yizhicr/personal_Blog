package com.yizhcr.my_blog.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class CreateCommentRequest {
    
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000字符")
    private String content;
    
    private Long parentId; // 父评论ID，可为空
}