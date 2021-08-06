package com.eseasky.modules.order.vo.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListWithPage<T> {
    /**
     * 总数
     */
    private Integer total = 0;

    /**
     * 数据
     */
    private List<T> dataList = new ArrayList<>();

    /**
     * 页数
     */
    private Integer pages;
}
