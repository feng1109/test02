package com.eseasky.modules.order.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.modules.order.entity.OrderGroupDetail;
import com.eseasky.modules.order.vo.request.UserListReqVO;
import com.eseasky.modules.order.vo.response.UserGroupRentListVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2021-06-08
 */
@Repository
public interface OrderGroupDetailMapper extends BaseMapper<OrderGroupDetail> {


    List<UserGroupRentListVO> getUserGroupRent(Page page,@Param("vo") UserListReqVO userListReqVO);
}
