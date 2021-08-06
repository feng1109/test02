package com.eseasky.modules.order.mapper;

import com.eseasky.modules.order.entity.OrderBlacklist;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eseasky.modules.order.vo.request.BlacklistDetailReqVO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2021-06-09
 */
@Repository
public interface OrderBlacklistMapper extends BaseMapper<OrderBlacklist> {


    /** 根据orgId查询黑名单规则*/
    List<BlacklistDetailReqVO> getRule(String orgId);

}
