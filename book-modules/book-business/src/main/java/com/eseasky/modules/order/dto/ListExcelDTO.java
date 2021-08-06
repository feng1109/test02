package com.eseasky.modules.order.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @describe:
 * @title: ListExcelDTO
 * @Author lc
 * @Date: 2021/5/25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="预约列表导出excel", description="预约列表导出excel")
public class ListExcelDTO {

    @Excel(name = "订单号")
    private String listNo;

    @Excel(name = "预约人")
    private String userName;

    @Excel(name = "人员类型")
    private String userType;

    @Excel(name = "学号/工号")
    private String userNo;

    @Excel(name = "预约地点")
    private String area;

    @Excel(name = "座位号")
    private String seatNum;

    @Excel(name = "预约时间")
    private String showTime;

    @Excel(name = "状态")
    private String listState;
}