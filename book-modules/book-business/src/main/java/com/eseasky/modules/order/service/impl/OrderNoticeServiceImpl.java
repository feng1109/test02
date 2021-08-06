package com.eseasky.modules.order.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.CommonUtil;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.OrderNoticeDTO;
import com.eseasky.modules.order.dto.SendNoticeDTO;
import com.eseasky.modules.order.entity.OrderNotice;
import com.eseasky.modules.order.mapper.OrderNoticeMapper;
import com.eseasky.modules.order.service.OrderNoticeService;
import com.eseasky.modules.order.utils.OrderNoticeUtils;
import com.eseasky.modules.order.vo.request.OrderNoticeReqVO;
import com.eseasky.modules.order.vo.response.ListWithPage;
import com.eseasky.modules.order.vo.response.OrderNoticeRepVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * @since 2021-05-14
 */
@Service
@Slf4j
public class OrderNoticeServiceImpl extends ServiceImpl<OrderNoticeMapper, OrderNotice> implements OrderNoticeService {

    @Autowired
    OrderNoticeMapper orderNoticeMapper;

    /**
     * @description: 推送信息
     * @author: lc
     * @date: 2021/5/17 9:33
     * @params [sendNoticeDTO]
     * @return void
     */
    @Override
    public void sendNotice(SendNoticeDTO sendNoticeDTO) {
        OrderNotice orderNotice = JSONObject.parseObject(JSON.toJSONString(sendNoticeDTO), OrderNotice.class);
        orderNotice.setIsRead(OrderConstant.Switch.NO).setDelFlag("1");
        Integer insert = orderNoticeMapper.insert(orderNotice);
        if (insert.equals(1)){
            log.info("推动成功");
        }else {
            log.info("推送失败");
        }
    }

    /**
     * @description: 展示消息通知列表
     * @author: lc
     * @date: 2021/5/17 10:05
     * @params [userId]
     * @return void
     */
    @Override
    public R<ListWithPage<OrderNoticeRepVO>> showNoticeList(OrderNoticeReqVO orderNoticeReqVO) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();

        // 获取通知信息
        LambdaQueryWrapper<OrderNotice> wrapper = new LambdaQueryWrapper<>();
        // 获取通知信息
        wrapper.eq(OrderNotice::getUserId,userId)
                .eq(OrderNotice::getNoticeType,OrderConstant.NoticeType.App)
                .orderByDesc(OrderNotice::getIsRead).orderByDesc(OrderNotice::getSendTime);

        Page<OrderNotice> page = new Page<>(orderNoticeReqVO.getPageNum(), orderNoticeReqVO.getPageSize());
        Page<OrderNotice> orderNoticePage = orderNoticeMapper.selectPage(page, wrapper);

        ListWithPage<OrderNoticeRepVO> listWithPage = new ListWithPage<>();
        listWithPage.setPages(Integer.valueOf(String.valueOf(orderNoticePage.getPages())));
        List<OrderNoticeRepVO> orderNoticeRepVOList = OrderNoticeUtils.getOrderNoticeRepVOListByOrderNoticeList(orderNoticePage.getRecords());
        listWithPage.setDataList(orderNoticeRepVOList);
        listWithPage.setTotal(Integer.valueOf(String.valueOf(orderNoticePage.getTotal())));
        return R.ok(listWithPage);

    }

    /**
     * @description: 读取通知详情
     * @author: lc
     * @date: 2021/5/17 10:29
     * @params [orderNoticeId]
     * @return com.eseasky.common.code.utils.R
     */
    @Override
    public R readNotice(String orderNoticeId) {
        LambdaUpdateWrapper<OrderNotice> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(OrderNotice::getIsRead, OrderConstant.Switch.YES)
                .set(OrderNotice::getUpdateTime,new Date())
                .eq(OrderNotice::getOrderNoticeId,orderNoticeId);
        orderNoticeMapper.update(null,wrapper);

        return R.ok("读取通知详情成功");
    }

    /**
     * @description: 删除通知
     * @author: lc
     * @date: 2021/5/17 11:21
     * @params [orderNoticeId]
     * @return com.eseasky.common.code.utils.R
     */
    @Override
    public R delNotice(String orderNoticeId) {
        orderNoticeMapper.deleteById(orderNoticeId);
        return R.ok("删除成功");
    }

    @Override
    public R<Integer> getNoticeCount(Integer isRead) {
        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        LambdaUpdateWrapper<OrderNotice> wrapper = new LambdaUpdateWrapper<>();
        if (ObjectUtil.isNotNull(isRead)){
            wrapper.eq(OrderNotice::getIsRead,isRead);
        }
        wrapper.eq(OrderNotice::getUserId,userId);
        Integer count = orderNoticeMapper.selectCount(wrapper);

        return R.ok(count);
    }

}


