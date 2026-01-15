package com.yizhcr.my_blog.service;

import com.yizhcr.my_blog.entity.Tag;
import com.yizhcr.my_blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagService {
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private RedisService redisService;
    
    private static final String TAG_CACHE_KEY = "tag:";
    private static final String TAGS_ALL_KEY = "tags:all";
    private static final String HOT_TAGS_KEY = "tags:hot:";
    private static final long TAG_CACHE_EXPIRE = 24 * 60 * 60;
    
    public List<Tag> getAllTags() {
        List<Tag> tags = redisService.getList(TAGS_ALL_KEY, Tag.class);
        if (tags != null) {
            return tags;
        }
        
        tags = tagRepository.findAll();
        redisService.set(TAGS_ALL_KEY, tags, TAG_CACHE_EXPIRE);
        return tags;
    }
    
    public List<Tag> getHotTags(int limit) {
        String cacheKey = HOT_TAGS_KEY + limit;
        List<Tag> tags = redisService.getList(cacheKey, Tag.class);
        if (tags != null) {
            return tags;
        }
        
        tags = tagRepository.findTop10ByOrderByArticleCountDesc();
        if (limit < tags.size()) {
            tags = tags.subList(0, limit);
        }
        
        redisService.set(cacheKey, tags, TAG_CACHE_EXPIRE);
        return tags;
    }
    
    @Transactional
    public Set<Tag> createOrGetTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> uniqueNames = new HashSet<>(tagNames);
        List<Tag> existingTags = tagRepository.findByNameIn(new ArrayList<>(uniqueNames));
        Set<Tag> resultTags = new HashSet<>(existingTags);
        
        Set<String> existingNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
        
        for (String name : uniqueNames) {
            if (!existingNames.contains(name)) {
                Tag newTag = new Tag();
                newTag.setName(name);
                newTag.setSlug(generateSlug(name));
                newTag.setColor(generateRandomColor());
                
                Tag savedTag = tagRepository.save(newTag);
                resultTags.add(savedTag);
            }
        }
        
        clearTagCache();
        return resultTags;
    }
    
    @Transactional
    public void incrementArticleCount(Set<Tag> tags) {
        if (tags != null && !tags.isEmpty()) {
            Set<Long> tagIds = tags.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            tagRepository.incrementArticleCount(tagIds);
            clearTagCache();
        }
    }
    
    @Transactional
    public void decrementArticleCount(Set<Tag> tags) {
        if (tags != null && !tags.isEmpty()) {
            Set<Long> tagIds = tags.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            tagRepository.decrementArticleCount(tagIds);
            clearTagCache();
        }
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-]", "")
                .replaceAll("\\-+", "-")
                .trim();
    }
    
    private String generateRandomColor() {
        Random random = new Random();
        int r = 50 + random.nextInt(150);
        int g = 50 + random.nextInt(150);
        int b = 50 + random.nextInt(150);
        return String.format("#%02x%02x%02x", r, g, b);
    }
    
    private void clearTagCache() {
        redisService.del(TAGS_ALL_KEY);
        redisService.del(TAG_CACHE_KEY + "*");
        redisService.del(HOT_TAGS_KEY + "*");
    }
}