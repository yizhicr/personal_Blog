package com.yizhcr.my_blog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // ============================= Common =============================

    /**
     * 根据模式删除多个键（支持通配符）
     */
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入缓存
     */
    public Boolean set(String key, Object value, Long timeOut) {
        try {
            redisTemplate.opsForValue().set(key, value, timeOut, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 写入缓存（使用默认超时）
     */
    public Boolean set(String key, Object value) {
        return set(key, value, 300L); // 默认5分钟过期
    }

    /**
     * 读取缓存
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj != null) {
                // 如果对象已经是目标类型，直接返回
                if (clazz.isInstance(obj)) {
                    return clazz.cast(obj);
                }
                // 否则尝试序列化转换
                String json = objectMapper.writeValueAsString(obj);
                return objectMapper.readValue(json, clazz);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取缓存（泛型列表）
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj != null) {
                // 如果对象已经是列表类型，尝试转换元素
                if (obj instanceof List) {
                    List<?> rawList = (List<?>) obj;
                    if (!rawList.isEmpty()) {
                        // 如果第一个元素已经是目标类型，直接返回
                        if (clazz.isInstance(rawList.get(0))) {
                            return (List<T>) rawList;
                        } else {
                            // 如果是Map类型的元素，则需要手动转换
                            List<T> convertedList = new ArrayList<>();
                            for (Object item : rawList) {
                                if (item instanceof Map) {
                                    // 将Map转换为目标类型
                                    String json = objectMapper.writeValueAsString(item);
                                    T convertedItem = objectMapper.readValue(json, clazz);
                                    convertedList.add(convertedItem);
                                } else {
                                    // 其他类型尝试直接转换
                                    String json = objectMapper.writeValueAsString(item);
                                    T convertedItem = objectMapper.readValue(json, clazz);
                                    convertedList.add(convertedItem);
                                }
                            }
                            return convertedList;
                        }
                    }
                }
                
                // 否则通过JSON序列化转换
                String json = objectMapper.writeValueAsString(obj);
                TypeReference<List<T>> typeRef = new TypeReference<List<T>>() {};
                return objectMapper.readValue(json, typeRef);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 删除key
     */
    public Boolean del(String key) {
        try {
            redisTemplate.delete(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }

    /**
     * Ping Redis服务器
     */
    public String ping() {
        try {
            stringRedisTemplate.opsForValue().set("ping_key", "PONG", 60, TimeUnit.SECONDS);
            return stringRedisTemplate.opsForValue().get("ping_key");
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * 检查key是否存在
     */
    public Boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout) {
        try {
            return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取字符串值
     */
    public String getString(String key) {
        try {
            return (String) stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置字符串值
     */
    public Boolean setString(String key, String value, Long timeOut) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, timeOut, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断成员是否存在于集合中
     */
    public Boolean isMember(String key, Object value) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(key, value);
            return isMember != null ? isMember : false;
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
        if (redisTemplate == null) {
            return 0;
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        if (redisTemplate == null) {
            return 0;
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ============================= Hash =============================

    /**
     * HashGet
     */
    public Object hget(String key, String item) {
        if (redisTemplate == null) {
            return null;
        }
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     */
    public Map<Object, Object> hmget(String key) {
        if (redisTemplate == null) {
            return new HashMap<>();
        }
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     */
    public boolean hmset(String key, Map<Object, Object> map) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置过期时间
     */
    public boolean hmset(String key, Map<Object, Object> map, long time) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     */
    public boolean hset(String key, String item, Object value) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建并设置过期时间
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     */
    public void hdel(String key, Object... items) {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.opsForHash().delete(key, items);
    }

    /**
     * 判断hash表中是否有该键的值
     */
    public boolean hHasKey(String key, String item) {
        if (redisTemplate == null) {
            return false;
        }
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个并把新增后的值返回
     */
    public double hincr(String key, String item, double delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        if (redisTemplate == null) {
            return 0;
        }
        return redisTemplate.opsForHash().increment(key, item, delta);
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
        try {
            if (redisTemplate == null) {
                return new HashSet<>();
            }
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将数据放入set缓存
     */
    public boolean sSet(String key, Object... values) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForSet().add(key, values);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将set数据放入缓存并设置过期时间
     */
    public boolean sSet(String key, long time, Object... values) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断set中是否有该值
     */
    public boolean sHasKey(String key, Object value) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            Boolean hasKey = redisTemplate.opsForSet().isMember(key, value);
            return hasKey != null && hasKey;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取set缓存的长度
     */
    public long sGetSetSize(String key) {
        try {
            if (redisTemplate == null) {
                return 0;
            }
            Long size = redisTemplate.opsForSet().size(key);
            return size == null ? 0 : size;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的set
     */
    public long setRemove(String key, Object... values) {
        try {
            if (redisTemplate == null) {
                return 0;
            }
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count == null ? 0 : count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ============================= List =============================

    /**
     * 获取list缓存的内容
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            if (redisTemplate == null) {
                return new ArrayList<>();
            }
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     */
    public long lGetListSize(String key) {
        try {
            if (redisTemplate == null) {
                return 0;
            }
            Long size = redisTemplate.opsForList().size(key);
            return size == null ? 0 : size;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引获取list中的值
     */
    public Object lGetIndex(String key, long index) {
        try {
            if (redisTemplate == null) {
                return null;
            }
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将list放入缓存
     */
    public boolean lSet(String key, Object value) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存并设置过期时间
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存并设置过期时间
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            if (redisTemplate == null) {
                return false;
            }
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        try {
            if (redisTemplate == null) {
                return 0;
            }
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove == null ? 0 : remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}