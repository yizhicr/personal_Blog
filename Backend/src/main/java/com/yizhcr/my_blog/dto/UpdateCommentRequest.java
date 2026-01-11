package com.yizhcr.my_blog.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;

@Data
public class UpdateCommentRequest {
    @Max(value = 1000, message = "评论内容不能超过1000字符")
    private String content;
    
    private Integer status; // 0-待审核，1-已发布，2-已删除，3-垃圾评论
    
    public void setContent(String content) {
        if (content != null && content.length() > 1000) {
            this.content = content.substring(0, 1000);
        } else {
            this.content = content;
        }
    }
}