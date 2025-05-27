package com.secondspin.common.dto;

import lombok.Data;

@Data
public class QueryDTO {

    // 当前页码
    private Long pageNo;

    // 每页大小
    private Long pageSize;

    // 排序字段
    private String sortBy;

    // 是否升序
    private Boolean isAsc;

    // 筛选
    private String filter;
}
