package com.eseasky.modules.space.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.fun.OrgEnvent;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.space.config.SpaceConstant;
import com.eseasky.modules.space.config.SpaceUtil;
import com.eseasky.modules.space.entity.SpaceBuild;
import com.eseasky.modules.space.entity.SpaceConf;
import com.eseasky.modules.space.entity.SpaceConfMenu;
import com.eseasky.modules.space.entity.SpaceFloor;
import com.eseasky.modules.space.entity.SpaceGroup;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.mapper.SpaceConfMapper;
import com.eseasky.modules.space.mapper.SpaceFloorMapper;
import com.eseasky.modules.space.mapper.SpaceRoomMapper;
import com.eseasky.modules.space.service.SpaceBuildService;
import com.eseasky.modules.space.service.SpaceConfMenuService;
import com.eseasky.modules.space.service.SpaceConfService;
import com.eseasky.modules.space.service.SpaceFloorService;
import com.eseasky.modules.space.service.SpaceGroupService;
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.SpaceConfApproveVO;
import com.eseasky.modules.space.vo.SpaceConfDateVO;
import com.eseasky.modules.space.vo.SpaceConfDeptVO;
import com.eseasky.modules.space.vo.SpaceConfDutyVO;
import com.eseasky.modules.space.vo.SpaceConfMenuVO;
import com.eseasky.modules.space.vo.SpaceConfSignVO;
import com.eseasky.modules.space.vo.SpaceConfTimeVO;
import com.eseasky.modules.space.vo.SpaceConfUserVO;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.SpaceConfWeekVO;
import com.eseasky.modules.space.vo.request.DeleteConfUsedParam;
import com.eseasky.modules.space.vo.request.QueryConfParam;
import com.eseasky.modules.space.vo.request.QueryConfUsedParam;
import com.eseasky.modules.space.vo.response.ConfMenuTypeVO;
import com.eseasky.modules.space.vo.response.ConfMenuVO;
import com.eseasky.modules.space.vo.response.ConfOrderTypeVO;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.OneBOneF;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.eseasky.modules.space.vo.response.PageListVO;
import com.eseasky.modules.space.vo.response.ParentConfAndState;
import com.eseasky.modules.space.vo.response.QueryConfResult;
import com.eseasky.modules.space.vo.response.QueryConfUsedVO;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ???????????????????????????
 * </p>
 *
 * @author
 * @since 2021-04-14
 */
@Slf4j
@Service
public class SpaceConfServiceImpl extends ServiceImpl<SpaceConfMapper, SpaceConf> implements SpaceConfService, OrgEnvent {

    @Autowired
    private SpaceGroupService groupService;
    @Autowired
    private SpaceSeatService seatService;
    @Autowired
    private SpaceRoomService roomService;
    @Autowired
    private SpaceFloorService floorService;
    @Autowired
    private SpaceBuildService buildService;
    @Autowired
    private SpaceConfMenuService menuService;
    @Autowired
    private SpaceUtil spaceUtil;

