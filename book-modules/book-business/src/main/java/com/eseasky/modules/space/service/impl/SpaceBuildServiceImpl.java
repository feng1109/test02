package com.eseasky.modules.space.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.fun.OrgEnvent;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.common.entity.SysOrg;
import com.eseasky.common.service.SysOrgService;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.service.OrderCommentService;
import com.eseasky.modules.order.service.OrderMeetingService;
import com.eseasky.modules.order.service.OrderSeatListService;
import com.eseasky.modules.space.config.SpaceConstant;
import com.eseasky.modules.space.config.SpaceUtil;
import com.eseasky.modules.space.config.UploadProp;
import com.eseasky.modules.space.entity.SpaceBuild;
import com.eseasky.modules.space.entity.SpaceConf;
import com.eseasky.modules.space.entity.SpaceDesk;
import com.eseasky.modules.space.entity.SpaceFloor;
import com.eseasky.modules.space.entity.SpaceGroup;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.mapper.SpaceBuildMapper;
import com.eseasky.modules.space.mapper.SpaceSeatMapper;
import com.eseasky.modules.space.service.SpaceBuildService;
import com.eseasky.modules.space.service.SpaceConfService;
import com.eseasky.modules.space.service.SpaceDeskService;
import com.eseasky.modules.space.service.SpaceFloorService;
import com.eseasky.modules.space.service.SpaceGroupService;
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.SpaceBuildVO;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.request.EditBuildStateBatchVO;
import com.eseasky.modules.space.vo.request.QueryBuildParam;
import com.eseasky.modules.space.vo.request.QueryMobileBuildParam;
import com.eseasky.modules.space.vo.request.QueryUseRuleVO;
import com.eseasky.modules.space.vo.response.BuildListVO;
import com.eseasky.modules.space.vo.response.BuildMapVO;
import com.eseasky.modules.space.vo.response.ConfIdVO;
import com.eseasky.modules.space.vo.response.PageListVO;
import com.eseasky.modules.space.vo.response.QueryMobileBulidList;
import com.eseasky.modules.space.vo.response.SubAdvanceDayVO;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeMultimap;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 综合楼服务实现类
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Slf4j
@Service
public class SpaceBuildServiceImpl extends ServiceImpl<SpaceBuildMapper, SpaceBuild> implements SpaceBuildService, OrgEnvent {

