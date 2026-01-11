package com.yizhcr.my_blog.service;

import com.yizhcr.my_blog.entity.User;
import com.yizhcr.my_blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // 注册用户
    public User register(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在（如果提供了邮箱）
        if (StringUtils.hasText(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 加密密码 - 这里使用注入的passwordEncoder实例
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
        // 设置默认昵称（如果没有提供）
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(user.getUsername());
        }
        
        return userRepository.save(user);
    }
    
    // 验证用户密码
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    // 更新用户密码
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    // 根据ID查找用户
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    // 根据用户名查找用户
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // 更新用户信息
    public User update(User user) {
        // 注意：这个方法不更新密码
        return userRepository.save(user);
    }
    
    // 检查用户名是否存在
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    // 检查邮箱是否存在
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}