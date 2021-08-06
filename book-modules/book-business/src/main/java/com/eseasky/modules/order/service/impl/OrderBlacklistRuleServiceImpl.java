package com.eseasky.modules.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.fun.OrgEnvent;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.order.entity.OrderBlacklistRule;
import com.eseasky.modules.order.entity.OrderBlacklistRuleDetail;
import com.eseasky.modules.order.mapper.OrderBlacklistRuleDetailMapper;
import com.eseasky.modules.order.mapper.OrderBlacklistRuleMapper;
import com.eseasky.modules.order.service.OrderBlacklistRuleDetailService;
import com.eseasky.modules.order.service.OrderBlacklistRuleService;
import com.eseasky.modules.order.vo.request.BlacklistDetailReqVO;
import com.eseasky.modules.order.vo.request.CreateBlrReqVO;
import com.eseasky.modules.order.vo.request.EditBlrReqVO;
import com.eseasky.modules.order.vo.request.ShowBlrListReqVO;
import com.eseasky.modules.order.vo.response.ShowBlrDetailRepVO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2021-06-09
 */
@Service
@Transactional
public class OrderBlacklistRuleServiceImpl extends ServiceImpl<OrderBlacklistRuleMapper,OrderBlacklistRule> implements OrderBlacklistRuleService, OrgEnvent {

    @Autowired
    OrderBlacklistRuleMapper orderBlacklistRuleMapper;

    @Autowired
    OrderBlacklistRuleDetailMapper orderBlacklistRuleDetailMapper;

    @Autowired
    OrderBlacklistRuleDetailService orderBlacklistRuleDetailService;


    /**
     * @description: 创建黑名单规则
     * @author: lc
     * @date: 2021/6/10 9:51
     * @params
     * @return
     */
    @Override
    public R creatBlacklistRule(CreateBlrReqVO createBlrReqVO) {

        // 获取传参
        String ruleName = createBlrReqVO.getRuleName();

        // 查看该组织是否已有黑名单规则
        String orgId = createBlrReqVO.getOrgId();
        LambdaQueryWrapper<OrderBlacklistRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderBlacklistRule::getOrgId,orgId);
        int count = count(queryWrapper);
        if (count>0){
            throw BusinessException.of("操作失败，该组织已有黑名单规则");
        }

        // 获取人员信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        String username = sysUserDTO.getUsername();
        String blrId = IdUtil.simpleUUID();

        // 初始化黑名单规则
        OrderBlacklistRule orderBlacklistRule = JSONObject.parseObject(JSON.toJSONString(createBlrReqVO), OrderBlacklistRule.class);
        orderBlacklistRule.setRuleName(ruleName)
                .setUserId(userId)
                .setUserName(username)
                .setBlacklistRuleId(blrId);

        // 初始化黑名单详情
        List<BlacklistDetailReqVO> blacklistDetailList = createBlrReqVO.getBlacklistDetailList();
        List<OrderBlacklistRuleDetail> orderBlacklistRuleDetails = JSONObject.parseArray(JSON.toJSONString(blacklistDetailList), OrderBlacklistRuleDetail.class);
        ArrayList<OrderBlacklistRuleDetail> blrDetails = orderBlacklistRuleDetails.stream().map(el -> {
            el.setBlacklistRuleId(blrId);
            return el;
        }).collect(Collectors.toCollection(Lists::newArrayList));

        // 插入数据
        save(orderBlacklistRule);
        orderBlacklistRuleDetailService.saveBatch(blrDetails);

