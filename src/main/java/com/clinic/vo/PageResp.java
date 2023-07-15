package com.clinic.vo;

import lombok.Builder;

import java.util.List;

// 分页查询的返回
public class PageResp<T> {
    public List<T> data;
    public Long total; // 总的元素数量
    public Integer pages; // 分页的数量
    public boolean success;
}
