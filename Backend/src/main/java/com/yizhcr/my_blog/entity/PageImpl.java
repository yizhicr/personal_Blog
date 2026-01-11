package com.yizhcr.my_blog.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;

/**
 * 简单的分页实现
 */
public class PageImpl<T> implements Page<T> {
    private final List<T> content;
    private final Pageable pageable;
    private final long total;

    public PageImpl(List<T> content, Pageable pageable, long total) {
        this.content = content;
        this.pageable = pageable;
        this.total = total;
    }

    @Override
    public int getTotalPages() {
        return total == 0 ? 1 : (int) Math.ceil((double) total / pageable.getPageSize());
    }

    @Override
    public long getTotalElements() {
        return total;
    }

    @Override
    public <S> Page<S> map(java.util.function.Function<? super T, ? extends S> converter) {
        return null;
    }

    @Override
    public int getNumber() {
        return pageable.getPageNumber();
    }

    @Override
    public int getSize() {
        return pageable.getPageSize();
    }

    @Override
    public int getNumberOfElements() {
        return content.size();
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @Override
    public Sort getSort() {
        return pageable.getSort();
    }

    @Override
    public boolean isFirst() {
        return !hasPrevious();
    }

    @Override
    public boolean isLast() {
        return getNumber() + 1 >= getTotalPages();
    }

    @Override
    public boolean hasNext() {
        return getNumber() + 1 < getTotalPages();
    }

    @Override
    public boolean hasPrevious() {
        return getNumber() > 0;
    }

    @Override
    public Pageable nextPageable() {
        return hasNext() ? PageRequest.of(getNumber() + 1, getSize(), getSort()) : null;
    }

    @Override
    public Pageable previousPageable() {
        return hasPrevious() ? PageRequest.of(getNumber() - 1, getSize(), getSort()) : null;
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }

    @Override
    public Pageable getPageable() {
        return pageable;
    }
}