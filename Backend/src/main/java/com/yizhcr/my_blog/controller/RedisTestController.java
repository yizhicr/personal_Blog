package com.yizhcr.my_blog.controller;

import com.yizhcr.my_blog.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class RedisTestController {

    @Autowired
    private RedisService redisService;

    @GetMapping("/redis")
    public Map<String, Object> testCache() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 测试设置值
            redisService.set("testKey", "Hello Memory Cache");
            // 获取值
            String value = (String) redisService.get("testKey");
            response.put("success", true);
            response.put("message", "内存缓存连接成功");
            response.put("value", value);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "内存缓存连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
    
    @GetMapping("/redis/detailed")
    public Map<String, Object> detailedCacheTest() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 测试各种缓存操作
            String testKey = "health_check_" + System.currentTimeMillis();
            String testValue = "test_value";
            
            // 1. 测试写入
            boolean setResult = redisService.set(testKey, testValue, 60); // 60秒过期
            if (!setResult) {
                response.put("success", false);
                response.put("message", "缓存 SET操作失败");
                return response;
            }
            
            // 2. 测试读取
            Object getValue = redisService.get(testKey);
            if (getValue == null || !getValue.equals(testValue)) {
                response.put("success", false);
                response.put("message", "缓存 GET操作失败或值不匹配");
                return response;
            }
            
            // 3. 测试过期
            long ttl = redisService.getExpire(testKey);
            if (ttl <= 0) {
                response.put("success", false);
                response.put("message", "缓存 TTL操作失败");
                return response;
            }
            
            // 4. 测试删除
            redisService.del(testKey);
            Object valueAfterDel = redisService.get(testKey);
            if (valueAfterDel != null) {
                response.put("success", false);
                response.put("message", "缓存 DEL操作失败");
                return response;
            }
            
            response.put("success", true);
            response.put("message", "所有缓存操作测试通过");
            response.put("details", Map.of(
                "set_operation", "success",
                "get_operation", "success", 
                "ttl_operation", "success (" + ttl + "s)",
                "del_operation", "success"
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "缓存详细测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
    
    @GetMapping("/redis/connection-info")
    public Map<String, Object> getConnectionInfo() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 使用RedisService的ping方法
            String pingResult = redisService.ping();
            response.put("success", true);
            response.put("message", "Redis连接信息获取成功");
            response.put("ping_result", pingResult);
            response.put("host", "127.0.0.1");
            response.put("port", 6451);
            response.put("status", "configured");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "无法获取Redis连接信息: " + e.getMessage());
            response.put("error_details", e.getClass().getName() + ": " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/redis/health")
    public Map<String, Object> cacheHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        try {
            String pingResult = redisService.ping();
            response.put("status", "UP");
            response.put("message", "内存缓存正常");
            response.put("ping_result", pingResult);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "内存缓存异常: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            response.put("error_message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/redis/debug")
    public Map<String, Object> cacheDebugInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String pingResult = redisService.ping();
            response.put("cache_service", RedisService.class.getSimpleName());
            response.put("ping_result", pingResult);
            response.put("status", "UP");
            response.put("message", "内存缓存调试信息获取成功");
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "内存缓存调试失败: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            response.put("error_message", e.getMessage());
            
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0) {
                response.put("first_stack_element", stackTrace[0].toString());
            }
        }
        
        return response;
    }
}