    @Autowired
    private SpaceFloorService floorService;
    @Autowired
    private SpaceRoomService roomService;
    @Autowired
    private SpaceDeskService deskService;
    @Autowired
    private SpaceSeatService seatService;
    @Autowired
    private SpaceConfService confService;
    @Autowired
    private SpaceGroupService groupService;
    @Autowired
    private OrderSeatListService seatListService;
    @Autowired
    private SpaceUtil spaceUtil;
    @Autowired
    private UploadProp prop;
    @Autowired
    private SysOrgService orgService;
    @Autowired
    private OrderMeetingService meetingService;
    @Autowired
    private OrderCommentService commentService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceBuildDistanceCache, allEntries = true)
    public R<String> addBuild(SpaceBuildVO spaceBuildVO) {
        SpaceBuild build = JSONObject.parseObject(JSON.toJSONString(spaceBuildVO), SpaceBuild.class);
        Date now = new Date();

        build.setCreateTime(now);
        build.setCreateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        build.setBuildImage(spaceUtil.deleteIpAndPort(build.getBuildImage()));
        Integer floorCount = build.getFloorCount();
        if (floorCount == null || floorCount < 1) {
            log.error("SpaceBuildServiceImpl-addBuild，综合楼楼层数量错误，spaceBuildVO：{}", JSON.toJSONString(spaceBuildVO));
            throw BusinessException.of("综合楼楼层数量错误！");
        }

        boolean save = save(build);
        if (!save) {
            log.error("SpaceBuildServiceImpl-addBuild，综合楼保存失败，spaceBuildVO：{}", JSON.toJSONString(spaceBuildVO));
            throw BusinessException.of("综合楼保存失败！");
        }

        floorService.createFloorByBuildId(floorCount, build.getBuildId(), now);

        return R.ok("保存成功！");
    }

    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceBuildDistanceCache, allEntries = true)
    public R<String> deleteBuild(String buildId) {

        SpaceBuild buildToLog = getById(buildId);
        if (buildToLog == null) {
            throw BusinessException.of("该综合楼不存在！");
        }

        // 逻辑删除
        removeById(buildId);
        floorService.remove(new QueryWrapper<SpaceFloor>().lambda().eq(SpaceFloor::getBuildId, buildId));
        roomService.remove(new QueryWrapper<SpaceRoom>().lambda().eq(SpaceRoom::getBuildId, buildId));
        deskService.remove(new QueryWrapper<SpaceDesk>().lambda().eq(SpaceDesk::getBuildId, buildId));
        seatService.remove(new QueryWrapper<SpaceSeat>().lambda().eq(SpaceSeat::getBuildId, buildId));
        groupService.remove(new QueryWrapper<SpaceGroup>().lambda().eq(SpaceGroup::getBuildId, buildId));

        // 根据综合楼id取消订单
        cancelOrder(Lists.newArrayList(buildId), SpaceConstant.SpaceType.BUILD);
        return R.ok("删除成功！");
    }

    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceBuildDistanceCache, allEntries = true)
    public R<String> deleteBuildBatch(JSONObject param) {
        List<String> buildIdList = JSONArray.parseArray(param.getJSONArray("buildIdList").toJSONString(), String.class);
        if (CollectionUtils.isEmpty(buildIdList)) {
            return R.error("获取参数失败！");
        }

        // 逻辑删除
        removeByIds(buildIdList);
        floorService.remove(new QueryWrapper<SpaceFloor>().lambda().in(SpaceFloor::getBuildId, buildIdList));
        roomService.remove(new QueryWrapper<SpaceRoom>().lambda().in(SpaceRoom::getBuildId, buildIdList));
        deskService.remove(new QueryWrapper<SpaceDesk>().lambda().in(SpaceDesk::getBuildId, buildIdList));
        seatService.remove(new QueryWrapper<SpaceSeat>().lambda().in(SpaceSeat::getBuildId, buildIdList));
        groupService.remove(new QueryWrapper<SpaceGroup>().lambda().in(SpaceGroup::getBuildId, buildIdList));

        // 根据综合楼id取消订单
        cancelOrder(buildIdList, SpaceConstant.SpaceType.BUILD);
        return R.ok("删除成功！");
    }


    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceBuildDistanceCache, allEntries = true)
    public R<String> editBuild(SpaceBuildVO spaceBuildVO) {

        SpaceBuild oldBuild = getById(spaceBuildVO.getBuildId());
        if (oldBuild == null) {
            log.error("SpaceBuildServiceImpl-editBuild，获取综合楼数据失败，spaceBuildVO：{}", JSON.toJSONString(spaceBuildVO));
            throw BusinessException.of("获取综合楼数据失败！");
        }

        SpaceBuild newBuild = JSONObject.parseObject(JSON.toJSONString(spaceBuildVO), SpaceBuild.class);
        newBuild.setCreateTime(oldBuild.getCreateTime());
        newBuild.setCreateUser(oldBuild.getCreateUser());
        newBuild.setUpdateTime(new Date());
        newBuild.setUpdateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        newBuild.setBuildImage(spaceUtil.deleteIpAndPort(newBuild.getBuildImage()));
        newBuild.setFloorCount(oldBuild.getFloorCount()); // 综合楼数量不能修改
        updateById(newBuild);

        // 根据新综合楼和老综合楼，批量修改座位的状态
        changeStateByNewBuildAndOldBuild(newBuild, oldBuild);

        // 原状态是开放，修改后的状态是关闭
        if (oldBuild.getBuildState().intValue() == SpaceConstant.Switch.OPEN) {
            if (newBuild.getBuildState().intValue() == SpaceConstant.Switch.CLOSE) {
                // 取消订单
                cancelOrder(Lists.newArrayList(spaceBuildVO.getBuildId()), SpaceConstant.SpaceType.BUILD);
            }
        }

        return R.ok("修改成功！");
    }

    /** 根据新综合楼和老综合楼，批量修改座位的状态 */
    private void changeStateByNewBuildAndOldBuild(SpaceBuild newBuild, SpaceBuild oldBuild) {
        // 综合楼状态前后不一致
        if (oldBuild.getBuildState().intValue() != newBuild.getBuildState().intValue()) {

            // 综合楼变为关闭
            if (newBuild.getBuildState().intValue() == SpaceConstant.Switch.CLOSE) {
                seatService.lambdaUpdate() //
                        .set(SpaceSeat::getSeatState, SpaceConstant.Switch.CLOSE) //
                        .set(SpaceSeat::getParentState, SpaceConstant.SpaceType.BUILD) //
                        .eq(SpaceSeat::getBuildId, newBuild.getBuildId()) //
                        .le(SpaceSeat::getParentState, SpaceConstant.SpaceType.BUILD) //
                        .update();
            }

            // 综合楼变为开放
            if (newBuild.getBuildState().intValue() == SpaceConstant.Switch.OPEN) {
                seatService.lambdaUpdate() //
                        .set(SpaceSeat::getSeatState, SpaceConstant.Switch.OPEN) //
                        .set(SpaceSeat::getParentState, SpaceConstant.SpaceType.NONE) //
                        .eq(SpaceSeat::getBuildId, newBuild.getBuildId()) //
                        .le(SpaceSeat::getParentState, SpaceConstant.SpaceType.BUILD) //
                        .update();
            }
        }
    }


    @Override
    @Transactional
    public R<String> modifyConfId(JSONObject param) {
        String buildId = param.getString("buildId");
        String confId = param.getString("confId");
        if (StringUtils.isBlank(buildId) || StringUtils.isBlank(confId)) {
            log.error("SpaceBuildServiceImpl-modifyConfId，获取参数失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取参数失败！");
        }

        SpaceBuild build = getById(buildId);
        if (build == null) {
            log.error("SpaceBuildServiceImpl-modifyConfId，获取综合楼失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取综合楼失败！");
        }

        SpaceConf conf = confService.lambdaQuery().eq(SpaceConf::getConfId, confId).one();
        if (conf == null) {
            log.error("SpaceBuildServiceImpl-modifyConfId，获取配置规则失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取配置规则失败！");
        }


        // 根据buildId更新
        lambdaUpdate() //
                .set(SpaceBuild::getConfId, confId) //
                .set(SpaceBuild::getConfUser, SecurityUtils.getUser().getSysUserDTO().getId()) //
                .set(SpaceBuild::getConfTime, new Date()) //
                .eq(SpaceBuild::getBuildId, buildId) //
                .update();

        // ParentConf <= 1，【0没有，1综合楼】
        seatService.lambdaUpdate()//
                .set(SpaceSeat::getConfId, confId)//
                .set(SpaceSeat::getParentConf, SpaceConstant.SpaceType.BUILD) //
                .eq(SpaceSeat::getBuildId, buildId) //
                .le(SpaceSeat::getParentConf, SpaceConstant.SpaceType.BUILD) //
                .update();

        return R.ok("修改成功！");
    }


    @Override
    @Transactional
    public R<PageListVO<SpaceBuildVO>> getSpaceBuildList(QueryBuildParam param) {
        long pageSize = param.getPageSize();
        long pageNum = param.getPageNum();
        Integer bulidState = param.getBulidState();
        String buildName = param.getBuildName();
        String buildDeptId = param.getBuildDeptId();

        // 数据权限
        Set<String> orgIdList = spaceUtil.getOrgIdList();

        // 开始查询SpaceBuild表
        LambdaQueryChainWrapper<SpaceBuild> wrapper = lambdaQuery();
        wrapper.select( //
                SpaceBuild::getBuildId, //
                SpaceBuild::getBuildName, //
                SpaceBuild::getFloorCount, //
                SpaceBuild::getBuildImage, //
                SpaceBuild::getCoordx, //
                SpaceBuild::getCoordy, //
                SpaceBuild::getCoordName, //
                SpaceBuild::getBuildDeptId, //
                SpaceBuild::getConfId, //
                SpaceBuild::getBuildNum, //
                SpaceBuild::getBuildState);
        if (!CollectionUtils.isEmpty(orgIdList)) {
            wrapper.in(SpaceBuild::getBuildDeptId, orgIdList);
        }
        if (bulidState != null) {
            wrapper.eq(SpaceBuild::getBuildState, bulidState);
        }
        if (!StringUtils.isEmpty(buildName)) {
            wrapper.like(SpaceBuild::getBuildName, buildName);
        }
        if (!StringUtils.isEmpty(buildDeptId)) {
            wrapper.eq(SpaceBuild::getBuildDeptId, buildDeptId);
        }
        wrapper.orderByDesc(SpaceBuild::getCreateTime); // 创建时间，倒序
        Page<SpaceBuild> page = new Page<SpaceBuild>(pageNum, pageSize);
        wrapper.page(page); // 分页

        // 分页查询没数据直接返回
        List<SpaceBuild> buildList = page.getRecords();
        if (CollectionUtils.isEmpty(buildList)) {
            PageListVO<SpaceBuildVO> result = new PageListVO<SpaceBuildVO>();
            result.setList(new ArrayList<SpaceBuildVO>());
            result.setTotal(0);
            result.setPages(0);
            result.setPageSize(pageSize);
            result.setPageNum(pageNum);
            return R.ok(result);
        }

        // 业务处理
        List<SpaceBuildVO> voList = new LinkedList<SpaceBuildVO>();
        for (SpaceBuild spaceBuild : buildList) {
            SpaceBuildVO vo = new SpaceBuildVO();
            vo.setBuildId(spaceBuild.getBuildId());
            vo.setBuildName(spaceBuild.getBuildName());
            vo.setBuildNum(spaceBuild.getBuildNum());
            vo.setCoordx(spaceBuild.getCoordx());
            vo.setCoordy(spaceBuild.getCoordy());
            vo.setCoordName(spaceBuild.getCoordName());
            vo.setBuildImage(spaceUtil.addIpAndPort(spaceBuild.getBuildImage(), prop.getIpPort()));
            vo.setFloorCount(spaceBuild.getFloorCount());
            vo.setBuildState(spaceBuild.getBuildState());
            vo.setBuildStateName(SpaceConstant.StateName.MAP.get(spaceBuild.getBuildState()));
            vo.setBuildDeptId(spaceBuild.getBuildDeptId());
            vo.setConfId(spaceBuild.getConfId());
            SysOrg org = orgService.lambdaQuery().select(SysOrg::getOrgName).eq(SysOrg::getId, spaceBuild.getBuildDeptId()).one();
            if (org != null) {
                vo.setBuildDeptName(org.getOrgName());
            }
            voList.add(vo);
        }

        PageListVO<SpaceBuildVO> result = new PageListVO<SpaceBuildVO>();
        result.setList(voList);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return R.ok(result);
    }

    @Override
    @Transactional
    public R<SpaceBuildVO> findOneBuild(String buildId) {
        SpaceBuild build = getById(buildId);
        if (build == null) {
            log.error("SpaceBuildServiceImpl-findOneBuild，获取单个综合楼失败，buildId：{}", buildId);
            throw BusinessException.of("获取单个综合楼失败！");
        }

        SpaceBuildVO vo = JSONObject.parseObject(JSON.toJSONString(build), SpaceBuildVO.class);
        vo.setBuildImage(spaceUtil.addIpAndPort(vo.getBuildImage(), prop.getIpPort()));
        vo.setBuildStateName(SpaceConstant.StateName.MAP.get(vo.getBuildState()));
        return R.ok(vo);
    }

    /**
     * 手机端--综合楼列表展示
     */
    @Override
    @Transactional
    public R<BuildListVO<QueryMobileBulidList>> getMobileBuildList(QueryMobileBuildParam param) {
        Integer closeWindow = param.getCloseWindow();
        Integer haveSocket = param.getHaveSocket();
        Integer awayToilet = param.getAwayToilet();
        Integer awayDoor = param.getAwayDoor();
        long pageNum = param.getPageNum();
        long pageSize = param.getPageSize();
        Integer orderType = param.getOrderType(); // 1,2,3,4,5
        String sDate = param.getStartDate();
        String eDate = param.getEndDate();
        String startTime = param.getStartTime(); // HH:mm
        String endTime = param.getEndTime(); // HH:mm，非手机端参数，下面判断用到

        Date startDate = null;
        Date endDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            startDate = sdf.parse(sDate + " " + startTime);
            endDate = sdf.parse(eDate + " " + endTime);
        } catch (Exception e) {
            log.error("SpaceBuildServiceImpl-getMobileBuildList，日期格式错误，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("日期格式错误！");
        }

        /** 规则筛选 */
        ConfIdVO confIdVO = spaceUtil.getConfIdListForCurrentUser(startTime, endTime, startDate, endDate, orderType);
        List<String> openList = confIdVO.getOpenList();
        List<String> freeList = confIdVO.getFreeList();


        Set<String> buildIdList = null;
        LinkedListMultimap<String, String> buildIdAndRoomTotal = LinkedListMultimap.create();

        /** 分为两种情况。1：未勾选座位属性，综合楼分为可见和可预约；2：勾选座位属性，根据属性全部过滤。 */
        if (closeWindow.intValue() == SpaceConstant.Switch.NO && //
                haveSocket.intValue() == SpaceConstant.Switch.NO && //
                awayToilet.intValue() == SpaceConstant.Switch.NO && //
                awayDoor.intValue() == SpaceConstant.Switch.NO) {

            /** 当前用户可见的confId、当前用户可预约的confId */
            if (CollectionUtils.isEmpty(openList)) {
                log.debug("SpaceBuildServiceImpl-getMobileBuildList，暂无适合您的空间-无配置规则！");
                return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
            }


            /** 根据规则筛选可见的综合楼，顺便统计每个综合楼下的房间总数 */
            List<Map<String, Object>> temList = seatService.getBaseMapper().selectMaps(new QueryWrapper<SpaceSeat>().lambda() //
                    .select(SpaceSeat::getBuildId, SpaceSeat::getRoomId) //
                    .in(SpaceSeat::getConfId, openList) //
                    .groupBy(SpaceSeat::getBuildId) //
                    .groupBy(SpaceSeat::getRoomId));
            for (Map<String, Object> map : temList) {
                buildIdAndRoomTotal.put(map.get("build_id").toString(), map.get("room_id").toString());
            }


            /** 所有可见的buildId */
            buildIdList = buildIdAndRoomTotal.keySet();
            if (CollectionUtils.isEmpty(buildIdList)) {
                log.debug("SpaceBuildServiceImpl-getMobileBuildList，暂无适合您的空间-无可见的综合楼！");
                return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
            }
        } else {
            /** 当前用户可预约的confId，因为要和buildIdbyTag取并集，就不再需要openList */
            if (CollectionUtils.isEmpty(freeList)) {
                log.debug("SpaceBuildServiceImpl-getMobileBuildList，暂无适合您的空间-根据日期时间过滤无配置规则！");
                return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
            }


            /** 根据规则筛选可预约的综合楼，顺便统计每个综合楼下的房间总数 */
            List<Map<String, Object>> temList = seatService.getBaseMapper().selectMaps(new QueryWrapper<SpaceSeat>().lambda() //
                    .select(SpaceSeat::getBuildId, SpaceSeat::getRoomId) //
                    .in(SpaceSeat::getConfId, freeList) //
                    .groupBy(SpaceSeat::getBuildId) //
                    .groupBy(SpaceSeat::getRoomId));
            for (Map<String, Object> map : temList) {
                buildIdAndRoomTotal.put(map.get("build_id").toString(), map.get("room_id").toString());
            }


            /** 所有可预约的buildId */
            buildIdList = buildIdAndRoomTotal.keySet();
            if (CollectionUtils.isEmpty(buildIdList)) {
                log.debug("SpaceBuildServiceImpl-getMobileBuildList，暂无适合您的空间-无可预约的综合楼！");
                return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
            }


            /** 如果是单人短租，就筛选座位属性。这里必定是单人短租，因为只有单人短租可选座位属性 */
            if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE) {
                List<String> buildIdbyTag = spaceUtil.filterBuildIdByTag(closeWindow, haveSocket, awayToilet, awayDoor);
                if (buildIdbyTag == null) {
                    // 手机端未勾选任何过滤条件
                } else if (buildIdbyTag.size() == 0) {
                    // 手机端勾选了过滤条件，但是没查到数据
                    buildIdList.clear();
                } else {
                    // 手机端勾选了过滤条件，并且查到数据，取交集
                    buildIdList.retainAll(buildIdbyTag);
                }

                if (CollectionUtils.isEmpty(buildIdList)) {
                    log.debug("SpaceBuildServiceImpl-getMobileBuildList，暂无适合您的空间-座位属性过滤之后无可预约综合楼！");
                    return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
                }
            }
        }



        /** 对可见的buildIdList进行分页 */
        Page<QueryMobileBulidList> page = pageAndOrderInMemory(param, buildIdList, SecurityUtils.getUser().getSysUserDTO().getId());
        List<QueryMobileBulidList> records = page.getRecords();
        buildIdList = records.stream().map(m -> m.getBuildId()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(buildIdList)) {
            log.debug("SpaceBuildServiceImpl-getMobileBuildList，暂无适合您的空间-分页无可见的综合楼！");
            return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
        }


        /** 查询可预约的数量 */
        Map<String, Integer> buildIdAndSeatNotUsedTotal = new HashMap<>();
        if (!CollectionUtils.isEmpty(freeList)) {
            List<JSONObject> list = null;
            if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.SINGLE_LONG
                    || orderType.intValue() == SpaceConstant.OrderType.SINGLE_GROUP) {
                // 可预约confId、可见seat、非删除seat，并且是分页buildId，排除时间段内占用的seatId
                list = ((SpaceSeatMapper) seatService.getBaseMapper()).singleOrderForBuild(freeList, buildIdList, startDate, endDate, orderType);
                for (JSONObject map : list) {
                    buildIdAndSeatNotUsedTotal.put(map.getString("buildId"), map.getIntValue("seatNotUsedTotal"));
                }
            } else if (orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_LONG) {
                // 可预约confId、可见seat、非删除seat，并且是分页buildId，排除时间段内占用的roomId
                list = ((SpaceSeatMapper) seatService.getBaseMapper()).multiOrderForBuild(freeList, buildIdList, startDate, endDate, orderType);
                for (JSONObject map : list) {
                    buildIdAndSeatNotUsedTotal.put(map.getString("buildId"), map.getIntValue("roomNotUsedTotal"));
                }
            }
        }


        // 分页综合楼数据处理
        for (QueryMobileBulidList vo : records) {
            vo.setBuildImage(spaceUtil.addIpAndPort(vo.getBuildImage(), prop.getIpPort()));
            Integer notUsedTotal = buildIdAndSeatNotUsedTotal.get(vo.getBuildId());
            if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.SINGLE_LONG
                    || orderType.intValue() == SpaceConstant.OrderType.SINGLE_GROUP) {

                // 禁用状态可用都是0，但总数不变
                if (vo.getBuildState().intValue() == SpaceConstant.Switch.CLOSE) {
                    vo.setSeatNotUsedTotal(0);
                } else {
                    vo.setSeatNotUsedTotal(notUsedTotal == null ? 0 : notUsedTotal);
                }

            } else if (orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_LONG) {

                // 禁用状态可用都是0，但总数不变
                if (vo.getBuildState().intValue() == SpaceConstant.Switch.CLOSE) {
                    vo.setRoomNotUsedTotal(0);
                } else {
                    vo.setRoomNotUsedTotal(notUsedTotal == null ? 0 : notUsedTotal);
                }
                vo.setRoomTotal(buildIdAndRoomTotal.get(vo.getBuildId()).size());
            }
        }

        BuildListVO<QueryMobileBulidList> result = new BuildListVO<QueryMobileBulidList>();
        result.setList(records);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return R.ok(result);
    }

    private R<BuildListVO<QueryMobileBulidList>> emptyPage(long pageNum, long pageSize, String msg) {
        BuildListVO<QueryMobileBulidList> result = new BuildListVO<QueryMobileBulidList>();
        result.setList(new ArrayList<>());
        result.setTotal(0);
        result.setPages(0);
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return new R<>(result, msg);
    }

    /**
     * 
     * @param param 前端参数
     * @param buildIdList 对用户开放的场馆id
     * @param userId 用户id
     */
    private Page<QueryMobileBulidList> pageAndOrderInMemory(QueryMobileBuildParam param, Set<String> buildIdList, String userId) {
        Integer usedToGo = param.getUsedToGo();
        Integer distanceFirst = param.getDistanceFirst();
        Integer highScore = param.getHighScore();
        long pageNum = param.getPageNum();
        long pageSize = param.getPageSize();


        // 前端没有任何排序，默认分页排序
        if (usedToGo.intValue() == SpaceConstant.Switch.NO // 未勾选常去
                && distanceFirst.intValue() == SpaceConstant.Switch.NO // 未勾选距离
                && highScore.intValue() == SpaceConstant.Switch.NO) { // 未勾选评分
            Page<QueryMobileBulidList> page = new Page<QueryMobileBulidList>(pageNum, pageSize);
            List<QueryMobileBulidList> records = getBaseMapper().queryMobileBulidList(page, buildIdList);
            page.setRecords(records);
            return page;
        }


        // 排序好的场馆id集合，还未分页
        List<String> orderedBuildIdToBePage = new ArrayList<String>();

        // 常去
        if (usedToGo.intValue() == SpaceConstant.Switch.YES) {

            // 常去场馆总次数从高到低排序，并且按顺序放入list集合
            ZSetOperations<String, Object> forZSet = redisTemplate.opsForZSet();
            Set<Object> range = forZSet.reverseRange(OrderConstant.PERSON_ORDER_BUILD + userId, 0, -1); // 取不到是空集合
            List<String> usedToGoList = new ArrayList<String>();
            for (Object object : range) {
                usedToGoList.add(object.toString());
            }

            TreeSet<String> buildIdAll = new TreeSet<String>(buildIdList); // hash排序
            usedToGoList.retainAll(buildIdAll); // 交集
            buildIdAll.removeAll(usedToGoList); // 并集
            usedToGoList.addAll(buildIdAll); // 常去+没去

            orderedBuildIdToBePage = usedToGoList;
        }

        // 高评分
        else if (highScore.intValue() == SpaceConstant.Switch.YES) {
            TreeMultimap<Double, String> avgAndBuildId = TreeMultimap.create();

            HashOperations<String, Object, Object> forHash = redisTemplate.opsForHash();
            TreeSet<String> buildIdAll = new TreeSet<String>(buildIdList); // hash排序
            for (String bid : buildIdAll) {
                Map<Object, Object> personTotal = forHash.entries(OrderConstant.COMMENT_PERSON + bid);
                Map<Object, Object> levelTotal = forHash.entries(OrderConstant.COMMENT_LEVEL + bid);
                if (MapUtils.isEmpty(personTotal)) {
                    avgAndBuildId.put(0.0d, bid);
                    continue;
                }
                double personSum = personTotal.values().stream().mapToDouble(m -> Double.valueOf(m.toString())).sum();
                double levelSum = levelTotal.values().stream().mapToDouble(m -> Double.valueOf(m.toString())).sum();
                avgAndBuildId.put(levelSum / personSum, bid);
            }

            // 场馆的评分由高到低排序
            List<String> highScoreList = Lists.newArrayList(avgAndBuildId.values());
            Collections.reverse(highScoreList);
            orderedBuildIdToBePage = highScoreList;
        }

        // 距离优先
        else if (distanceFirst.intValue() == SpaceConstant.Switch.YES) {

            String coordx = param.getCoordx();
            String coordy = param.getCoordy();
            if (StringUtils.isEmpty(coordx) || StringUtils.isEmpty(coordy)) {
                throw BusinessException.of("请确认当前用户坐标！");
            }

            // 当前用户的坐标对象
            double userCoordx = Double.parseDouble(param.getCoordx());
            double userCoordy = Double.parseDouble(param.getCoordy());
            GlobalCoordinates gpsFrom = new GlobalCoordinates(userCoordy, userCoordx);

            // 从缓存读取场馆
            SpaceBuildService buildService = (SpaceBuildService) AopContext.currentProxy();
            List<SpaceBuild> list = buildService.getAllBuildForMobileDistanceOrder(SecurityUtils.getUser().getTenantCode());

            // 循环每一个场馆，计算当前用户和场馆的距离
            TreeMultimap<Double, String> map = TreeMultimap.create();
            if (CollectionUtils.isEmpty(list)) {
                orderedBuildIdToBePage = Lists.newArrayList(new TreeSet<String>(buildIdList));
            } else {
                for (SpaceBuild build : list) {
                    if (!buildIdList.contains(build.getBuildId())) {
                        continue;
                    }

                    // 用户坐标和场馆坐标一一比对
                    GlobalCoordinates gpsTo = new GlobalCoordinates(Double.parseDouble(build.getCoordy()), Double.parseDouble(build.getCoordx()));
                    double distanceMeter = spaceUtil.getDistanceMeter(gpsFrom, gpsTo, Ellipsoid.WGS84);
                    map.put(distanceMeter, build.getBuildId());
                }

                // 场馆的距离由高到低排序
                List<String> distanceFirstList = Lists.newArrayList(map.values());
                Collections.reverse(distanceFirstList);
                orderedBuildIdToBePage = distanceFirstList;
            }
        }

        // 参数错误
        else {
            throw BusinessException.of("参数错误！");
        }


        // 截断集合参数
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = fromIndex + (int) pageSize;
        if (fromIndex > orderedBuildIdToBePage.size()) { // 起始索引不能大于集合长度
            fromIndex = orderedBuildIdToBePage.size();
        }
        if (toIndex > orderedBuildIdToBePage.size()) { // 终止索引不能大于集合长度
            toIndex = orderedBuildIdToBePage.size();
        }


        // 开始分页
        Page<QueryMobileBulidList> page = new Page<QueryMobileBulidList>(pageNum, pageSize);
        page.setTotal(orderedBuildIdToBePage.size());
        page.setPages(buildIdList.size() % pageSize == 0 ? buildIdList.size() / pageSize : (buildIdList.size() / pageSize + 1));


        List<String> subBuildIdList = orderedBuildIdToBePage.subList(fromIndex, toIndex);
        if (CollectionUtils.isEmpty(subBuildIdList)) {
            page.setRecords(new ArrayList<QueryMobileBulidList>());
        } else {
            // 分页场馆id查询分页数据
            List<QueryMobileBulidList> pageList = getBaseMapper().queryMobileBulidListNoPage(subBuildIdList);

            // 取出来的pageList必须根据subBuildIdList再排序
            List<QueryMobileBulidList> records = new ArrayList<QueryMobileBulidList>();
            for (String bid : subBuildIdList) {
                records.add(comp(pageList, bid));
            }
            page.setRecords(records);
        }

        return page;
    }

    private QueryMobileBulidList comp(List<QueryMobileBulidList> pageList, String bid) {
        for (QueryMobileBulidList build : pageList) {
            if (build.getBuildId().equals(bid)) {
                return build;
            }
        }
        return null;
    }


    @Override
    public R<SubAdvanceDayVO> getSubAdvanceDay() {

        Map<String, SpaceConfVO> map = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (map == null || map.size() == 0) {
            return R.ok(new SubAdvanceDayVO());
        }

        Collection<SpaceConfVO> values = map.values();
        TreeSet<Integer> subAdvanceDayList = new TreeSet<Integer>();
        TreeSet<Integer> subMinTimeList = new TreeSet<Integer>();
        TreeSet<Integer> subMaxTimeList = new TreeSet<Integer>();
        for (SpaceConfVO vo : values) {
            if (vo.getSubAdvanceDay() != null) {
                subAdvanceDayList.add(vo.getSubAdvanceDay().intValue());
            }
            if (vo.getSubMinTime() != null) {
                subMinTimeList.add(vo.getSubMinTime().intValue());
            }
            if (vo.getSubMaxTime() != null) {
                subMaxTimeList.add(vo.getSubMaxTime().intValue());
            }
        }

        Integer subAdvanceDay = subAdvanceDayList.last();
        LocalDate now = LocalDate.now();
        List<String> subList = Lists.newArrayList(now.toString());
        for (int i = 0; i < subAdvanceDay; i++) {
            subList.add(now.plusDays(i + 1).toString());
        }

        SubAdvanceDayVO result = new SubAdvanceDayVO();
        result.setSubAdvanceDay(subAdvanceDayList.last());
        result.setSubMinTime(subMinTimeList.first());
        result.setSubMaxTime(subMaxTimeList.last());
        result.setSubAdvanceDayList(subList);

        return R.ok(result);
    }


    /**
     * 删除场馆、楼层、房间、座位，级联删除相关的数据
     */
    @Override
    public void cancelOrder(List<String> idList, int spaceType) {
        seatListService.manageCancel(idList, spaceType);
        meetingService.delSpace(idList, spaceType);
        commentService.delCommentBySpace(idList, spaceType);
    }


    /**
     * 手机端首页，地图上需要展示当前用户可见的综合楼
     */
    @Override
    @Transactional
    public R<List<BuildMapVO>> getBuildListForMap() {
        Map<String, SpaceConfVO> map = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (MapUtils.isEmpty(map)) {
            return new R<List<BuildMapVO>>(new ArrayList<BuildMapVO>(), "获取配置规则失败！");
        }

        Collection<SpaceConfVO> values = map.values();
        if (CollectionUtils.isEmpty(values)) {
            return new R<List<BuildMapVO>>(new ArrayList<BuildMapVO>(), "获取配置规则失败！");
        }

        List<String> openList = new ArrayList<String>();
        for (SpaceConfVO conf : values) {
            String showRoom = spaceUtil.showRoom(conf, conf.getOrderType());
            if (showRoom == null) {
                openList.add(conf.getConfId());
            }
        }

        if (CollectionUtils.isEmpty(openList)) {
            return new R<List<BuildMapVO>>(new ArrayList<BuildMapVO>(), "暂无适合您的空间！");
        }

        /** 查询对当前用户可见的综合楼，综合楼可能被禁用 */
        List<Object> buildIdObj = seatService.getBaseMapper().selectObjs(new QueryWrapper<SpaceSeat>().lambda() //
                .select(SpaceSeat::getBuildId) //
                .in(SpaceSeat::getConfId, openList) //
                .groupBy(SpaceSeat::getBuildId));
        if (CollectionUtils.isEmpty(buildIdObj)) {
            return new R<List<BuildMapVO>>(new ArrayList<BuildMapVO>(), "暂无适合您的空间！");
        }
        Set<String> buildIdList = buildIdObj.stream().map(m -> m.toString()).collect(Collectors.toSet());

        List<SpaceBuild> list = lambdaQuery().in(SpaceBuild::getBuildId, buildIdList).list();
        List<BuildMapVO> voList = new LinkedList<BuildMapVO>();
        for (SpaceBuild spaceBuild : list) {
            BuildMapVO vo = new BuildMapVO();
            vo.setBuildId(spaceBuild.getBuildId());
            vo.setBuildName(spaceBuild.getBuildName());
            vo.setBuildNum(spaceBuild.getBuildNum());
            vo.setCoordx(spaceBuild.getCoordx());
            vo.setCoordy(spaceBuild.getCoordy());
            vo.setCoordName(spaceBuild.getCoordName());
            vo.setBuildState(spaceBuild.getBuildState());
            vo.setBuildImage(spaceUtil.addIpAndPort(spaceBuild.getBuildImage(), prop.getIpPort()));
            vo.setBuildStateName(SpaceConstant.StateName.MAP.get(spaceBuild.getBuildState()));
            voList.add(vo);
        }
        return R.ok(voList);
    }

    @Override
    @Transactional
    public R<String> addUseRule(QueryUseRuleVO param) {
        String buildId = param.getBuildId();
        String useRule = param.getUseRule();

        SpaceBuild build = getById(buildId);
        if (build == null) {
            log.debug("SpaceBuildServiceImpl-addSpaceUseRule，获取综合楼失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取综合楼失败！");
        }

        lambdaUpdate().set(SpaceBuild::getUseRule, useRule).eq(SpaceBuild::getBuildId, buildId).update();
        return R.ok();
    }

    @Override
    @Transactional
    public R<QueryUseRuleVO> getUseRule(String buildId) {
        SpaceBuild build = getById(buildId);
        if (build == null) {
            log.debug("SpaceBuildServiceImpl-getSpaceUseRule，获取综合楼失败，buildId：{}", buildId);
            throw BusinessException.of("获取综合楼失败！");
        }

        QueryUseRuleVO vo = new QueryUseRuleVO();
        vo.setBuildId(buildId);
        vo.setUseRule(build.getUseRule());
        return R.ok(vo);
    }

    @Override
    @Transactional
    public R<QueryUseRuleVO> getMobileUseRule(String buildId) {
        SpaceBuild build = getById(buildId);
        if (build == null) {
            log.debug("SpaceBuildServiceImpl-getSpaceUseRule，获取综合楼失败，buildId：{}", buildId);
            throw BusinessException.of("获取综合楼失败！");
        }

        QueryUseRuleVO vo = new QueryUseRuleVO();
        vo.setBuildId(buildId);
        vo.setUseRule(build.getUseRule());
        return R.ok(vo);
    }

    /**
     * 删除部门的时候，级联删除综合楼的所属部门
     */
    @Override
    public void deleteOrgCascade(List<String> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return;
        }
        lambdaUpdate().set(SpaceBuild::getBuildDeptId, null).in(SpaceBuild::getBuildDeptId, orgIds).update();
    }

    /**
     * 批量修改场馆的状态和所属组织
     */
    @Override
    @Transactional
    public R<String> editBuildStateBatch(EditBuildStateBatchVO param) {
        List<String> buildIdList = param.getBuildIdList();
        Integer buildState = param.getBuildState();
        String buildDeptId = param.getBuildDeptId();

        // 两个都是非必填参数
        if (buildState == null && StringUtils.isEmpty(buildDeptId)) {
            throw BusinessException.of("请选择批量修改内容！");
        }

        if (buildState != null && buildState != SpaceConstant.Switch.CLOSE && buildState != SpaceConstant.Switch.OPEN) {
            throw BusinessException.of("综合楼状态错误！");
        }

        // 记录哪些综合楼需要取消订单
        List<String> idList = new ArrayList<String>();

        // 第一：修改每个综合楼下面的座位状态，顺便记录需要取消订单的综合楼id
        if (buildState != null) {
            List<SpaceBuild> oldList = lambdaQuery().in(SpaceBuild::getBuildId, buildIdList).list();
            for (SpaceBuild oldBuild : oldList) {

                // 修改的状态和综合楼原来的值相同，就忽略
                if (buildState.intValue() == oldBuild.getBuildState().intValue()) {
                    continue;
                }

                SpaceBuild newBuild = new SpaceBuild();
                newBuild.setBuildId(oldBuild.getBuildId());
                newBuild.setBuildState(buildState);

                // 根据新综合楼和老综合楼，批量修改座位的状态
                changeStateByNewBuildAndOldBuild(newBuild, oldBuild);

                if (oldBuild.getBuildState().intValue() == SpaceConstant.Switch.OPEN) {
                    if (newBuild.getBuildState().intValue() == SpaceConstant.Switch.CLOSE) {
                        // 取消订单的综合楼id
                        idList.add(oldBuild.getBuildId());
                    }
                }
            }
        }


        // 第二：修改综合楼本身的状态和所属部门id
        LambdaUpdateChainWrapper<SpaceBuild> lambdaUpdate = lambdaUpdate();
        if (buildState != null) {
            lambdaUpdate.set(SpaceBuild::getBuildState, buildState);
        }
        if (StringUtils.isNotBlank(buildDeptId)) {
            lambdaUpdate.set(SpaceBuild::getBuildDeptId, buildDeptId);
        }
        lambdaUpdate.in(SpaceBuild::getBuildId, buildIdList);
        lambdaUpdate.update();


        // 第三：取消订单
        if (!CollectionUtils.isEmpty(idList)) {
            cancelOrder(idList, SpaceConstant.SpaceType.BUILD);
        }

        return R.ok();
    }

    /**
     * 手机首页需要按照综合楼距离远近排序，在这里获取所有的综合楼id和坐标放在缓存
     */
    @Override
    @Cacheable(value = SpaceConstant.spaceBuildDistanceCache, key = "#tenantCode")
    public List<SpaceBuild> getAllBuildForMobileDistanceOrder(String tenantCode) {
        return lambdaQuery().select(SpaceBuild::getBuildId, SpaceBuild::getCoordx, SpaceBuild::getCoordy).list();
    }


}
