package com.yizhcr.my_blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 高级搜索请求DTO
 */
@Data
public class SearchRequest {
    private String keyword;           // 关键词搜索
    private List<Long> tagIds;       // 标签筛选
    private Long categoryId;         // 分类筛选
    private String author;           // 作者筛选
    private Integer status;          // 状态筛选
    private String startDate;        // 开始日期
    private String endDate;          // 结束日期
    private String sortBy;           // 排序字段
    private String sortOrder;        // 排序方向
    private int page = 0;            // 页码
    private int size = 10;           // 每页数量
}