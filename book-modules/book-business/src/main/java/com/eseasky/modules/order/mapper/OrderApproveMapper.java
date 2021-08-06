package com.eseasky.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.modules.order.entity.OrderApprove;
import com.eseasky.modules.order.vo.request.ShowApproveListReqVO;
import com.eseasky.modules.order.vo.response.ShowApproveListRepVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 审批 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2021-06-11
 */
@Repository
public interface OrderApproveMapper extends BaseMapper<OrderApprove> {

    List<ShowApproveListRepVO> getApproveList(Page page,@Param("userId") String userId ,@Param("vo")ShowApproveListReqVO showApproveListReqVO);


}
