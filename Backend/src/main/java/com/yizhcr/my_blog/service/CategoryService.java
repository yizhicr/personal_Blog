package com.yizhcr.my_blog.service;

import com.yizhcr.my_blog.entity.Category;
import com.yizhcr.my_blog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private RedisService redisService;
    
    private static final String CATEGORY_CACHE_KEY = "category:";
    private static final String CATEGORIES_ALL_KEY = "categories:all";
    private static final long CATEGORY_CACHE_EXPIRE = 24 * 60 * 60; // 24小时
    
    public List<Category> getAllCategories() {
        List<Category> categories = redisService.getList(CATEGORIES_ALL_KEY, Category.class);
        if (categories != null) {
            return categories;
        }
        
        categories = categoryRepository.findAllByOrderByOrderNumAsc();
        redisService.set(CATEGORIES_ALL_KEY, categories, CATEGORY_CACHE_EXPIRE);
        return categories;
    }
    
    public Optional<Category> getCategoryById(Long id) {
        String cacheKey = CATEGORY_CACHE_KEY + id;
        Category category = redisService.get(cacheKey, Category.class);
        if (category != null) {
            return Optional.of(category);
        }
        
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        categoryOpt.ifPresent(c -> redisService.set(cacheKey, c, CATEGORY_CACHE_EXPIRE));
        return categoryOpt;
    }
    
    @Transactional
    public Category createCategory(Category category) {
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }
        
        Category savedCategory = categoryRepository.save(category);
        clearCategoryCache();
        return savedCategory;
    }
    
    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        
        if (categoryDetails.getName() != null) {
            category.setName(categoryDetails.getName());
        }
        if (categoryDetails.getSlug() != null) {
            category.setSlug(categoryDetails.getSlug());
        }
        if (categoryDetails.getDescription() != null) {
            category.setDescription(categoryDetails.getDescription());
        }
        if (categoryDetails.getOrderNum() != null) {
            category.setOrderNum(categoryDetails.getOrderNum());
        }
        
        Category updatedCategory = categoryRepository.save(category);
        clearCategoryCache();
        return updatedCategory;
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        
        if (category.getArticleCount() > 0) {
            throw new RuntimeException("该分类下还有文章，无法删除");
        }
        
        categoryRepository.deleteById(id);
        clearCategoryCache();
    }
    
    @Transactional
    public void incrementArticleCount(Long categoryId) {
        categoryRepository.incrementArticleCount(categoryId);
        clearCategoryCache();
    }
    
    @Transactional
    public void decrementArticleCount(Long categoryId) {
        categoryRepository.decrementArticleCount(categoryId);
        clearCategoryCache();
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-]", "")
                .replaceAll("\\-+", "-")
                .trim();
    }
    
    private void clearCategoryCache() {
        redisService.del(CATEGORIES_ALL_KEY);
        redisService.del(CATEGORY_CACHE_KEY + "*");
    }
}