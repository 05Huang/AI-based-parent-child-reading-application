package com.qz.sns.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDTO<T> {
    private List<T> records;
    private Long total;
    private Integer current;
    private Integer size;
}