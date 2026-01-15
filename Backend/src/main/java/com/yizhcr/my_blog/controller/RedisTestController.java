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
            redisService.set("testKey", "Hello Redis!");
            // 获取值
            String value = (String) redisService.get("testKey", String.class);
            response.put("success", true);
            response.put("message", "Redis连接成功");
            response.put("value", value);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Redis连接失败: " + e.getMessage());
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
            boolean setResult = redisService.set(testKey, testValue, 60L); // 60秒过期
            if (!setResult) {
                response.put("success", false);
                response.put("message", "缓存 SET操作失败");
                return response;
            }
            
            // 2. 测试读取
            Object getValue = redisService.get(testKey, String.class);
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
            Object valueAfterDel = redisService.get(testKey, String.class);
            if (valueAfterDel != null) {
                response.put("success", false);
                response.put("message", "缓存 DEL操作失败");
                return response;
            }
            
            response.put("success", true);
            response.put("message", "所有Redis操作测试通过");
            response.put("details", Map.of(
                "set_operation", "success",
                "get_operation", "success", 
                "ttl_operation", "success (" + ttl + "s)",
                "del_operation", "success"
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Redis详细测试失败: " + e.getMessage());
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
            response.put("host", "localhost");
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
            response.put("message", "Redis正常");
            response.put("ping_result", pingResult);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Redis异常: " + e.getMessage());
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
            response.put("message", "Redis调试信息获取成功");
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Redis调试失败: " + e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            response.put("error_message", e.getMessage());
            
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0) {
                response.put("first_stack_element", stackTrace[0].toString());
            }
        }
        
        return response;
    }
    
    @GetMapping("/redis/cache-performance")
    public Map<String, Object> cachePerformanceTest() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 性能测试：批量写入和读取
            int testSize = 1000;
            long startTime = System.currentTimeMillis();
            
            // 批量写入
            for (int i = 0; i < testSize; i++) {
                redisService.set("perf_test_key_" + i, "value_" + i, 300L);
            }
            long writeEndTime = System.currentTimeMillis();
            
            // 批量读取
            int hits = 0;
            for (int i = 0; i < testSize; i++) {
                String value = redisService.get("perf_test_key_" + i, String.class);
                if (value != null) {
                    hits++;
                }
            }
            long readEndTime = System.currentTimeMillis();
            
            response.put("success", true);
            response.put("message", "缓存性能测试完成");
            response.put("write_time_ms", writeEndTime - startTime);
            response.put("read_time_ms", readEndTime - writeEndTime);
            response.put("total_items", testSize);
            response.put("hits", hits);
            response.put("hit_rate", String.format("%.2f%%", (double) hits / testSize * 100));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "缓存性能测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
}