package com.eseasky.modules.order.mapper;

import com.eseasky.modules.order.dto.OrderGroupListDetailedDTO;
import com.eseasky.modules.order.entity.OrderGroupList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eseasky.modules.order.vo.response.GroupInviteRepVO;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Repository
public interface OrderGroupListMapper extends BaseMapper<OrderGroupList> {

    Integer getSeatGroupRepeat(OrderGroupList orderGroupList);

    GroupInviteRepVO getInviteGroupInfo(String orderGroupId);

    /**
     * 获取拼团详细信息
     * @param orderGroupId
     */
    OrderGroupListDetailedDTO getOrderGroupListDetailed(String orderGroupId);
}
