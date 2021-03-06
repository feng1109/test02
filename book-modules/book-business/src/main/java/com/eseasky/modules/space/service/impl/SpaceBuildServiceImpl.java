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
 * ????????????????????????
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
            log.error("SpaceBuildServiceImpl-addBuild?????????????????????????????????spaceBuildVO???{}", JSON.toJSONString(spaceBuildVO));
            throw BusinessException.of("??????????????????????????????");
        }

        boolean save = save(build);
        if (!save) {
            log.error("SpaceBuildServiceImpl-addBuild???????????????????????????spaceBuildVO???{}", JSON.toJSONString(spaceBuildVO));
            throw BusinessException.of("????????????????????????");
        }

        floorService.createFloorByBuildId(floorCount, build.getBuildId(), now);

        return R.ok("???????????????");
    }

    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceBuildDistanceCache, allEntries = true)
    public R<String> deleteBuild(String buildId) {

        SpaceBuild buildToLog = getById(buildId);
        if (buildToLog == null) {
            throw BusinessException.of("????????????????????????");
        }

        // ????????????
        removeById(buildId);
        floorService.remove(new QueryWrapper<SpaceFloor>().lambda().eq(SpaceFloor::getBuildId, buildId));
        roomService.remove(new QueryWrapper<SpaceRoom>().lambda().eq(SpaceRoom::getBuildId, buildId));
        deskService.remove(new QueryWrapper<SpaceDesk>().lambda().eq(SpaceDesk::getBuildId, buildId));
        seatService.remove(new QueryWrapper<SpaceSeat>().lambda().eq(SpaceSeat::getBuildId, buildId));
        groupService.remove(new QueryWrapper<SpaceGroup>().lambda().eq(SpaceGroup::getBuildId, buildId));

        // ???????????????id????????????
        cancelOrder(Lists.newArrayList(buildId), SpaceConstant.SpaceType.BUILD);
        return R.ok("???????????????");
    }

    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceBuildDistanceCache, allEntries = true)
    public R<String> deleteBuildBatch(JSONObject param) {
        List<String> buildIdList = JSONArray.parseArray(param.getJSONArray("buildIdList").toJSONString(), String.class);
        if (CollectionUtils.isEmpty(buildIdList)) {
            return R.error("?????????????????????");
        }

        // ????????????
        removeByIds(buildIdList);
        floorService.remove(new QueryWrapper<SpaceFloor>().lambda().in(SpaceFloor::getBuildId, buildIdList));
        roomService.remove(new QueryWrapper<SpaceRoom>().lambda().in(SpaceRoom::getBuildId, buildIdList));
        deskService.remove(new QueryWrapper<SpaceDesk>().lambda().in(SpaceDesk::getBuildId, buildIdList));
        seatService.remove(new QueryWrapper<SpaceSeat>().lambda().in(SpaceSeat::getBuildId, buildIdList));
        groupService.remove(new QueryWrapper<SpaceGroup>().lambda().in(SpaceGroup::getBuildId, buildIdList));

        // ???????????????id????????????
        cancelOrder(buildIdList, SpaceConstant.SpaceType.BUILD);
        return R.ok("???????????????");
    }


    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceBuildDistanceCache, allEntries = true)
    public R<String> editBuild(SpaceBuildVO spaceBuildVO) {

        SpaceBuild oldBuild = getById(spaceBuildVO.getBuildId());
        if (oldBuild == null) {
            log.error("SpaceBuildServiceImpl-editBuild?????????????????????????????????spaceBuildVO???{}", JSON.toJSONString(spaceBuildVO));
            throw BusinessException.of("??????????????????????????????");
        }

        SpaceBuild newBuild = JSONObject.parseObject(JSON.toJSONString(spaceBuildVO), SpaceBuild.class);
        newBuild.setCreateTime(oldBuild.getCreateTime());
        newBuild.setCreateUser(oldBuild.getCreateUser());
        newBuild.setUpdateTime(new Date());
        newBuild.setUpdateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        newBuild.setBuildImage(spaceUtil.deleteIpAndPort(newBuild.getBuildImage()));
        newBuild.setFloorCount(oldBuild.getFloorCount()); // ???????????????????????????
        updateById(newBuild);

        // ???????????????????????????????????????????????????????????????
        changeStateByNewBuildAndOldBuild(newBuild, oldBuild);

        // ????????????????????????????????????????????????
        if (oldBuild.getBuildState().intValue() == SpaceConstant.Switch.OPEN) {
            if (newBuild.getBuildState().intValue() == SpaceConstant.Switch.CLOSE) {
                // ????????????
                cancelOrder(Lists.newArrayList(spaceBuildVO.getBuildId()), SpaceConstant.SpaceType.BUILD);
            }
        }

        return R.ok("???????????????");
    }

    /** ??????????????????????????????????????????????????????????????? */
    private void changeStateByNewBuildAndOldBuild(SpaceBuild newBuild, SpaceBuild oldBuild) {
        // ??????????????????????????????
        if (oldBuild.getBuildState().intValue() != newBuild.getBuildState().intValue()) {

            // ?????????????????????
            if (newBuild.getBuildState().intValue() == SpaceConstant.Switch.CLOSE) {
                seatService.lambdaUpdate() //
                        .set(SpaceSeat::getSeatState, SpaceConstant.Switch.CLOSE) //
                        .set(SpaceSeat::getParentState, SpaceConstant.SpaceType.BUILD) //
                        .eq(SpaceSeat::getBuildId, newBuild.getBuildId()) //
                        .le(SpaceSeat::getParentState, SpaceConstant.SpaceType.BUILD) //
                        .update();
            }

            // ?????????????????????
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
            log.error("SpaceBuildServiceImpl-modifyConfId????????????????????????param???{}", JSON.toJSONString(param));
            throw BusinessException.of("?????????????????????");
        }

        SpaceBuild build = getById(buildId);
        if (build == null) {
            log.error("SpaceBuildServiceImpl-modifyConfId???????????????????????????param???{}", JSON.toJSONString(param));
            throw BusinessException.of("????????????????????????");
        }

        SpaceConf conf = confService.lambdaQuery().eq(SpaceConf::getConfId, confId).one();
        if (conf == null) {
            log.error("SpaceBuildServiceImpl-modifyConfId??????????????????????????????param???{}", JSON.toJSONString(param));
            throw BusinessException.of("???????????????????????????");
        }


        // ??????buildId??????
        lambdaUpdate() //
                .set(SpaceBuild::getConfId, confId) //
                .set(SpaceBuild::getConfUser, SecurityUtils.getUser().getSysUserDTO().getId()) //
                .set(SpaceBuild::getConfTime, new Date()) //
                .eq(SpaceBuild::getBuildId, buildId) //
                .update();

        // ParentConf <= 1??????0?????????1????????????
        seatService.lambdaUpdate()//
                .set(SpaceSeat::getConfId, confId)//
                .set(SpaceSeat::getParentConf, SpaceConstant.SpaceType.BUILD) //
                .eq(SpaceSeat::getBuildId, buildId) //
                .le(SpaceSeat::getParentConf, SpaceConstant.SpaceType.BUILD) //
                .update();

        return R.ok("???????????????");
    }


    @Override
    @Transactional
    public R<PageListVO<SpaceBuildVO>> getSpaceBuildList(QueryBuildParam param) {
        long pageSize = param.getPageSize();
        long pageNum = param.getPageNum();
        Integer bulidState = param.getBulidState();
        String buildName = param.getBuildName();
        String buildDeptId = param.getBuildDeptId();

        // ????????????
        Set<String> orgIdList = spaceUtil.getOrgIdList();

        // ????????????SpaceBuild???
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
        wrapper.orderByDesc(SpaceBuild::getCreateTime); // ?????????????????????
        Page<SpaceBuild> page = new Page<SpaceBuild>(pageNum, pageSize);
        wrapper.page(page); // ??????

        // ?????????????????????????????????
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

        // ????????????
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
            log.error("SpaceBuildServiceImpl-findOneBuild?????????????????????????????????buildId???{}", buildId);
            throw BusinessException.of("??????????????????????????????");
        }

        SpaceBuildVO vo = JSONObject.parseObject(JSON.toJSONString(build), SpaceBuildVO.class);
        vo.setBuildImage(spaceUtil.addIpAndPort(vo.getBuildImage(), prop.getIpPort()));
        vo.setBuildStateName(SpaceConstant.StateName.MAP.get(vo.getBuildState()));
        return R.ok(vo);
    }

    /**
     * ?????????--?????????????????????
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
        String endTime = param.getEndTime(); // HH:mm??????????????????????????????????????????

        Date startDate = null;
        Date endDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            startDate = sdf.parse(sDate + " " + startTime);
            endDate = sdf.parse(eDate + " " + endTime);
        } catch (Exception e) {
            log.error("SpaceBuildServiceImpl-getMobileBuildList????????????????????????param???{}", JSON.toJSONString(param));
            throw BusinessException.of("?????????????????????");
        }

        /** ???????????? */
        ConfIdVO confIdVO = spaceUtil.getConfIdListForCurrentUser(startTime, endTime, startDate, endDate, orderType);
        List<String> openList = confIdVO.getOpenList();
        List<String> freeList = confIdVO.getFreeList();


        Set<String> buildIdList = null;
        LinkedListMultimap<String, String> buildIdAndRoomTotal = LinkedListMultimap.create();

        /** ?????????????????????1???????????????????????????????????????????????????????????????2??????????????????????????????????????????????????? */
        if (closeWindow.intValue() == SpaceConstant.Switch.NO && //
                haveSocket.intValue() == SpaceConstant.Switch.NO && //
                awayToilet.intValue() == SpaceConstant.Switch.NO && //
                awayDoor.intValue() == SpaceConstant.Switch.NO) {

            /** ?????????????????????confId???????????????????????????confId */
            if (CollectionUtils.isEmpty(openList)) {
                log.debug("SpaceBuildServiceImpl-getMobileBuildList???????????????????????????-??????????????????");
                return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
            }


            /** ???????????????????????????????????????????????????????????????????????????????????? */
            List<Map<String, Object>> temList = seatService.getBaseMapper().selectMaps(new QueryWrapper<SpaceSeat>().lambda() //
                    .select(SpaceSeat::getBuildId, SpaceSeat::getRoomId) //
                    .in(SpaceSeat::getConfId, openList) //
                    .groupBy(SpaceSeat::getBuildId) //
                    .groupBy(SpaceSeat::getRoomId));
            for (Map<String, Object> map : temList) {
                buildIdAndRoomTotal.put(map.get("build_id").toString(), map.get("room_id").toString());
            }


            /** ???????????????buildId */
            buildIdList = buildIdAndRoomTotal.keySet();
            if (CollectionUtils.isEmpty(buildIdList)) {
                log.debug("SpaceBuildServiceImpl-getMobileBuildList???????????????????????????-????????????????????????");
                return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
            }
        } else {
            /** ????????????????????????confId???????????????buildIdbyTag???????????????????????????openList */
            if (CollectionUtils.isEmpty(freeList)) {
                log.debug("SpaceBuildServiceImpl-getMobileBuildList???????????????????????????-??????????????????????????????????????????");
                return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
            }


            /** ??????????????????????????????????????????????????????????????????????????????????????? */
            List<Map<String, Object>> temList = seatService.getBaseMapper().selectMaps(new QueryWrapper<SpaceSeat>().lambda() //
                    .select(SpaceSeat::getBuildId, SpaceSeat::getRoomId) //
                    .in(SpaceSeat::getConfId, freeList) //
                    .groupBy(SpaceSeat::getBuildId) //
                    .groupBy(SpaceSeat::getRoomId));
            for (Map<String, Object> map : temList) {
                buildIdAndRoomTotal.put(map.get("build_id").toString(), map.get("room_id").toString());
            }


            /** ??????????????????buildId */
            buildIdList = buildIdAndRoomTotal.keySet();
            if (CollectionUtils.isEmpty(buildIdList)) {
                log.debug("SpaceBuildServiceImpl-getMobileBuildList???????????????????????????-???????????????????????????");
                return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
            }


            /** ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? */
            if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE) {
                List<String> buildIdbyTag = spaceUtil.filterBuildIdByTag(closeWindow, haveSocket, awayToilet, awayDoor);
                if (buildIdbyTag == null) {
                    // ????????????????????????????????????
                } else if (buildIdbyTag.size() == 0) {
                    // ??????????????????????????????????????????????????????
                    buildIdList.clear();
                } else {
                    // ???????????????????????????????????????????????????????????????
                    buildIdList.retainAll(buildIdbyTag);
                }

                if (CollectionUtils.isEmpty(buildIdList)) {
                    log.debug("SpaceBuildServiceImpl-getMobileBuildList???????????????????????????-????????????????????????????????????????????????");
                    return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
                }
            }
        }



        /** ????????????buildIdList???????????? */
        Page<QueryMobileBulidList> page = pageAndOrderInMemory(param, buildIdList, SecurityUtils.getUser().getSysUserDTO().getId());
        List<QueryMobileBulidList> records = page.getRecords();
        buildIdList = records.stream().map(m -> m.getBuildId()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(buildIdList)) {
            log.debug("SpaceBuildServiceImpl-getMobileBuildList???????????????????????????-??????????????????????????????");
            return emptyPage(pageNum, pageSize, SpaceConstant.MOBILE_NO_SPACE);
        }


        /** ???????????????????????? */
        Map<String, Integer> buildIdAndSeatNotUsedTotal = new HashMap<>();
        if (!CollectionUtils.isEmpty(freeList)) {
            List<JSONObject> list = null;
            if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.SINGLE_LONG
                    || orderType.intValue() == SpaceConstant.OrderType.SINGLE_GROUP) {
                // ?????????confId?????????seat????????????seat??????????????????buildId??????????????????????????????seatId
                list = ((SpaceSeatMapper) seatService.getBaseMapper()).singleOrderForBuild(freeList, buildIdList, startDate, endDate, orderType);
                for (JSONObject map : list) {
                    buildIdAndSeatNotUsedTotal.put(map.getString("buildId"), map.getIntValue("seatNotUsedTotal"));
                }
            } else if (orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_LONG) {
                // ?????????confId?????????seat????????????seat??????????????????buildId??????????????????????????????roomId
                list = ((SpaceSeatMapper) seatService.getBaseMapper()).multiOrderForBuild(freeList, buildIdList, startDate, endDate, orderType);
                for (JSONObject map : list) {
                    buildIdAndSeatNotUsedTotal.put(map.getString("buildId"), map.getIntValue("roomNotUsedTotal"));
                }
            }
        }


        // ???????????????????????????
        for (QueryMobileBulidList vo : records) {
            vo.setBuildImage(spaceUtil.addIpAndPort(vo.getBuildImage(), prop.getIpPort()));
            Integer notUsedTotal = buildIdAndSeatNotUsedTotal.get(vo.getBuildId());
            if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.SINGLE_LONG
                    || orderType.intValue() == SpaceConstant.OrderType.SINGLE_GROUP) {

                // ????????????????????????0??????????????????
                if (vo.getBuildState().intValue() == SpaceConstant.Switch.CLOSE) {
                    vo.setSeatNotUsedTotal(0);
                } else {
                    vo.setSeatNotUsedTotal(notUsedTotal == null ? 0 : notUsedTotal);
                }

            } else if (orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_LONG) {

                // ????????????????????????0??????????????????
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
     * @param param ????????????
     * @param buildIdList ????????????????????????id
     * @param userId ??????id
     */
    private Page<QueryMobileBulidList> pageAndOrderInMemory(QueryMobileBuildParam param, Set<String> buildIdList, String userId) {
        Integer usedToGo = param.getUsedToGo();
        Integer distanceFirst = param.getDistanceFirst();
        Integer highScore = param.getHighScore();
        long pageNum = param.getPageNum();
        long pageSize = param.getPageSize();


        // ?????????????????????????????????????????????
        if (usedToGo.intValue() == SpaceConstant.Switch.NO // ???????????????
                && distanceFirst.intValue() == SpaceConstant.Switch.NO // ???????????????
                && highScore.intValue() == SpaceConstant.Switch.NO) { // ???????????????
            Page<QueryMobileBulidList> page = new Page<QueryMobileBulidList>(pageNum, pageSize);
            List<QueryMobileBulidList> records = getBaseMapper().queryMobileBulidList(page, buildIdList);
            page.setRecords(records);
            return page;
        }


        // ??????????????????id?????????????????????
        List<String> orderedBuildIdToBePage = new ArrayList<String>();

        // ??????
        if (usedToGo.intValue() == SpaceConstant.Switch.YES) {

            // ???????????????????????????????????????????????????????????????list??????
            ZSetOperations<String, Object> forZSet = redisTemplate.opsForZSet();
            Set<Object> range = forZSet.reverseRange(OrderConstant.PERSON_ORDER_BUILD + userId, 0, -1); // ?????????????????????
            List<String> usedToGoList = new ArrayList<String>();
            for (Object object : range) {
                usedToGoList.add(object.toString());
            }

            TreeSet<String> buildIdAll = new TreeSet<String>(buildIdList); // hash??????
            usedToGoList.retainAll(buildIdAll); // ??????
            buildIdAll.removeAll(usedToGoList); // ??????
            usedToGoList.addAll(buildIdAll); // ??????+??????

            orderedBuildIdToBePage = usedToGoList;
        }

        // ?????????
        else if (highScore.intValue() == SpaceConstant.Switch.YES) {
            TreeMultimap<Double, String> avgAndBuildId = TreeMultimap.create();

            HashOperations<String, Object, Object> forHash = redisTemplate.opsForHash();
            TreeSet<String> buildIdAll = new TreeSet<String>(buildIdList); // hash??????
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

            // ?????????????????????????????????
            List<String> highScoreList = Lists.newArrayList(avgAndBuildId.values());
            Collections.reverse(highScoreList);
            orderedBuildIdToBePage = highScoreList;
        }

        // ????????????
        else if (distanceFirst.intValue() == SpaceConstant.Switch.YES) {

            String coordx = param.getCoordx();
            String coordy = param.getCoordy();
            if (StringUtils.isEmpty(coordx) || StringUtils.isEmpty(coordy)) {
                throw BusinessException.of("??????????????????????????????");
            }

            // ???????????????????????????
            double userCoordx = Double.parseDouble(param.getCoordx());
            double userCoordy = Double.parseDouble(param.getCoordy());
            GlobalCoordinates gpsFrom = new GlobalCoordinates(userCoordy, userCoordx);

            // ?????????????????????
            SpaceBuildService buildService = (SpaceBuildService) AopContext.currentProxy();
            List<SpaceBuild> list = buildService.getAllBuildForMobileDistanceOrder(SecurityUtils.getUser().getTenantCode());

            // ????????????????????????????????????????????????????????????
            TreeMultimap<Double, String> map = TreeMultimap.create();
            if (CollectionUtils.isEmpty(list)) {
                orderedBuildIdToBePage = Lists.newArrayList(new TreeSet<String>(buildIdList));
            } else {
                for (SpaceBuild build : list) {
                    if (!buildIdList.contains(build.getBuildId())) {
                        continue;
                    }

                    // ???????????????????????????????????????
                    GlobalCoordinates gpsTo = new GlobalCoordinates(Double.parseDouble(build.getCoordy()), Double.parseDouble(build.getCoordx()));
                    double distanceMeter = spaceUtil.getDistanceMeter(gpsFrom, gpsTo, Ellipsoid.WGS84);
                    map.put(distanceMeter, build.getBuildId());
                }

                // ?????????????????????????????????
                List<String> distanceFirstList = Lists.newArrayList(map.values());
                Collections.reverse(distanceFirstList);
                orderedBuildIdToBePage = distanceFirstList;
            }
        }

        // ????????????
        else {
            throw BusinessException.of("???????????????");
        }


        // ??????????????????
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = fromIndex + (int) pageSize;
        if (fromIndex > orderedBuildIdToBePage.size()) { // ????????????????????????????????????
            fromIndex = orderedBuildIdToBePage.size();
        }
        if (toIndex > orderedBuildIdToBePage.size()) { // ????????????????????????????????????
            toIndex = orderedBuildIdToBePage.size();
        }


        // ????????????
        Page<QueryMobileBulidList> page = new Page<QueryMobileBulidList>(pageNum, pageSize);
        page.setTotal(orderedBuildIdToBePage.size());
        page.setPages(buildIdList.size() % pageSize == 0 ? buildIdList.size() / pageSize : (buildIdList.size() / pageSize + 1));


        List<String> subBuildIdList = orderedBuildIdToBePage.subList(fromIndex, toIndex);
        if (CollectionUtils.isEmpty(subBuildIdList)) {
            page.setRecords(new ArrayList<QueryMobileBulidList>());
        } else {
            // ????????????id??????????????????
            List<QueryMobileBulidList> pageList = getBaseMapper().queryMobileBulidListNoPage(subBuildIdList);

            // ????????????pageList????????????subBuildIdList?????????
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
     * ?????????????????????????????????????????????????????????????????????
     */
    @Override
    public void cancelOrder(List<String> idList, int spaceType) {
        seatListService.manageCancel(idList, spaceType);
        meetingService.delSpace(idList, spaceType);
        commentService.delCommentBySpace(idList, spaceType);
    }


    /**
     * ?????????????????????????????????????????????????????????????????????
     */
    @Override
    @Transactional
    public R<List<BuildMapVO>> getBuildListForMap() {
        Map<String, SpaceConfVO> map = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (MapUtils.isEmpty(map)) {
            return new R<List<BuildMapVO>>(new ArrayList<BuildMapVO>(), "???????????????????????????");
        }

        Collection<SpaceConfVO> values = map.values();
        if (CollectionUtils.isEmpty(values)) {
            return new R<List<BuildMapVO>>(new ArrayList<BuildMapVO>(), "???????????????????????????");
        }

        List<String> openList = new ArrayList<String>();
        for (SpaceConfVO conf : values) {
            String showRoom = spaceUtil.showRoom(conf, conf.getOrderType());
            if (showRoom == null) {
                openList.add(conf.getConfId());
            }
        }

        if (CollectionUtils.isEmpty(openList)) {
            return new R<List<BuildMapVO>>(new ArrayList<BuildMapVO>(), "???????????????????????????");
        }

        /** ?????????????????????????????????????????????????????????????????? */
        List<Object> buildIdObj = seatService.getBaseMapper().selectObjs(new QueryWrapper<SpaceSeat>().lambda() //
                .select(SpaceSeat::getBuildId) //
                .in(SpaceSeat::getConfId, openList) //
                .groupBy(SpaceSeat::getBuildId));
        if (CollectionUtils.isEmpty(buildIdObj)) {
            return new R<List<BuildMapVO>>(new ArrayList<BuildMapVO>(), "???????????????????????????");
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
            log.debug("SpaceBuildServiceImpl-addSpaceUseRule???????????????????????????param???{}", JSON.toJSONString(param));
            throw BusinessException.of("????????????????????????");
        }

        lambdaUpdate().set(SpaceBuild::getUseRule, useRule).eq(SpaceBuild::getBuildId, buildId).update();
        return R.ok();
    }

    @Override
    @Transactional
    public R<QueryUseRuleVO> getUseRule(String buildId) {
        SpaceBuild build = getById(buildId);
        if (build == null) {
            log.debug("SpaceBuildServiceImpl-getSpaceUseRule???????????????????????????buildId???{}", buildId);
            throw BusinessException.of("????????????????????????");
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
            log.debug("SpaceBuildServiceImpl-getSpaceUseRule???????????????????????????buildId???{}", buildId);
            throw BusinessException.of("????????????????????????");
        }

        QueryUseRuleVO vo = new QueryUseRuleVO();
        vo.setBuildId(buildId);
        vo.setUseRule(build.getUseRule());
        return R.ok(vo);
    }

    /**
     * ????????????????????????????????????????????????????????????
     */
    @Override
    public void deleteOrgCascade(List<String> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return;
        }
        lambdaUpdate().set(SpaceBuild::getBuildDeptId, null).in(SpaceBuild::getBuildDeptId, orgIds).update();
    }

    /**
     * ??????????????????????????????????????????
     */
    @Override
    @Transactional
    public R<String> editBuildStateBatch(EditBuildStateBatchVO param) {
        List<String> buildIdList = param.getBuildIdList();
        Integer buildState = param.getBuildState();
        String buildDeptId = param.getBuildDeptId();

        // ???????????????????????????
        if (buildState == null && StringUtils.isEmpty(buildDeptId)) {
            throw BusinessException.of("??????????????????????????????");
        }

        if (buildState != null && buildState != SpaceConstant.Switch.CLOSE && buildState != SpaceConstant.Switch.OPEN) {
            throw BusinessException.of("????????????????????????");
        }

        // ???????????????????????????????????????
        List<String> idList = new ArrayList<String>();

        // ????????????????????????????????????????????????????????????????????????????????????????????????id
        if (buildState != null) {
            List<SpaceBuild> oldList = lambdaQuery().in(SpaceBuild::getBuildId, buildIdList).list();
            for (SpaceBuild oldBuild : oldList) {

                // ?????????????????????????????????????????????????????????
                if (buildState.intValue() == oldBuild.getBuildState().intValue()) {
                    continue;
                }

                SpaceBuild newBuild = new SpaceBuild();
                newBuild.setBuildId(oldBuild.getBuildId());
                newBuild.setBuildState(buildState);

                // ???????????????????????????????????????????????????????????????
                changeStateByNewBuildAndOldBuild(newBuild, oldBuild);

                if (oldBuild.getBuildState().intValue() == SpaceConstant.Switch.OPEN) {
                    if (newBuild.getBuildState().intValue() == SpaceConstant.Switch.CLOSE) {
                        // ????????????????????????id
                        idList.add(oldBuild.getBuildId());
                    }
                }
            }
        }


        // ??????????????????????????????????????????????????????id
        LambdaUpdateChainWrapper<SpaceBuild> lambdaUpdate = lambdaUpdate();
        if (buildState != null) {
            lambdaUpdate.set(SpaceBuild::getBuildState, buildState);
        }
        if (StringUtils.isNotBlank(buildDeptId)) {
            lambdaUpdate.set(SpaceBuild::getBuildDeptId, buildDeptId);
        }
        lambdaUpdate.in(SpaceBuild::getBuildId, buildIdList);
        lambdaUpdate.update();


        // ?????????????????????
        if (!CollectionUtils.isEmpty(idList)) {
            cancelOrder(idList, SpaceConstant.SpaceType.BUILD);
        }

        return R.ok();
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????id?????????????????????
     */
    @Override
    @Cacheable(value = SpaceConstant.spaceBuildDistanceCache, key = "#tenantCode")
    public List<SpaceBuild> getAllBuildForMobileDistanceOrder(String tenantCode) {
        return lambdaQuery().select(SpaceBuild::getBuildId, SpaceBuild::getCoordx, SpaceBuild::getCoordy).list();
    }


}
