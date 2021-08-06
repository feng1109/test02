package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * @describe:
 * @title: OrderListInfoDTO
 * @Author lc
 * @Date: 2021/4/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "预约订单信息")
@ComponentScan
@Alias("OrderListInfoDTO")
public class OrderListInfoDTO {

    private static final long serialVersionUID = 1L;

    private String roomId;

    private String seatId;

    private Integer listState;

    private Date startTime;

    private Date endTime;

}