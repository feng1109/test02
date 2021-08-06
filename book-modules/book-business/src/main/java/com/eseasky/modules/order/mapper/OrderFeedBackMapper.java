package com.eseasky.modules.order.mapper;

import com.eseasky.modules.order.entity.OrderFeedBack;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户反馈 Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-07-16
 */
public interface OrderFeedBackMapper extends BaseMapper<OrderFeedBack> {

    List<OrderFeedBack> getOrderFeedBackByIds(@Param("ids") List<String> ids);
}
