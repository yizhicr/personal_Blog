package com.yizhcr.my_blog.controller;

import com.yizhcr.my_blog.entity.User;
import com.yizhcr.my_blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            
            // 不返回密码
            registeredUser.setPassword(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", registeredUser);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 获取用户信息
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            // 不返回密码
            user.get().setPassword(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", user.get());
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户不存在");
            
            return ResponseEntity.notFound().build();
        }
    }

    // 获取当前登录用户信息
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            
            // 从用户名查找用户信息
            Optional<User> user = userService.findByUsername(userDetails.getUsername());
            if (user.isPresent()) {
                User currentUser = user.get();
                // 不返回密码
                currentUser.setPassword(null);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", currentUser);
                
                return ResponseEntity.ok(response);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "未找到当前用户信息");
        return ResponseEntity.badRequest().body(response);
    }
    
    // 更新用户信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> existingUser = userService.findById(id);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            // 只更新允许更新的字段
            if (userDetails.getNickname() != null) {
                user.setNickname(userDetails.getNickname());
            }
            
            if (userDetails.getEmail() != null) {
                user.setEmail(userDetails.getEmail());
            }
            
            if (userDetails.getAvatar() != null) {
                user.setAvatar(userDetails.getAvatar());
            }
            
            User updatedUser = userService.update(user);
            updatedUser.setPassword(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", updatedUser);
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户不存在");
            
            return ResponseEntity.notFound().build();
        }
    }
}