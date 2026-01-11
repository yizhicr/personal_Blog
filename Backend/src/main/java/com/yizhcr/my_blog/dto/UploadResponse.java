package com.yizhcr.my_blog.dto;

import lombok.Data;

import java.util.List;

@Data
public class UploadResponse {
    private boolean success;
    private String message;
    private List<FileDTO> files;
    
    public UploadResponse(boolean success, String message, List<FileDTO> files) {
        this.success = success;
        this.message = message;
        this.files = files;
    }
}