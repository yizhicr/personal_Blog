package com.yizhcr.my_blog;

import com.yizhcr.my_blog.entity.Article;
import com.yizhcr.my_blog.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6451"
})
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    // 生成唯一键，避免测试间相互干扰
    private String generateUniqueKey(String prefix) {
        return prefix + ":" + UUID.randomUUID().toString();
    }

    @BeforeEach
    void setUp() {
        // 清理可能存在的测试数据
        redisService.deleteByPattern("test:*");
    }

    @Test
    void testPing() {
        String result = redisService.ping();
        assertEquals("PONG", result, "Redis ping should return PONG");
    }

    @Test
    void testStringOperations() {
        String key = generateUniqueKey("test:string");
        String value = "Hello Redis";

        // 测试设置字符串值
        Boolean setResult = redisService.setString(key, value, 60L);
        assertTrue(setResult, "Setting string value should succeed");

        // 测试获取字符串值
        String getResult = redisService.getString(key);
        assertEquals(value, getResult, "Retrieved value should match stored value");

        // 测试删除键
        Boolean delResult = redisService.del(key);
        assertTrue(delResult, "Deleting key should succeed");

        // 验证键已被删除
        String deletedValue = redisService.getString(key);
        assertNull(deletedValue, "Value should be null after deletion");
    }

    @Test
    void testObjectOperations() {
        String key = generateUniqueKey("test:object");
        Article article = new Article();
        article.setId(1L);
        article.setTitle("Test Article");
        article.setContent("This is a test article content");
        article.setCreatedAt(LocalDateTime.now());

        // 测试设置对象值
        Boolean setResult = redisService.set(key, article, 60L);
        assertTrue(setResult, "Setting object value should succeed");

        // 测试获取对象值
        Article getResult = redisService.get(key, Article.class);
        assertNotNull(getResult, "Retrieved article should not be null");
        assertEquals(article.getId(), getResult.getId(), "Article IDs should match");
        assertEquals(article.getTitle(), getResult.getTitle(), "Article titles should match");
        assertEquals(article.getContent(), getResult.getContent(), "Article contents should match");
    }

    @Test
    void testListOperations() {
        String key = generateUniqueKey("test:list");
        
        // 创建测试文章列表
        List<Article> articles = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Article article = new Article();
            article.setId((long) i);
            article.setTitle("Article " + i);
            article.setContent("Content of article " + i);
            articles.add(article);
        }

        // 测试设置列表
        Boolean setResult = redisService.set(key, articles, 60L);
        assertTrue(setResult, "Setting list value should succeed");

        // 测试获取列表 - 修正这里的反序列化问题
        List<Article> getResult = redisService.getList(key, Article.class);
        assertNotNull(getResult, "Retrieved list should not be null");
        assertEquals(3, getResult.size(), "List should contain 3 articles");
        
        for (int i = 0; i < 3; i++) {
            assertEquals(articles.get(i).getId(), getResult.get(i).getId(), "Article IDs should match");
            assertEquals(articles.get(i).getTitle(), getResult.get(i).getTitle(), "Article titles should match");
        }
    }

    @Test
    void testHashOperations() {
        String key = generateUniqueKey("test:hash");
        
        // 创建测试哈希数据
        Map<Object, Object> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");
        map.put("number", 123);

        // 测试设置哈希
        Boolean setResult = redisService.hmset(key, map, 60L);
        assertTrue(setResult, "Setting hash should succeed");

        // 测试获取整个哈希
        Map<Object, Object> getResult = redisService.hmget(key);
        assertNotNull(getResult, "Retrieved hash should not be null");
        assertEquals(3, getResult.size(), "Hash should contain 3 fields");
        assertEquals("value1", getResult.get("field1"), "Field1 value should match");
        assertEquals("value2", getResult.get("field2"), "Field2 value should match");
        assertEquals(123, getResult.get("number"), "Number field should match");

        // 测试获取单个字段
        Object field1Value = redisService.hget(key, "field1");
        assertEquals("value1", field1Value, "Single field value should match");

        // 测试设置单个字段
        Boolean setSingleField = redisService.hset(key, "field3", "value3", 60L);
        assertTrue(setSingleField, "Setting single field should succeed");

        Object field3Value = redisService.hget(key, "field3");
        assertEquals("value3", field3Value, "New field value should match");
    }

    @Test
    void testCounterOperations() {
        String key = generateUniqueKey("test:counter");
        
        // 清除可能存在的计数器
        redisService.del(key);
        
        // 测试递增
        long incResult = redisService.incr(key, 5);
        assertEquals(5, incResult, "Increment result should be 5");
        
        long incResult2 = redisService.incr(key, 3);
        assertEquals(8, incResult2, "Second increment result should be 8");
        
        // 测试递减
        long decResult = redisService.decr(key, 3);
        assertEquals(5, decResult, "Decrement result should be 5");
    }

    @Test
    void testExpireOperations() {
        String key = generateUniqueKey("test:expire");
        String value = "Test value";

        // 设置值
        redisService.setString(key, value, 100L);
        assertEquals(value, redisService.getString(key), "Value should be set correctly");

        // 测试获取过期时间
        Long expireTime = redisService.getExpire(key);
        assertTrue(expireTime > 0, "Expire time should be greater than 0");

        // 设置新的过期时间
        Boolean expireResult = redisService.expire(key, 120);
        assertTrue(expireResult, "Setting expire time should succeed");

        // 检查过期时间是否更新
        Long newExpireTime = redisService.getExpire(key);
        assertTrue(newExpireTime > expireTime, "New expire time should be greater than original");
    }

    @Test
    void testExistsOperation() {
        String key = generateUniqueKey("test:exists");
        String value = "Test value";

        // 检查键不存在
        assertFalse(redisService.exists(key), "Key should not exist initially");

        // 设置值
        redisService.setString(key, value, 60L);

        // 检查键存在
        assertTrue(redisService.exists(key), "Key should exist after setting value");
    }

    @Test
    void testDeleteByPattern() {
        String key1 = "pattern:test:" + UUID.randomUUID().toString() + ":1";
        String key2 = "pattern:test:" + UUID.randomUUID().toString() + ":2";
        String key3 = "pattern:other:" + UUID.randomUUID().toString() + ":1";
        
        // 设置一些测试键
        redisService.setString(key1, "value1", 60L);
        redisService.setString(key2, "value2", 60L);
        redisService.setString(key3, "value3", 60L);

        // 验证键存在
        assertTrue(redisService.exists(key1), "First test key should exist");
        assertTrue(redisService.exists(key2), "Second test key should exist");

        // 删除符合模式的键
        redisService.deleteByPattern("pattern:test:*");

        // 验证匹配模式的键已被删除，不匹配的仍然存在
        assertFalse(redisService.exists(key1), "First test key should be deleted");
        assertFalse(redisService.exists(key2), "Second test key should be deleted");
        assertTrue(redisService.exists(key3), "Other key should still exist");
    }
}