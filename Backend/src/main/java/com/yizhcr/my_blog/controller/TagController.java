package com.yizhcr.my_blog.controller;

import com.yizhcr.my_blog.entity.Tag;
import com.yizhcr.my_blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    @GetMapping
    public ResponseEntity<?> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tags);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/hot")
    public ResponseEntity<?> getHotTags(@RequestParam(defaultValue = "10") int limit) {
        List<Tag> tags = tagService.getHotTags(limit);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tags);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createTag(@RequestBody Tag tag) {
        try {
            List<String> tagNames = List.of(tag.getName());
            var tags = tagService.createOrGetTags(tagNames);
            Tag createdTag = tags.iterator().next();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "标签创建成功");
            response.put("data", createdTag);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}