        return R.ok("编辑黑名单规则成功");

    }

    /**
     * @description: 获取已创建黑名单组织id
     * @author: lc
     * @date: 2021/6/25 11:01
     * @params
     * @return
     */
    @Override
    public R getAlreadyCreateOrgId() {

        LambdaQueryWrapper<OrderBlacklistRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(OrderBlacklistRule::getOrgId);
        List<OrderBlacklistRule> orderBlacklistRules = orderBlacklistRuleMapper.selectList(queryWrapper);
        String[] orgIds = orderBlacklistRules.stream().map(el -> el.getOrgId()).toArray(String[]::new);
        return R.ok(orgIds);
    }

    /**
     * @description: 查看黑名单详情
     * @author: lc
     * @date: 2021/6/10 13:53
     * @params
     * @return
     */
    @Override
    public R showBlrDetail(String ruleId) {

        LambdaQueryWrapper<OrderBlacklistRuleDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderBlacklistRuleDetail::getBlacklistRuleId,ruleId);
        List<OrderBlacklistRuleDetail> orderBlacklistRuleDetails = orderBlacklistRuleDetailMapper.selectList(queryWrapper);
        List<ShowBlrDetailRepVO> showBlrDetailRepVOS = JSONObject.parseArray(JSON.toJSONString(orderBlacklistRuleDetails), ShowBlrDetailRepVO.class);
        return R.ok(showBlrDetailRepVOS);

    }

    /**
     * @description:  编辑黑名单规则
     * @author: lc
     * @date: 2021/6/10 11:18
     * @params
     * @return
     */
    @Override
    public R editBlacklistRule(EditBlrReqVO editBlrReqVO) {

        // 获取人员信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        String username = sysUserDTO.getUsername();
        String blrId = editBlrReqVO.getBlacklistRuleId();

        // 更新黑名单规则
        OrderBlacklistRule orderBlacklistRule = JSONObject.parseObject(JSON.toJSONString(editBlrReqVO), OrderBlacklistRule.class);
        orderBlacklistRule.setUserId(userId)
                .setUserName(username);
        LambdaUpdateWrapper<OrderBlacklistRule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(OrderBlacklistRule::getRuleName,editBlrReqVO.getRuleName())
                .set(OrderBlacklistRule::getOrgId,editBlrReqVO.getOrgId())
                .set(OrderBlacklistRule::getOrgName,editBlrReqVO.getOrgName())
                .set(OrderBlacklistRule::getUserId,userId)
                .set(OrderBlacklistRule::getUserName,username)
                .set(OrderBlacklistRule::getUpdateTime,new Date())
                .eq(OrderBlacklistRule::getBlacklistRuleId,blrId);
        update(null,updateWrapper);


        // 更新黑名单详情
        List<OrderBlacklistRuleDetail> orderBlacklistRuleDetails = JSONObject.parseArray(JSON.toJSONString(editBlrReqVO.getBlacklistDetailList()), OrderBlacklistRuleDetail.class);
        orderBlacklistRuleDetails.stream().map(el->{
            el.setBlacklistRuleId(blrId);
            return el;
        });
        orderBlacklistRuleDetailService.updateBatchById(orderBlacklistRuleDetails);

        return R.ok("编辑成功");
    }

    /**
     * @description: 查看黑名单规则列表
     * @date: 2021/6/10 14:27
     * @params
     * @return
     */
    @Override
    public R showBlrList(ShowBlrListReqVO showBlrListReqVO) {

        // 获取用户权限范围
        Set<String> dataScope = SecurityUtils.getDataScope();

        // 获取传参
        String ruleName = showBlrListReqVO.getRuleName();
        String orgId = showBlrListReqVO.getOrgId();

        // 拼接查询条件
        Page<OrderBlacklistRule> page = new Page<>(showBlrListReqVO.getPageNum(), showBlrListReqVO.getPageSize());
        LambdaQueryWrapper<OrderBlacklistRule> queryWrapper = new LambdaQueryWrapper<>();

        if(CollectionUtil.isNotEmpty(dataScope)){
            queryWrapper.in(OrderBlacklistRule::getOrgId,dataScope);
        }
        if (StrUtil.isNotEmpty(ruleName)){
            queryWrapper.like(OrderBlacklistRule::getRuleName,ruleName);
        }
        if (StrUtil.isNotEmpty(orgId)){
            queryWrapper.eq(OrderBlacklistRule::getOrgId,orgId);
        }

        queryWrapper.orderByDesc(OrderBlacklistRule::getCreateTime);

        Page<OrderBlacklistRule> orderBlacklistRulePage = orderBlacklistRuleMapper.selectPage(page, queryWrapper);
        return R.ok(orderBlacklistRulePage);
    }

    /**
     * @description: 批量删除
     * @author: lc
     * @date: 2021/6/10 14:42
     * @params
     * @return
     */
    @Override
    public R delBlacklistRule(String[] ruleIds) {

        // 删除黑名单规则
        ArrayList<String> ids = Lists.newArrayList(ruleIds);
        orderBlacklistRuleMapper.deleteBatchIds(ids);

        // 删除黑名单详情
        LambdaUpdateWrapper<OrderBlacklistRuleDetail> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(OrderBlacklistRuleDetail::getBlacklistRuleId, ids);
        orderBlacklistRuleDetailMapper.delete(wrapper);

        return R.ok("黑名单规则已删除");
    }

    /**
     * 删除部门的时候，级联删除黑名单的所属部门
     */
    @Override
    public void deleteOrgCascade(List<String> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return;
        }
        lambdaUpdate().set(OrderBlacklistRule::getOrgId, null).set(OrderBlacklistRule::getOrgName, null).in(OrderBlacklistRule::getOrgId, orgIds).update();
    }


}
