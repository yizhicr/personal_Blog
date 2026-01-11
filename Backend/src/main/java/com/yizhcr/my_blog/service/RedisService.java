package com.yizhcr.my_blog.service;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class RedisService {

    // 使用ConcurrentHashMap作为内存缓存
    private final Map<String, CachedObject> cache = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 内部类，用于存储值及其过期时间
    private static class CachedObject {
        final Object value;
        final long expirationTime;

        CachedObject(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
    }

    /**
     * 测试缓存连接是否正常
     * @return 返回"PONG"表示正常
     */
    public String ping() {
        try {
            set("ping_test", "pong", 60);
            String result = (String) get("ping_test");
            del("ping_test");
            return result != null && result.equals("pong") ? "PONG" : "ERROR";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    // ============================= Common =============================

    /**
     * 根据模式删除多个键（支持通配符）
     */
    public void deleteByPattern(String pattern) {
        try {
            // 转义特殊字符并替换*为.*
            String regex = pattern.replace(".", "\\.")
                                  .replace("*", ".*")
                                  .replace("?", ".");
            Set<String> keys = new HashSet<>();
            
            for (String key : cache.keySet()) {
                if (key.matches(regex)) {
                    keys.add(key);
                }
            }
            
            if (!CollectionUtils.isEmpty(keys)) {
                for (String key : keys) {
                    cache.remove(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置缓存过期时间
     */
    public boolean expire(String key, long time) {
        try {
            CachedObject obj = cache.get(key);
            if (obj != null && time > 0) {
                cache.put(key, new CachedObject(obj.value, System.currentTimeMillis() + time * 1000));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取过期时间
     */
    public long getExpire(String key) {
        CachedObject obj = cache.get(key);
        if (obj == null) {
            return -1;
        }
        
        long currentTime = System.currentTimeMillis();
        if (obj.expirationTime < currentTime) {
            // 已经过期，应该删除
            cache.remove(key);
            return -2;
        }
        
        return (obj.expirationTime - currentTime) / 1000;
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            CachedObject obj = cache.get(key);
            if (obj != null) {
                if (System.currentTimeMillis() > obj.expirationTime) {
                    // 已过期，删除它
                    cache.remove(key);
                    return false;
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存（支持通配符）
     */
    public void del(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }

        try {
            for (String key : keys) {
                if (key.contains("*") || key.contains("?")) {
                    // 包含通配符，使用模式匹配
                    deleteByPattern(key);
                } else {
                    cache.remove(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 普通缓存获取
     */
    public Object get(String key) {
        CachedObject obj = cache.get(key);
        if (obj == null) {
            return null;
        }

        if (System.currentTimeMillis() > obj.expirationTime) {
            // 已过期，删除它
            cache.remove(key);
            return null;
        }

        return obj.value;
    }

    /**
     * 普通缓存放入
     */
    public boolean set(String key, Object value) {
        try {
            cache.put(key, new CachedObject(value, Long.MAX_VALUE)); // Long.MAX_VALUE 表示永不过期
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                cache.put(key, new CachedObject(value, System.currentTimeMillis() + time * 1000));
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        
        lock.writeLock().lock();
        try {
            Object currentValue = get(key);
            if (currentValue == null) {
                set(key, delta);
                return delta;
            }
            
            long newValue = (Long) currentValue + delta;
            set(key, newValue);
            return newValue;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 递减
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        
        lock.writeLock().lock();
        try {
            Object currentValue = get(key);
            if (currentValue == null) {
                throw new RuntimeException("不存在的键");
            }
            
            long newValue = (Long) currentValue - delta;
            set(key, newValue);
            return newValue;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ============================= Hash =============================

    /**
     * HashGet
     */
    public Object hget(String key, String item) {
        Map<Object, Object> map = (Map<Object, Object>) get(key);
        return map != null ? map.get(item) : null;
    }

    /**
     * 获取hashKey对应的所有键值
     */
    public Map<Object, Object> hmget(String key) {
        return (Map<Object, Object>) get(key);
    }

    /**
     * HashSet
     */
    public boolean hmset(String key, Map<Object, Object> map) {
        return set(key, map);
    }

    /**
     * HashSet 并设置过期时间
     */
    public boolean hmset(String key, Map<Object, Object> map, long time) {
        return set(key, map, time);
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     */
    public boolean hset(String key, String item, Object value) {
        Map<Object, Object> map = (Map<Object, Object>) get(key);
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(item, value);
        return set(key, map);
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建并设置过期时间
     */
    public boolean hset(String key, String item, Object value, long time) {
        Map<Object, Object> map = (Map<Object, Object>) get(key);
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(item, value);
        return set(key, map, time);
    }

    /**
     * 删除hash表中的值
     */
    public void hdel(String key, Object... items) {
        Map<Object, Object> map = (Map<Object, Object>) get(key);
        if (map != null) {
            for (Object item : items) {
                map.remove(item);
            }
            // 重新设置更新后的map
            set(key, map);
        }
    }

    /**
     * 判断hash表中是否有该键的值
     */
    public boolean hHasKey(String key, String item) {
        Map<Object, Object> map = (Map<Object, Object>) get(key);
        return map != null && map.containsKey(item);
    }

    /**
     * hash递增 如果不存在,就会创建一个并把新增后的值返回
     */
    public double hincr(String key, String item, double delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        
        lock.writeLock().lock();
        try {
            Map<Object, Object> map = (Map<Object, Object>) get(key);
            if (map == null) {
                map = new HashMap<>();
            }
            
            Object currentValue = map.get(item);
            double newValue;
            if (currentValue != null) {
                // 确保值是数字类型
                newValue = ((Number) currentValue).doubleValue() + delta;
            } else {
                newValue = delta; // 如果不存在，从 delta 开始
            }
            
            map.put(item, newValue);
            set(key, map); // 更新整个 map
            return newValue;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * hash递减
     */
    public double hdecr(String key, String item, double delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return hincr(key, item, -delta); // 复用 hincr 的逻辑，传入负数
    }

    // ============================= Set =============================

    /**
     * 根据key获取Set中的所有value
     */
    public Set<Object> sGet(String key) {
        return (Set<Object>) get(key);
    }

    /**
     * 将数据放入set缓存
     */
    public boolean sSet(String key, Object... values) {
        return set(key, new HashSet<>(Arrays.asList(values)));
    }

    /**
     * 将set数据放入缓存并设置过期时间
     */
    public boolean sSet(String key, long time, Object... values) {
        boolean result = set(key, new HashSet<>(Arrays.asList(values)), time);
        return result;
    }

    /**
     * 判断set中是否有该值
     */
    public boolean sHasKey(String key, Object value) {
        Set<Object> set = (Set<Object>) get(key);
        return set != null && set.contains(value);
    }

    /**
     * 获取set缓存的长度
     */
    public long sGetSetSize(String key) {
        Set<Object> set = (Set<Object>) get(key);
        return set == null ? 0 : set.size();
    }

    /**
     * 移除值为value的set
     */
    public long setRemove(String key, Object... values) {
        Set<Object> set = (Set<Object>) get(key);
        if (set == null) {
            return 0;
        }
        
        long count = 0;
        for (Object value : values) {
            if (set.remove(value)) {
                count++;
            }
        }
        
        // 重新设置更新后的set
        set(key, set);
        return count;
    }

    // ============================= List =============================

    /**
     * 获取list缓存的内容
     */
    public List<Object> lGet(String key, long start, long end) {
        List<Object> list = (List<Object>) get(key);
        if (list == null) {
            return new ArrayList<>();
        }
        
        int size = list.size();
        if (start < 0) start = Math.max(0, size + start);
        if (end < 0) end = Math.max(0, size + end);
        if (end >= size) end = size - 1;
        
        if (start > end || start >= size) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(list.subList((int) start, (int) end + 1));
    }

    /**
     * 获取list缓存的长度
     */
    public long lGetListSize(String key) {
        List<Object> list = (List<Object>) get(key);
        return list == null ? 0 : list.size();
    }

    /**
     * 通过索引获取list中的值
     */
    public Object lGetIndex(String key, long index) {
        List<Object> list = (List<Object>) get(key);
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get((int) index);
    }

    /**
     * 将list放入缓存
     */
    public boolean lSet(String key, Object value) {
        List<Object> list = (List<Object>) get(key);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(value);
        return set(key, list);
    }

    /**
     * 将list放入缓存并设置过期时间
     */
    public boolean lSet(String key, Object value, long time) {
        List<Object> list = (List<Object>) get(key);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(value);
        return set(key, list, time);
    }

    /**
     * 将list放入缓存
     */
    public boolean lSet(String key, List<Object> value) {
        return set(key, value);
    }

    /**
     * 将list放入缓存并设置过期时间
     */
    public boolean lSet(String key, List<Object> value, long time) {
        return set(key, value, time);
    }

    /**
     * 根据索引修改list
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        List<Object> list = (List<Object>) get(key);
        if (list == null || index < 0 || index >= list.size()) {
            return false;
        }
        list.set((int) index, value);
        return set(key, list);
    }

    /**
     * 移除N个值为value的
     *
     * @param key 键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        List<Object> list = (List<Object>) get(key);
        if (list == null) {
            return 0;
        }
        
        long removeCount = 0;
        Iterator<Object> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (removeCount >= count) break;
            if (Objects.equals(iterator.next(), value)) {
                iterator.remove();
                removeCount++;
            }
        }
        
        set(key, list);
        return removeCount;
    }
}