package com.eseasky.modules.space.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.space.config.SpaceConstant;
import com.eseasky.modules.space.config.SpaceUtil;
import com.eseasky.modules.space.entity.SpaceConf;
import com.eseasky.modules.space.entity.SpaceGroup;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.mapper.SpaceGroupMapper;
import com.eseasky.modules.space.mapper.SpaceRoomMapper;
import com.eseasky.modules.space.mapper.SpaceSeatMapper;
import com.eseasky.modules.space.service.SpaceBuildService;
import com.eseasky.modules.space.service.SpaceConfService;
import com.eseasky.modules.space.service.SpaceGroupService;
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.SpaceGroupVO;
import com.eseasky.modules.space.vo.request.QueryGroupParam;
import com.eseasky.modules.space.vo.request.QueryMobileSeatParam;
import com.eseasky.modules.space.vo.request.SeatIdAndSeatNumVO;
import com.eseasky.modules.space.vo.response.ConfIdVO;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.eseasky.modules.space.vo.response.PageListVO;
import com.eseasky.modules.space.vo.response.ParentConfAndState;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ???????????????????????????
 * </p>
 *
 * @author
 * @since 2021-06-18
 */
@Slf4j
@Service
public class SpaceGroupServiceImpl extends ServiceImpl<SpaceGroupMapper, SpaceGroup> implements SpaceGroupService {

    @Autowired
    private SpaceSeatService seatService;
    @Autowired
    private SpaceRoomService roomService;
    @Autowired
    private SpaceBuildService buildService;

