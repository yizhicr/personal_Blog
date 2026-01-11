package com.yizhcr.my_blog.controller;

import com.yizhcr.my_blog.dto.FileDTO;
import com.yizhcr.my_blog.dto.UploadResponse;
import com.yizhcr.my_blog.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<FileDTO> fileDTOs = fileService.saveFiles(files);
            UploadResponse response = new UploadResponse(true, "文件上传成功", fileDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            UploadResponse response = new UploadResponse(false, "文件上传失败: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取所有文件列表
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllFiles() {
        try {
            List<FileDTO> files = fileService.getAllFiles();
            return ResponseEntity.ok()
                    .body(java.util.Map.of(
                            "success", true,
                            "data", files
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                            "success", false,
                            "message", "获取文件列表失败: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * 获取单个文件信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFileById(@PathVariable Long id) {
        try {
            FileDTO file = fileService.getFileById(id);
            if (file != null) {
                return ResponseEntity.ok()
                        .body(java.util.Map.of(
                                "success", true,
                                "data", file
                        ));
            } else {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of(
                                "success", false,
                                "message", "文件不存在"
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                            "success", false,
                            "message", "获取文件失败: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        try {
            boolean success = fileService.deleteFile(id);
            if (success) {
                return ResponseEntity.ok()
                        .body(java.util.Map.of(
                                "success", true,
                                "message", "文件删除成功"
                        ));
            } else {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of(
                                "success", false,
                                "message", "文件不存在或删除失败"
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of(
                            "success", false,
                            "message", "删除文件失败: " + e.getMessage()
                    ));
        }
    }
}