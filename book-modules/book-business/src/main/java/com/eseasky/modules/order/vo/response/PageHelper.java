package com.eseasky.modules.order.vo.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageHelper implements Serializable {

    /**
     * 页数
     */
    private Integer pageNum = 1;

    /**
     * 条数
     */
    private Integer pageSize = 10;

    /**
     * 开始
     */
    private Integer start;

    /**
     * 结束
     */
    private Integer end;

    public Integer getStart() {
        return (pageNum - 1) * pageSize;
    }

    public Integer getEnd() {
        return this.pageSize;
    }
}