    private final long night = 1000 * 60 * 60 * 24 - 1000;
    private String SLASH = "???";


    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceConfCache, allEntries = true)
    public R<String> addConf(SpaceConfVO spaceConfVO) {
        SpaceConf conf = voToEntity(spaceConfVO);
        Date now = new Date();
        conf.setConfId(null);
        conf.setBuiltIn(SpaceConstant.Switch.NO);
        conf.setCreateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        conf.setCreateTime(now);
        conf.setUpdateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        conf.setUpdateTime(now);
        save(conf);

        return R.ok("???????????????");
    }


    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceConfCache, allEntries = true)
    public R<String> deleteConfBatch(JSONObject param) {
        List<String> confIdList = JSONArray.parseArray(param.getJSONArray("confIdList").toJSONString(), String.class);
        if (CollectionUtils.isEmpty(confIdList)) {
            return R.error("?????????????????????");
        }

        // ?????????????????????????????????????????????
        List<QueryConfUsedVO> groupList = getBaseMapper().findConfInUsed();
        for (QueryConfUsedVO vo : groupList) {
            String confId = vo.getConfId();
            if (StringUtils.isBlank(confId)) {
                continue;
            }
            if (!confIdList.contains(confId)) {
                continue;
            }
            changeConfState(vo.getSpaceId(), vo.getSpaceType());
        }

        removeByIds(confIdList);

        return R.ok();
    }

    @Override
    @Transactional
    @CacheEvict(value = SpaceConstant.spaceConfCache, allEntries = true)
    public R<String> editConf(SpaceConfVO spaceConfVO) {

        SpaceConf oldConf = getById(spaceConfVO.getConfId());
        if (oldConf == null) {
            log.error("SpaceConfServiceImpl-editConf??????????????????????????????confId???{}", spaceConfVO.getConfId());
            throw BusinessException.of("???????????????????????????");
        }
        if (oldConf.getBuiltIn() == SpaceConstant.Switch.YES) {
            throw BusinessException.of("??????????????????????????????");
        }

        SpaceConf conf = voToEntity(spaceConfVO);
        conf.setBuiltIn(SpaceConstant.Switch.NO);
        conf.setCreateUser(oldConf.getCreateUser());
        conf.setCreateTime(oldConf.getCreateTime());
        conf.setUpdateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        conf.setUpdateTime(new Date());
        boolean updateById = updateById(conf);

        if (!updateById) {
            log.error("SpaceConfServiceImpl-editConf??????????????????????????????spaceConfVO???{}", JSON.toJSONString(spaceConfVO));
            throw BusinessException.of("???????????????????????????");
        }

        return R.ok("???????????????");
    }

    @Override
    @Transactional
    public R<SpaceConfVO> findOneConf(String confId) {
        SpaceConf conf = getById(confId);
        if (conf == null) {
            log.error("SpaceConfServiceImpl-findOneConf??????????????????????????????confId???{}", confId);
            throw BusinessException.of("???????????????????????????");
        }
        return R.ok(entityToVO(conf));
    }

    @Override
    @Transactional
    public R<PageListVO<QueryConfResult>> findConfList(QueryConfParam param) {
        long pageSize = param.getPageSize();
        long pageNum = param.getPageNum();
        Integer orderType = param.getOrderType();
        String confDeptId = param.getConfDeptId();
        String confName = param.getConfName();


        Set<String> orgIdList = spaceUtil.getOrgIdList();

        Page<QueryConfResult> page = new Page<QueryConfResult>(pageNum, pageSize);
        List<QueryConfResult> confList = getBaseMapper().findConfList(page, orderType, confDeptId, confName, orgIdList);

        if (CollectionUtils.isEmpty(confList)) {
            PageListVO<QueryConfResult> result = new PageListVO<QueryConfResult>();
            result.setList(confList);
            result.setTotal(0);
            result.setPages(0);
            result.setPageSize(pageSize);
            result.setPageNum(pageNum);
            return R.ok(result);
        }


        // ??????????????????????????????????????????????????????
        List<QueryConfUsedVO> groupList = getBaseMapper().findConfInUsed();
        LinkedListMultimap<String, String> confIdAndUseList = LinkedListMultimap.create();
        for (QueryConfUsedVO map : groupList) {
            confIdAndUseList.put(map.getConfId(), "");
        }

        // ????????????
        for (QueryConfResult conf : confList) {
            List<String> list = confIdAndUseList.get(conf.getConfId());
            conf.setInUsedCount(list.size());
            conf.setOrderTypeName(SpaceConstant.OrderTypeName.MAP.get(conf.getOrderType()));
        }

        PageListVO<QueryConfResult> result = new PageListVO<QueryConfResult>();
        result.setList(confList);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return R.ok(result);
    }

    /**
     * ??????????????????id???????????????????????????????????????
     */
    @Override
    @Transactional
    public R<PageListVO<QueryConfUsedVO>> findConfUsedList(QueryConfUsedParam param) {
        long pageSize = param.getPageSize();
        long pageNum = param.getPageNum();
        String confId = param.getConfId();
        String spaceName = param.getSpaceName();

        Page<QueryConfUsedVO> page = new Page<QueryConfUsedVO>(pageNum, pageSize);
        List<QueryConfUsedVO> usedList = getBaseMapper().findConfUsedList(page, confId, spaceName);

        for (QueryConfUsedVO vo : usedList) {
            Integer spaceType = vo.getSpaceType();
            if (spaceType == SpaceConstant.SpaceType.GROUP) {
                SpaceGroup group = groupService.getById(vo.getSpaceId());
                OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(group.getRoomId());
                if (room == null) {
                    log.error("SpaceConfServiceImpl-findConfUsedList????????????????????????????????????QueryConfUsedVO???{}", JSON.toJSONString(vo));
                    throw BusinessException.of("?????????????????????????????????");
                }
                vo.setSpaceName(room.getBuildName() + SLASH + room.getFloorName() + SLASH + room.getRoomName() + SLASH + group.getGroupName());

                // ???????????????????????????
                vo.setSpaceState(SpaceConstant.Switch.OPEN);
            } else if (spaceType == SpaceConstant.SpaceType.SEAT) {
                SpaceSeat seat = seatService.getById(vo.getSpaceId());
                if (seat == null) {
                    log.error("SpaceConfServiceImpl-findConfUsedList??????????????????????????????QueryConfUsedVO???{}", JSON.toJSONString(vo));
                    throw BusinessException.of("???????????????????????????");
                }
                OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(seat.getRoomId());
                if (room == null) {
                    log.error("SpaceConfServiceImpl-findConfUsedList??????????????????????????????QueryConfUsedVO???{}", JSON.toJSONString(vo));
                    throw BusinessException.of("???????????????????????????");
                }
                vo.setSpaceName(room.getBuildName() + SLASH + room.getFloorName() + SLASH + room.getRoomName() + SLASH + seat.getSeatNum());
                vo.setSpaceState(seat.getSeatState());
            } else if (spaceType == SpaceConstant.SpaceType.ROOM) {
                OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(vo.getSpaceId());
                if (room == null) {
                    log.error("SpaceConfServiceImpl-findConfUsedList??????????????????????????????QueryConfUsedVO???{}", JSON.toJSONString(vo));
                    throw BusinessException.of("???????????????????????????");
                }
                vo.setSpaceName(room.getBuildName() + SLASH + room.getFloorName() + SLASH + room.getRoomName());
                if (room.getRoomState() == SpaceConstant.Switch.CLOSE || room.getBuildState() == SpaceConstant.Switch.CLOSE) {
                    vo.setSpaceState(SpaceConstant.Switch.CLOSE);
                } else {
                    vo.setSpaceState(SpaceConstant.Switch.OPEN);
                }
            } else if (spaceType == SpaceConstant.SpaceType.FLOOR) {
                OneBOneF floor = ((SpaceFloorMapper) floorService.getBaseMapper()).getOneBuildOneFloor(vo.getSpaceId());
                if (floor == null) {
                    log.error("SpaceConfServiceImpl-findConfUsedList??????????????????????????????QueryConfUsedVO???{}", JSON.toJSONString(vo));
                    throw BusinessException.of("???????????????????????????");
                }
                vo.setSpaceName(floor.getBuildName() + SLASH + floor.getFloorName());
                vo.setSpaceState(floor.getBuildState());
            } else if (spaceType == SpaceConstant.SpaceType.BUILD) {
                SpaceBuild build = buildService.getById(vo.getSpaceId());
                vo.setSpaceState(build.getBuildState());
            }
            vo.setSpaceTypeName(SpaceConstant.SpaceTypeName.MAP.get(vo.getSpaceType()));
        }

        PageListVO<QueryConfUsedVO> result = new PageListVO<QueryConfUsedVO>();
        result.setList(usedList);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return R.ok(result);
    }


    @Override
    @Transactional
    public R<String> deleteConfUsedList(List<DeleteConfUsedParam> param) {
        if (CollectionUtils.isEmpty(param)) {
            return R.error("?????????????????????");
        }

        for (DeleteConfUsedParam p : param) {
            String spaceId = p.getSpaceId();
            Integer spaceType = p.getSpaceType();
            if (StringUtils.isBlank(spaceId) || spaceType == null) {
                continue;
            }

            changeConfState(spaceId, spaceType);
        }
        return R.ok();
    }

    private void changeConfState(String spaceId, Integer spaceType) {
        if (spaceType.intValue() == SpaceConstant.SpaceType.SEAT || spaceType.intValue() == SpaceConstant.SpaceType.GROUP) {

            List<SpaceSeat> list = new ArrayList<SpaceSeat>();
            if (spaceType.intValue() == SpaceConstant.SpaceType.SEAT) {
                SpaceSeat seat = seatService.getById(spaceId);
                list.add(seat);
            } else if (spaceType.intValue() == SpaceConstant.SpaceType.GROUP) {
                list = seatService.lambdaQuery().eq(SpaceSeat::getSeatGroupId, spaceId).list();
            }

            for (SpaceSeat seat : list) {
                if (seat.getSeatState() == SpaceConstant.Switch.OPEN) {
                    log.error("SpaceConfServiceImpl-changeConfState????????????????????????????????????seat???{}", JSON.toJSONString(seat));
                    throw BusinessException.of("?????????????????????????????????");
                }

                OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(seat.getRoomId());
                ParentConfAndState tem = spaceUtil.getParentProp(room);
                seatService.lambdaUpdate() //
                        .set(SpaceSeat::getConfId, tem.getConfId()) //
                        .set(SpaceSeat::getParentConf, tem.getParentConf())//
                        .set(SpaceSeat::getConfTime, null)//
                        .set(SpaceSeat::getConfUser, null)//
                        .eq(SpaceSeat::getSeatId, seat.getSeatId()) //
                        .update();
            }

            // ????????????????????????????????????
            if (spaceType.intValue() == SpaceConstant.SpaceType.GROUP) {
                groupService.deleteGroup(spaceId);
            }
        } else if (spaceType.intValue() == SpaceConstant.SpaceType.ROOM) {
            OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(spaceId);
            ParentConfAndState tem = spaceUtil.getParentProp(room);
            // ?????????????????????????????????
            if (tem.getSeatState() == SpaceConstant.Switch.OPEN) {
                log.error("SpaceConfServiceImpl-changeConfState????????????????????????????????????room???{}", JSON.toJSONString(room));
                throw BusinessException.of("?????????????????????????????????");
            }
            // ????????????confId??????
            roomService.lambdaUpdate() //
                    .set(SpaceRoom::getConfId, null) //
                    .set(SpaceRoom::getConfTime, null) //
                    .set(SpaceRoom::getConfUser, null) //
                    .eq(SpaceRoom::getRoomId, room.getRoomId()) //
                    .update();

            room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(spaceId);
            tem = spaceUtil.getParentProp(room);
            // ????????????????????????????????????????????????????????????????????????????????????
            seatService.lambdaUpdate() //
                    .set(SpaceSeat::getConfId, tem.getConfId()) //
                    .set(SpaceSeat::getParentConf, tem.getParentConf())//
                    .set(SpaceSeat::getConfTime, null) //
                    .set(SpaceSeat::getConfUser, null) //
                    .eq(SpaceSeat::getRoomId, room.getRoomId()) //
                    .eq(SpaceSeat::getParentConf, SpaceConstant.SpaceType.ROOM) //
                    .update();
        } else if (spaceType.intValue() == SpaceConstant.SpaceType.FLOOR) {
            OneBOneF floor = ((SpaceFloorMapper) floorService.getBaseMapper()).getOneBuildOneFloor(spaceId);
            ParentConfAndState tem = spaceUtil.getParentProp(floor);
            if (tem.getSeatState() == SpaceConstant.Switch.OPEN) {
                log.error("SpaceConfServiceImpl-changeConfState????????????????????????????????????floor???{}", JSON.toJSONString(floor));
                throw BusinessException.of("?????????????????????????????????");
            }
            // ????????????confId??????
            floorService.lambdaUpdate() //
                    .set(SpaceFloor::getConfId, null) //
                    .set(SpaceFloor::getConfTime, null) //
                    .set(SpaceFloor::getConfUser, null) //
                    .eq(SpaceFloor::getFloorId, floor.getFloorId()) //
                    .update();

            floor = ((SpaceFloorMapper) floorService.getBaseMapper()).getOneBuildOneFloor(spaceId);
            tem = spaceUtil.getParentProp(floor);
            // ???????????????????????????????????????????????????????????????????????????
            seatService.lambdaUpdate() //
                    .set(SpaceSeat::getConfId, tem.getConfId()) //
                    .set(SpaceSeat::getParentConf, tem.getParentConf())//
                    .set(SpaceSeat::getConfTime, null) //
                    .set(SpaceSeat::getConfUser, null) //
                    .eq(SpaceSeat::getFloorId, floor.getFloorId()) //
                    .eq(SpaceSeat::getParentConf, SpaceConstant.SpaceType.FLOOR) //
                    .update();
        } else if (spaceType.intValue() == SpaceConstant.SpaceType.BUILD) {
            SpaceBuild spaceBuild = buildService.lambdaQuery().eq(SpaceBuild::getBuildId, spaceId).one();
            if (spaceBuild.getBuildState() == SpaceConstant.Switch.OPEN) {
                log.error("SpaceConfServiceImpl-changeConfState????????????????????????????????????spaceBuild???{}", JSON.toJSONString(spaceBuild));
                throw BusinessException.of("?????????????????????????????????");
            }
            // ????????????confId??????
            buildService.lambdaUpdate() //
                    .set(SpaceBuild::getConfId, null) //
                    .set(SpaceBuild::getConfTime, null) //
                    .set(SpaceBuild::getConfUser, null) //
                    .eq(SpaceBuild::getBuildId, spaceBuild.getBuildId()) //
                    .update();
            // ????????????????????????????????????????????????????????????
            seatService.lambdaUpdate() //
                    .set(SpaceSeat::getConfId, null) //
                    .set(SpaceSeat::getParentConf, SpaceConstant.SpaceType.NONE)//
                    .set(SpaceSeat::getConfTime, null) //
                    .set(SpaceSeat::getConfUser, null) //
                    .eq(SpaceSeat::getBuildId, spaceBuild.getBuildId()) //
                    .eq(SpaceSeat::getParentConf, SpaceConstant.SpaceType.BUILD) //
                    .update();
        }
    }


    @Override
    @Transactional
    @Cacheable(value = SpaceConstant.spaceConfCache, key = "#tenantCode") // ???????????????????????????????????????conf
    public LinkedHashMap<String, SpaceConfVO> confIdAndSpaceConf(String tenantCode) {
        List<SpaceConf> list = lambdaQuery() //
                .orderByDesc(SpaceConf::getUpdateTime) //
                .orderByDesc(SpaceConf::getCreateTime) //
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        LinkedHashMap<String, SpaceConfVO> result = new LinkedHashMap<>();
        for (SpaceConf spaceConf : list) {
            SpaceConfVO vo = entityToVO(spaceConf);
            result.put(vo.getConfId(), vo);
        }

        return result;
    }

    @Override
    public R<ConfOrderTypeVO> getConfMenuList() {
        List<SpaceConfMenu> list = menuService.lambdaQuery() //
                .in(SpaceConfMenu::getOrderType, Lists.newArrayList(SpaceConstant.OrderType.SINGLE_ONCE, //
                        SpaceConstant.OrderType.SINGLE_LONG, //
                        SpaceConstant.OrderType.MULTI_ONCE, //
                        SpaceConstant.OrderType.MULTI_LONG)) //
                .list();

        LinkedListMultimap<Integer, SpaceConfMenu> map = LinkedListMultimap.create();
        for (SpaceConfMenu item : list) {
            map.put(item.getOrderType(), item);
        }

        ConfMenuTypeVO singleOnce = group(map, SpaceConstant.OrderType.SINGLE_ONCE);
        ConfMenuTypeVO singleLong = group(map, SpaceConstant.OrderType.SINGLE_LONG);
        ConfMenuTypeVO multiOnce = group(map, SpaceConstant.OrderType.MULTI_ONCE);
        ConfMenuTypeVO multiLong = group(map, SpaceConstant.OrderType.MULTI_LONG);

        ConfOrderTypeVO result = new ConfOrderTypeVO();
        result.setSingleOnce(singleOnce);
        result.setSingleLong(singleLong);
        result.setMultiOnce(multiOnce);
        result.setMultiLong(multiLong);

        return R.ok(result);
    }

    private ConfMenuTypeVO group(LinkedListMultimap<Integer, SpaceConfMenu> map, int singleonce) {

        ConfMenuTypeVO result = new ConfMenuTypeVO();
        List<ConfMenuVO> spaceList = new LinkedList<ConfMenuVO>();
        List<ConfMenuVO> orderList = new LinkedList<ConfMenuVO>();
        List<ConfMenuVO> signList = new LinkedList<ConfMenuVO>();
        result.setSpaceList(spaceList);
        result.setOrderList(orderList);
        result.setSignList(signList);

        List<SpaceConfMenu> list = map.get(singleonce);
        for (SpaceConfMenu item : list) {
            SpaceConfMenuVO menu = new SpaceConfMenuVO();
            BeanUtils.copyProperties(item, menu);
            String value = item.getValue();

            ConfMenuVO vo = new ConfMenuVO();
            vo.setValue(value);
            vo.setLabel(item.getLabel());
            vo.setMenu(menu);
            if (value.startsWith("1")) {
                spaceList.add(vo);
            } else if (value.startsWith("2")) {
                orderList.add(vo);
            } else if (value.startsWith("3")) {
                signList.add(vo);
            }
        }

        return result;
    }

    /** ???????????????????????????????????? */
    private SpaceConf voToEntity(SpaceConfVO vo) {
        // ????????????????????????
        for (SpaceConfDateVO date : vo.getDateList()) {
            if (date == null) {
                continue;
            }
            if (date.getAllowStartDate() == null || date.getAllowEndDate() == null) {
                throw BusinessException.of("????????????????????????????????????");
            }
            date.setAllowEndDate(new Date(date.getAllowEndDate().getTime() + night));
        }

        // ????????????????????????
        for (SpaceConfTimeVO time : vo.getTimeList()) {
            if (time == null) {
                continue;
            }
            if (StringUtils.isBlank(time.getAllowStartTime()) || StringUtils.isBlank(time.getAllowEndTime())) {
                throw BusinessException.of("????????????????????????????????????");
            }
            if (time.getAllowStartTime().length() != 5 || time.getAllowEndTime().length() != 5) {
                throw BusinessException.of("????????????????????????????????????");
            }
        }

        SpaceConf conf = new SpaceConf();
        BeanUtils.copyProperties(vo, conf);

        conf.setWeekList(JSON.toJSONString(vo.getWeekList() == null ? new ArrayList<>() : vo.getWeekList()));
        conf.setDateList(JSON.toJSONString(vo.getDateList() == null ? new ArrayList<>() : vo.getDateList(), SerializerFeature.WriteDateUseDateFormat));
        conf.setTimeList(JSON.toJSONString(vo.getTimeList() == null ? new ArrayList<>() : vo.getTimeList()));
        conf.setSignList(JSON.toJSONString(vo.getSignList() == null ? new ArrayList<>() : vo.getSignList()));
        conf.setDutyList(JSON.toJSONString(vo.getDutyList() == null ? new ArrayList<>() : vo.getDutyList()));
        conf.setDeptList(JSON.toJSONString(vo.getDeptList() == null ? new ArrayList<>() : vo.getDeptList()));
        conf.setUserList(JSON.toJSONString(vo.getUserList() == null ? new ArrayList<>() : vo.getUserList()));
        conf.setApproveList(JSON.toJSONString(vo.getApproveList() == null ? new ArrayList<>() : vo.getApproveList()));
        conf.setMenuList(JSON.toJSONString(vo.getMenuList() == null ? new ArrayList<>() : vo.getMenuList()));

        return conf;
    }

    private SpaceConfVO entityToVO(SpaceConf conf) {
        SpaceConfVO vo = new SpaceConfVO();
        BeanUtils.copyProperties(conf, vo);

        if (StringUtils.isNotBlank(conf.getWeekList())) {
            vo.setWeekList(JSONArray.parseArray(conf.getWeekList(), SpaceConfWeekVO.class));
        }
        if (StringUtils.isNotBlank(conf.getDateList())) {
            vo.setDateList(JSONArray.parseArray(conf.getDateList(), SpaceConfDateVO.class));
        }
        if (StringUtils.isNotBlank(conf.getTimeList())) {
            vo.setTimeList(JSONArray.parseArray(conf.getTimeList(), SpaceConfTimeVO.class));
        }
        if (StringUtils.isNotBlank(conf.getSignList())) {
            vo.setSignList(JSONArray.parseArray(conf.getSignList(), SpaceConfSignVO.class));
        }
        if (StringUtils.isNotBlank(conf.getDutyList())) {
            vo.setDutyList(JSONArray.parseArray(conf.getDutyList(), SpaceConfDutyVO.class));
        }
        if (StringUtils.isNotBlank(conf.getDeptList())) {
            vo.setDeptList(JSONArray.parseArray(conf.getDeptList(), SpaceConfDeptVO.class));
        }
        if (StringUtils.isNotBlank(conf.getUserList())) {
            vo.setUserList(JSONArray.parseArray(conf.getUserList(), SpaceConfUserVO.class));
        }
        if (StringUtils.isNotBlank(conf.getApproveList())) {
            vo.setApproveList(JSONArray.parseArray(conf.getApproveList(), SpaceConfApproveVO.class));
        }
        if (StringUtils.isNotBlank(conf.getMenuList())) {
            vo.setMenuList(JSONArray.parseArray(conf.getMenuList(), String.class));
        }

        return vo;
    }


    @Override
    @Transactional
    public R<JSONObject> judgeConf(Integer orderType, String sDate, String eDate, String startTime, String endTime) {
        Date startDate = null;
        Date endDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            startDate = sdf.parse(sDate + " " + startTime);
            endDate = sdf.parse(eDate + " " + endTime);
        } catch (Exception e) {
            throw BusinessException.of("?????????????????????");
        }

        Map<String, SpaceConfVO> confIdAndSpaceConf = confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (confIdAndSpaceConf == null || confIdAndSpaceConf.isEmpty()) {
            return null;
        }
        Collection<SpaceConfVO> allConfList = confIdAndSpaceConf.values();


        JSONObject result = new JSONObject();
        for (SpaceConfVO conf : allConfList) {

            int orderTypeToUse = orderType;

            String confName = conf.getConfName() + ":" + conf.getConfId();

            // ?????????????????????5?????????
            if (orderTypeToUse == SpaceConstant.OrderType.SINGLE_GROUP) {

                // ???????????????????????????????????????????????????
                if (conf.getOrderType().intValue() != SpaceConstant.OrderType.SINGLE_ONCE) {
                    result.put(confName, "???????????????5????????????????????????????????????????????????");
                    continue;
                }

                if (conf.getIsGroup().intValue() == SpaceConstant.Switch.YES) {
                    orderTypeToUse = SpaceConstant.OrderType.SINGLE_ONCE; // ??????????????????????????????
                } else {
                    result.put(confName, "???????????????5?????????????????????????????????????????????");
                    continue;
                }
            }
            // ?????????????????????1234???????????????????????????
            else {
                if (conf.getIsGroup().intValue() == SpaceConstant.Switch.YES) {
                    result.put(confName, "??????????????????5?????????????????????????????????????????????");
                    continue;
                }
            }

            // ??????????????????????????????????????????
            if (conf.getOrderType().intValue() != orderTypeToUse) {
                result.put(confName, "??????????????????" + orderType.intValue() + "????????????????????????" + conf.getOrderType().intValue());
                continue;
            }


            // ????????????????????????????????????????????????
            String showRoom = spaceUtil.showRoom(conf, orderTypeToUse);
            if (showRoom != null) {
                result.put(confName, "????????????????????????????????????" + showRoom);
                continue;
            }


            // ????????????????????????
            if (orderTypeToUse == SpaceConstant.OrderType.SINGLE_ONCE || orderTypeToUse == SpaceConstant.OrderType.MULTI_ONCE) {
                String judgeOpenTime = spaceUtil.judgeOpenTime(conf, startTime, endTime, startDate, endDate, orderTypeToUse);
                if (judgeOpenTime == null) {
                    result.put(confName, "??????????????????????????????" + orderTypeToUse);
                } else {
                    result.put(confName, "????????????????????????????????????" + judgeOpenTime);
                }
            } else {
                result.put(confName, "??????????????????????????????" + orderTypeToUse);
            }
        }
        return R.ok(result);
    }


    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    @Transactional
    public R<List<DropDownVO>> getConfDropDownByCurrentUser() {
        Set<String> orgIdList = spaceUtil.getOrgIdList();

        List<DropDownVO> confDropDown = getBaseMapper().getConfDropDown(orgIdList);
        return R.ok(confDropDown);
    }


    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    @CacheEvict(value = SpaceConstant.spaceConfCache, allEntries = true)
    public void deleteOrgCascade(List<String> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return;
        }
        HashSet<String> orgList = new HashSet<String>(orgIds);

        // ????????????????????????????????????
        lambdaUpdate().set(SpaceConf::getConfDeptId, null).in(SpaceConf::getConfDeptId, orgIds).update();

        // ????????????????????????????????????????????????
        List<SpaceConf> list = list();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        for (SpaceConf conf : list) {
            String deptList = conf.getDeptList();
            if (StringUtils.isBlank(deptList)) {
                continue;
            }

            // ??????oldDeptList??????????????????orgList???????????????newDeptList
            List<SpaceConfDeptVO> newDeptList = new LinkedList<SpaceConfDeptVO>();
            List<SpaceConfDeptVO> oldDeptList = JSONArray.parseArray(deptList, SpaceConfDeptVO.class);
            for (SpaceConfDeptVO deptVO : oldDeptList) {
                if (!orgList.contains(deptVO.getDeptId())) {
                    newDeptList.add(deptVO);
                }
            }

            // ????????????????????????????????????????????????
            if (oldDeptList.size() == newDeptList.size()) {
                continue;
            }

            // ?????????newDeptList?????????orgIds??????
            lambdaUpdate().set(SpaceConf::getDeptList, JSON.toJSONString(newDeptList)).eq(SpaceConf::getConfId, conf.getConfId()).update();
        }
    }

}