    @Autowired
    private SpaceConfService confService;
    @Autowired
    private SpaceUtil spaceUtil;

    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceGroupCache, allEntries = true)
    public R<String> addGroup(SpaceGroupVO param) {
        SpaceRoom room = roomService.getById(param.getRoomId());
        if (room == null) {
            log.error("SpaceGroupServiceImpl-addGroup????????????????????????param???{}", JSON.toJSONString(param));
            throw BusinessException.of("?????????????????????");
        }

        // ????????????
        SpaceGroup group = JSONObject.parseObject(JSON.toJSONString(param), SpaceGroup.class);
        Date now = new Date();
        group.setGroupId(null);
        group.setFloorId(room.getFloorId());
        group.setBuildId(room.getBuildId());
        group.setConfUser(SecurityUtils.getUser().getSysUserDTO().getId());
        group.setConfTime(now);
        group.setCreateTime(now);
        group.setCreateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        group.setUpdateTime(now);
        group.setUpdateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        save(group);

        List<SeatIdAndSeatNumVO> seatList = param.getSeatList();
        if (!CollectionUtils.isEmpty(seatList)) {
            List<String> seatIdList = seatList.stream().map(m -> m.getSeatId()).collect(Collectors.toList());
            List<SpaceSeat> list = seatService.lambdaQuery().in(SpaceSeat::getSeatId, seatIdList).list();
            for (SpaceSeat seat : list) {
                if (StringUtils.isNotBlank(seat.getConfId())) {
                    if (seat.getParentState() == SpaceConstant.SpaceType.SEAT) {
                        log.error("SpaceGroupServiceImpl-addGroup???????????????-???????????????????????????seat???{}", JSON.toJSONString(seat));
                        throw BusinessException.of(seat.getSeatNum() + "?????????????????????");
                    } else if (seat.getParentState() == SpaceConstant.SpaceType.GROUP) {
                        log.error("SpaceGroupServiceImpl-addGroup???????????????-???????????????????????????seat???{}", JSON.toJSONString(seat));
                        throw BusinessException.of(seat.getSeatNum() + "?????????????????????");
                    }
                }
            }

            // ?????????????????????id
            seatService.lambdaUpdate() //
                    .set(SpaceSeat::getSeatGroupId, group.getGroupId()) //
                    .set(SpaceSeat::getConfId, group.getConfId()) //
                    .set(SpaceSeat::getParentConf, SpaceConstant.SpaceType.GROUP) //
                    .in(SpaceSeat::getSeatId, seatIdList) //
                    .update();
        }


        return R.ok();
    }

    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceGroupCache, allEntries = true)
    public R<String> editGroup(SpaceGroupVO param) {

        // ????????????????????????2?????????
        List<SeatIdAndSeatNumVO> seatList = param.getSeatList();
        if (seatList.size() < 2) {
            throw BusinessException.of("????????????????????????????????????");
        }

        SpaceGroup oldGroup = getById(param.getGroupId());
        if (oldGroup == null) {
            log.error("SpaceGroupServiceImpl-editGroup??????????????????????????????param???{}", JSON.toJSONString(param));
            throw BusinessException.of("???????????????????????????");
        }

        // ?????????????????????????????????
        Date now = new Date();
        lambdaUpdate() //
                .set(SpaceGroup::getUpdateTime, now) //
                .set(SpaceGroup::getUpdateUser, SecurityUtils.getUser().getSysUserDTO().getId()) //
                .set(SpaceGroup::getConfUser, SecurityUtils.getUser().getSysUserDTO().getId()) //
                .set(SpaceGroup::getConfTime, now) //
                .set(SpaceGroup::getGroupName, param.getGroupName()) //
                .set(SpaceGroup::getConfId, param.getConfId()) //
                .eq(SpaceGroup::getGroupId, param.getGroupId()) //
                .update();


        // ?????????groupId???????????????????????????id???conf???parentConf??????
        seatService.lambdaUpdate() //
                .set(SpaceSeat::getSeatGroupId, "") //
                .set(SpaceSeat::getConfId, null) //
                .set(SpaceSeat::getParentConf, SpaceConstant.SpaceType.NONE) // ?????????
                .eq(SpaceSeat::getSeatGroupId, param.getGroupId()) //
                .update();

        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
        List<String> seatIdList = seatList.stream().map(m -> m.getSeatId()).collect(Collectors.toList());
        seatIdList = seatIdList.stream().filter(m -> StringUtils.isNotBlank(m)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(seatIdList)) {
            List<SpaceSeat> list = seatService.lambdaQuery().in(SpaceSeat::getSeatId, seatIdList).list();
            for (SpaceSeat seat : list) {
                if (StringUtils.isNotBlank(seat.getSeatGroupId())) {
                    log.error("SpaceGroupServiceImpl-editGroup???????????????-?????????????????????seat???{}", JSON.toJSONString(seat));
                    throw BusinessException.of(seat.getSeatNum() + "???????????????");
                }
            }

            // ?????????????????????id
            seatService.lambdaUpdate() //
                    .set(SpaceSeat::getSeatGroupId, param.getGroupId()) //
                    .set(SpaceSeat::getConfId, param.getConfId()) //
                    .set(SpaceSeat::getParentConf, SpaceConstant.SpaceType.GROUP) //
                    .in(SpaceSeat::getSeatId, seatIdList) //
                    .update();
        }
        return R.ok();
    }

    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceGroupCache, allEntries = true)
    public R<String> deleteGroup(String groupId) {
        SpaceGroup group = getById(groupId);
        if (group == null) {
            log.error("SpaceGroupServiceImpl-deleteGroup??????????????????????????????groupId???{}", groupId);
            throw BusinessException.of("???????????????????????????");
        }

        boolean removeById = removeById(groupId);
        if (!removeById) {
            log.error("SpaceGroupServiceImpl-editGroup????????????????????????groupId???{}", groupId);
            throw BusinessException.of("?????????????????????");
        }

        OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(group.getRoomId());
        ParentConfAndState confAndState = spaceUtil.getParentProp(room);
        String confId = confAndState.getConfId();
        Integer parentConf = confAndState.getParentConf();

        seatService.lambdaUpdate()//
                .set(SpaceSeat::getSeatGroupId, "") //
                .set(SpaceSeat::getConfTime, null)//
                .set(SpaceSeat::getConfUser, null)//
                .set(SpaceSeat::getConfId, confId)//
                .set(SpaceSeat::getParentConf, parentConf) //
                .eq(SpaceSeat::getSeatGroupId, groupId) //
                .update();

        buildService.cancelOrder(Lists.newArrayList(groupId), SpaceConstant.SpaceType.GROUP);

        return R.ok();
    }


    @Override
    @Transactional
    @Cacheable(value = SpaceConstant.spaceGroupCache, key = "#roomId") // roomId???????????????????????????group
    public Map<String, SpaceGroupVO> groupIdAndSpaceGroupVO(String roomId) {
        List<SpaceGroup> list = list();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        Map<String, SpaceGroupVO> result = new HashMap<>();
        for (SpaceGroup group : list) {
            SpaceGroupVO vo = new SpaceGroupVO();
            vo.setConfId(group.getConfId());
            vo.setGroupId(group.getGroupId());
            vo.setGroupName(group.getGroupName());
            vo.setRoomId(group.getRoomId());
            result.put(group.getGroupId(), vo);
        }
        return result;
    }


    /**
     * ??????????????????????????????????????????????????????????????????????????????
     */
    @Override
    @Transactional
    public R<List<DropDownVO>> getGroupConfDropDown() {
        Set<String> orgIdList = spaceUtil.getOrgIdList();

        // ???????????????????????????????????????
        LambdaQueryChainWrapper<SpaceConf> wrapper = confService.lambdaQuery();
        wrapper.select(SpaceConf::getConfId, SpaceConf::getConfName);
        if (!CollectionUtils.isEmpty(orgIdList)) {
            wrapper.nested(i -> i.in(SpaceConf::getConfDeptId, orgIdList) //
                    .or().eq(SpaceConf::getConfDeptId, "") //
                    .or().isNull(SpaceConf::getConfDeptId));
        }
        wrapper.eq(SpaceConf::getIsGroup, SpaceConstant.Switch.YES);
        wrapper.orderByDesc(SpaceConf::getCreateTime);
        List<SpaceConf> list = wrapper.list();

        List<DropDownVO> result = new ArrayList<DropDownVO>();
        for (SpaceConf conf : list) {
            DropDownVO vo = new DropDownVO();
            vo.setValue(conf.getConfId());
            vo.setLabel(conf.getConfName());
            result.add(vo);
        }
        return R.ok(result);
    }


    @Override
    @Transactional
    public R<PageListVO<SpaceGroupVO>> getSpaceGroupList(QueryGroupParam param) {
        long pageSize = param.getPageSize();
        long pageNum = param.getPageNum();
        String roomId = param.getRoomId();

        // ??????
        Page<SpaceGroup> page = new Page<SpaceGroup>(pageNum, pageSize);
        Page<SpaceGroup> pageResult = lambdaQuery() //
                .eq(SpaceGroup::getRoomId, roomId) //
                .orderByDesc(SpaceGroup::getCreateTime) //
                .page(page);
        List<SpaceGroup> groupList = pageResult.getRecords();
        if (CollectionUtils.isEmpty(groupList)) {
            PageListVO<SpaceGroupVO> result = new PageListVO<SpaceGroupVO>();
            result.setList(new ArrayList<SpaceGroupVO>());
            result.setTotal(0);
            result.setPages(0);
            result.setPageSize(pageSize);
            result.setPageNum(pageNum);
            return R.ok(result);
        }


        // ?????????????????????????????????????????????????????????
        Set<String> groupIdSet = groupList.stream().map(m -> m.getGroupId()).collect(Collectors.toSet());
        List<SpaceSeat> seatList = seatService.lambdaQuery() //
                .select(SpaceSeat::getSeatId, SpaceSeat::getSeatNum, SpaceSeat::getSeatGroupId) //
                .eq(SpaceSeat::getRoomId, roomId) //
                .in(SpaceSeat::getSeatGroupId, groupIdSet) //
                .orderByAsc(SpaceSeat::getSeatNum) //
                .list();

        // ?????????????????????????????????
        ArrayListMultimap<String, SeatIdAndSeatNumVO> seatIdGroup = ArrayListMultimap.create();
        ArrayListMultimap<String, String> seatNumGroup = ArrayListMultimap.create();
        if (!CollectionUtils.isEmpty(seatList)) {
            for (SpaceSeat seat : seatList) {
                seatIdGroup.put(seat.getSeatGroupId(), new SeatIdAndSeatNumVO(seat.getSeatId(), seat.getSeatNum()));
                seatNumGroup.put(seat.getSeatGroupId(), seat.getSeatNum());
            }
        }


        List<SpaceGroupVO> array = JSONArray.parseArray(JSON.toJSONString(groupList), SpaceGroupVO.class);
        for (SpaceGroupVO vo : array) {
            vo.setSeatList(seatIdGroup.get(vo.getGroupId()));
            vo.setSeatNumList(seatNumGroup.get(vo.getGroupId()));
        }

        PageListVO<SpaceGroupVO> result = new PageListVO<SpaceGroupVO>();
        result.setList(array);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return R.ok(result);
    }

    @Override
    @Transactional
    public R<List<SpaceGroupVO>> getMobileGroupList(QueryMobileSeatParam param) {

        String roomId = param.getRoomId();
        Integer orderType = param.getOrderType();
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
            log.error("SpaceBuildServiceImpl-getMobileGroupList????????????????????????param???{}", JSON.toJSONString(param));
            throw BusinessException.of("?????????????????????");
        }


        // ???????????????????????????confIdList
        ConfIdVO confIdVO = spaceUtil.getConfIdListForCurrentUser(startTime, endTime, startDate, endDate, orderType);
        if (CollectionUtils.isEmpty(confIdVO.getFreeList())) {
            log.error("SpaceBuildServiceImpl-getMobileGroupList?????????FreeList");
            return R.ok(new ArrayList<SpaceGroupVO>());
        }

        List<SpaceSeat> list = ((SpaceSeatMapper) seatService.getBaseMapper()).groupOrderForSeat(confIdVO.getFreeList(), roomId, startDate, endDate);
        if (CollectionUtils.isEmpty(list)) {
            log.error("SpaceBuildServiceImpl-getMobileGroupList??????FreeList??????????????????FreeList???{}", JSON.toJSONString(confIdVO.getFreeList()));
            return R.ok(new ArrayList<SpaceGroupVO>());
        }

        ArrayListMultimap<String, SeatIdAndSeatNumVO> seatIdGroup = ArrayListMultimap.create();
        ArrayListMultimap<String, String> seatNumGroup = ArrayListMultimap.create();
        for (SpaceSeat seat : list) {
            if (seat.getSeatState() == SpaceConstant.Switch.CLOSE) {
                continue;
            }
            seatIdGroup.put(seat.getSeatGroupId(), new SeatIdAndSeatNumVO(seat.getSeatId(), seat.getSeatNum()));
            seatNumGroup.put(seat.getSeatGroupId(), seat.getSeatNum());
        }

        List<String> groupIdList = list.stream().map(m -> m.getSeatGroupId()).distinct().collect(Collectors.toList());
        List<SpaceGroup> groupList = lambdaQuery() //
                .eq(SpaceGroup::getRoomId, roomId) //
                .in(SpaceGroup::getGroupId, groupIdList) //
                .orderByDesc(SpaceGroup::getCreateTime) //
                .list();

        List<SpaceGroupVO> array = JSONArray.parseArray(JSON.toJSONString(groupList), SpaceGroupVO.class);
        for (SpaceGroupVO vo : array) {
            vo.setSeatNumList(seatNumGroup.get(vo.getGroupId()));
            vo.setSeatList(seatIdGroup.get(vo.getGroupId()));
        }

        return R.ok(array);
    }

}
