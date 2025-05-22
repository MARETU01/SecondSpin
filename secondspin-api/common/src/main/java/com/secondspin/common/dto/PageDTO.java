package com.secondspin.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDTO<T> {

    private List<T> data;

    // 总记录数
    private Long total;

    // 总页数
    private Long totalPage;
}
