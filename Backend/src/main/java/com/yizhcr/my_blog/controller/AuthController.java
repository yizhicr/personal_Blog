package com.yizhcr.my_blog.controller;

import com.yizhcr.my_blog.config.jwt.JwtTokenUtil;
import com.yizhcr.my_blog.dto.LoginRequest;
import com.yizhcr.my_blog.dto.LoginResponse;
import com.yizhcr.my_blog.dto.RegisterRequest;
import com.yizhcr.my_blog.entity.User;
import com.yizhcr.my_blog.service.CustomUserDetailsService;
import com.yizhcr.my_blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    // ================ 注册接口 ================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 创建用户实体
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setEmail(registerRequest.getEmail());
            user.setNickname(registerRequest.getNickname());
            
            // 调用注册服务
            User registeredUser = userService.register(user);
            
            // 清除密码信息
            registeredUser.setPassword(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", registeredUser);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            // 捕获用户名已存在等异常
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "注册失败: " + e.getMessage()
            ));
        }
    }
    
    // ================ 登录接口 ================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. 认证用户（Spring Security 会调用我们的 CustomUserDetailsService）
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 2. 生成 JWT Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(userDetails);
            
            // 3. 获取用户详细信息
            Optional<User> user = userService.findByUsername(userDetails.getUsername());
            
            if (user.isPresent()) {
                // 直接创建 LoginResponse 实例并设置属性
                LoginResponse response = new LoginResponse();
                response.setToken(token);
                response.setId(user.get().getId());
                response.setUsername(user.get().getUsername());
                response.setEmail(user.get().getEmail());
                response.setNickname(user.get().getNickname());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "登录成功");
                result.put("data", response);
                
                return ResponseEntity.ok(result);
            }
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "用户不存在"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "用户名或密码错误: " + e.getMessage()
            ));
        }
    }
    
    // ================ 获取当前用户 ================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> user = userService.findByUsername(username);
            
            if (user.isPresent()) {
                // 不返回密码
                user.get().setPassword(null);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", user.get());
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.status(404).body(Map.of(
                "success", false, 
                "message", "用户不存在"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取用户信息失败: " + e.getMessage()
            ));
        }
    }
    
    // ================ 注销接口 ================
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 由于我们使用无状态的JWT，客户端只需丢弃token即可
        // 在服务端，我们可以将token加入黑名单（需要Redis支持，后面再实现）
        SecurityContextHolder.clearContext();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "注销成功");
        
        return ResponseEntity.ok(response);
    }
    
    // ================ 验证JWT令牌接口 ================
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        try {
            // 从请求头中获取JWT令牌
            String token = jwtTokenUtil.getTokenFromRequest();
            
            if (token != null && jwtTokenUtil.validateToken(token)) {
                // 令牌有效，返回用户信息
                String username = jwtTokenUtil.getUsernameFromToken(token);
                
                Optional<User> userOptional = userService.findByUsername(username);
                
                if (userOptional.isPresent()) {
                    // 不返回密码
                    User user = userOptional.get();
                    user.setPassword(null);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", user);
                    
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(404).body(Map.of(
                        "success", false, 
                        "message", "用户不存在"
                    ));
                }
            } else {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "令牌无效或已过期"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "验证令牌失败: " + e.getMessage()
            ));
        }
    }
}