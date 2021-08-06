package com.eseasky.modules.order.mapper;

import com.eseasky.modules.order.entity.OrderMeetingUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2021-06-21
 */
@Repository
public interface OrderMeetingUserMapper extends BaseMapper<OrderMeetingUser> {

    /**
     * 批量新增
     * @param meetingUserDataList
     */
    void batchInsert(@Param("list") List<OrderMeetingUser> meetingUserDataList);
}
