package com.eseasky.modules.space.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.space.config.SpaceConstant;
import com.eseasky.modules.space.entity.SpaceBuild;
import com.eseasky.modules.space.entity.SpaceConf;
import com.eseasky.modules.space.entity.SpaceDesk;
import com.eseasky.modules.space.entity.SpaceFloor;
import com.eseasky.modules.space.entity.SpaceGroup;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.mapper.SpaceFloorMapper;
import com.eseasky.modules.space.service.SpaceBuildService;
import com.eseasky.modules.space.service.SpaceConfService;
import com.eseasky.modules.space.service.SpaceDeskService;
import com.eseasky.modules.space.service.SpaceFloorService;
import com.eseasky.modules.space.service.SpaceGroupService;
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.SpaceFloorVO;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 楼层服务实现类
 * </p>
 *
 * @author
 * @param <E>
 * @since 2021-04-09
 */
@Slf4j
@Service
public class SpaceFloorServiceImpl extends ServiceImpl<SpaceFloorMapper, SpaceFloor> implements SpaceFloorService {

    @Autowired
    private SpaceBuildService buildService;
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

    private String lou = "楼";

    @Override
    @Transactional
    public R<String> addFloor(JSONObject param) {

        String buildId = param.getString("buildId");
        SpaceBuild build = buildService.getById(buildId);
        if (build == null) {
            log.error("SpaceFloorServiceImpl-addFloor，获取综合楼信息失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取综合楼信息失败！");
        }

        // 最高楼层
        List<SpaceFloor> list = lambdaQuery().eq(SpaceFloor::getBuildId, buildId).orderByDesc(SpaceFloor::getFloorNum).list();
        int topFloor = 0;
        if (!CollectionUtils.isEmpty(list)) {
            topFloor = list.get(0).getFloorNum();
        }

        // 最高楼层 +1
        SpaceFloor toSave = new SpaceFloor();
        toSave.setBuildId(buildId);
        toSave.setCreateTime(new Date());
        toSave.setCreateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        toSave.setFloorNum(topFloor + 1);
        toSave.setFloorName(toSave.getFloorNum().intValue() + lou);

        if (!save(toSave)) {
            log.error("SpaceFloorServiceImpl-addFloor，楼层保存失败，toSave：{}", JSON.toJSONString(toSave));
            throw BusinessException.of("楼层保存失败！");
        }

        // 综合楼中的楼层数量+1
        boolean update = buildService.lambdaUpdate().set(SpaceBuild::getFloorCount, list.size() + 1).eq(SpaceBuild::getBuildId, buildId).update();
        if (!update) {
            throw BusinessException.of("更新楼层总数失败！");
        }

        return R.ok("保存成功！");
    }

    @Override
    @Transactional
    public R<String> deleteFloor(String floorId) {
        SpaceFloor floorToLog = getById(floorId);
        if (floorToLog == null) {
            throw BusinessException.of("该楼层不存在！");
        }

        SpaceBuild build = buildService.getById(floorToLog.getBuildId());
        if (build == null) {
            throw BusinessException.of("获取综合楼失败！");
        }

        // 逻辑删除
        removeById(floorId);
        roomService.remove(new QueryWrapper<SpaceRoom>().lambda().eq(SpaceRoom::getFloorId, floorId));
        deskService.remove(new QueryWrapper<SpaceDesk>().lambda().eq(SpaceDesk::getFloorId, floorId));
        seatService.remove(new QueryWrapper<SpaceSeat>().lambda().eq(SpaceSeat::getFloorId, floorId));
        groupService.remove(new QueryWrapper<SpaceGroup>().lambda().eq(SpaceGroup::getFloorId, floorId));

        // 更新综合楼楼层数量
        if (build.getFloorCount() != null && build.getFloorCount().intValue() > 0) {
            buildService.lambdaUpdate() //
                    .set(SpaceBuild::getFloorCount, build.getFloorCount().intValue() - 1) //
                    .eq(SpaceBuild::getBuildId, floorToLog.getBuildId()) //
                    .update();
        }


        // 根据楼层id取消订单
        buildService.cancelOrder(Lists.newArrayList(floorId), SpaceConstant.SpaceType.FLOOR);

        return R.ok("删除成功！");
    }

    @Override
    @Transactional
    public R<String> editFloor(JSONObject param) {
        String floorId = param.getString("floorId");
        String floorName = param.getString("floorName");
        if (StringUtils.isBlank(floorName)) {
            log.error("SpaceFloorServiceImpl-editFloor，请填写楼层名称，param：{}", JSON.toJSONString(param));
            return R.error("请填写楼层名称！");
        }

        SpaceFloor oldFloor = getById(floorId);
        if (oldFloor == null) {
            log.error("SpaceFloorServiceImpl-editFloor，获取楼层信息失败，param：{}", JSON.toJSONString(param));
            return R.error("获取楼层信息失败！");
        }

        boolean update = lambdaUpdate().set(SpaceFloor::getFloorName, floorName) //
                .set(SpaceFloor::getUpdateTime, new Date()) //
                .set(SpaceFloor::getUpdateUser, SecurityUtils.getUser().getSysUserDTO().getId()) //
                .eq(SpaceFloor::getFloorId, floorId) //
                .update();

        if (!update) {
            log.error("SpaceFloorServiceImpl-editFloor，楼层修改失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("楼层修改失败！");
        }
        return R.ok("修改成功！");
    }


    @Override
    @Transactional
    public R<String> modifyConfId(JSONObject param) {
        String floorId = param.getString("floorId");
        String confId = param.getString("confId");
        if (StringUtils.isBlank(floorId) || StringUtils.isBlank(confId)) {
            log.error("SpaceFloorServiceImpl-modifyConfId，获取参数失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取参数失败！");
        }

        SpaceFloor floor = getById(floorId);
        if (floor == null) {
            log.error("SpaceFloorServiceImpl-modifyConfId，获取楼层失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取楼层失败！");
        }

        SpaceConf conf = confService.lambdaQuery().eq(SpaceConf::getConfId, confId).one();
        if (conf == null) {
            log.error("SpaceFloorServiceImpl-modifyConfId，获取配置规则失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取配置规则失败！");
        }

        // 根据floorId更新
        lambdaUpdate() //
                .set(SpaceFloor::getConfId, confId) //
                .set(SpaceFloor::getConfUser, SecurityUtils.getUser().getSysUserDTO().getId()) //
                .set(SpaceFloor::getConfTime, new Date()) //
                .eq(SpaceFloor::getFloorId, floorId) //
                .update();


        // ParentConf <= 2，【0没有，1综合楼，2楼层】
        seatService.lambdaUpdate()//
                .set(SpaceSeat::getConfId, confId)//
                .set(SpaceSeat::getParentConf, SpaceConstant.SpaceType.FLOOR) //
                .eq(SpaceSeat::getFloorId, floorId) //
                .le(SpaceSeat::getParentConf, SpaceConstant.SpaceType.FLOOR) //
                .update();

        return R.ok("修改成功！");
    }


    @Override
    @Transactional
    public R<List<SpaceFloorVO>> findFloorList(String buildId) {
        List<SpaceFloor> list = lambdaQuery().eq(SpaceFloor::getBuildId, buildId).orderByDesc(SpaceFloor::getFloorNum).list();
        if (CollectionUtils.isEmpty(list)) {
            return R.ok(new ArrayList<SpaceFloorVO>());
        }

        for (SpaceFloor spaceFloor : list) {
            List<SpaceRoom> roomList = roomService.lambdaQuery() //
                    .eq(SpaceRoom::getFloorId, spaceFloor.getFloorId()) //
                    .orderByDesc(SpaceRoom::getRoomNum) //
                    .list();
            spaceFloor.setRoomList(roomList);
        }

        List<SpaceFloorVO> result = JSONArray.parseArray(JSON.toJSONString(list), SpaceFloorVO.class);
        return R.ok(result);
    }

    @Override
    @Transactional
    public boolean createFloorByBuildId(Integer floorCount, String buildId, Date now) {
        List<SpaceFloor> toSave = new ArrayList<SpaceFloor>();
        for (int i = 0; i < floorCount; i++) {
            SpaceFloor floor = new SpaceFloor();
            floor.setBuildId(buildId);
            floor.setFloorNum(i + 1);
            floor.setFloorName(i + 1 + lou);
            floor.setCreateTime(now);
            floor.setCreateUser(SecurityUtils.getUser().getSysUserDTO().getId());
            toSave.add(floor);
        }
        return saveBatch(toSave);
    }

    @Override
    @Transactional
    public R<List<DropDownVO>> getFloorDropDown(String buildId) {
        List<SpaceFloor> list = lambdaQuery().eq(SpaceFloor::getBuildId, buildId).orderByAsc(SpaceFloor::getFloorNum).list();
        List<DropDownVO> array = new ArrayList<DropDownVO>();
        for (SpaceFloor spaceFloor : list) {
            DropDownVO vo = new DropDownVO();
            vo.setValue(spaceFloor.getFloorId());
            vo.setLabel(spaceFloor.getFloorName());
            array.add(vo);
        }
        return R.ok(array);
    }

    @Override
    @Transactional
    public R<String> moveFloor(JSONObject param) {
        String floorId = param.getString("floorId");
        int floorMove = param.getIntValue("floorMove");

        SpaceFloor floor = getById(floorId);
        if (floor == null) {
            log.error("SpaceFloorServiceImpl-moveFloor，获取楼层信息失败，floorId：{}", floorId);
            throw BusinessException.of("获取楼层信息失败！");
        }


        SpaceFloor nextFloor = null;
        if (floorMove == SpaceConstant.FloorMove.UP) {
            // 因为是上移，楼号正序
            List<SpaceFloor> allList = lambdaQuery() //
                    .eq(SpaceFloor::getBuildId, floor.getBuildId()) //
                    .orderByAsc(SpaceFloor::getFloorNum) //
                    .list();

            String nextFloorId = null;
            for (SpaceFloor f : allList) {
                if ("0".equals(nextFloorId)) {
                    nextFloorId = f.getFloorId();
                    nextFloor = f;
                    break;
                }
                if (floorId.equals(f.getFloorId())) {
                    nextFloorId = "0";
                }
            }

            if (nextFloor == null) {
                return R.error("此楼层无法上移");
            }
        } else if (floorMove == SpaceConstant.FloorMove.DOWN) {
            // 因为是下移，楼号倒序
            List<SpaceFloor> allList = lambdaQuery() //
                    .eq(SpaceFloor::getBuildId, floor.getBuildId()) //
                    .orderByDesc(SpaceFloor::getFloorNum) //
                    .list();

            String nextFloorId = null;
            for (SpaceFloor f : allList) {
                if ("0".equals(nextFloorId)) {
                    nextFloorId = f.getFloorId();
                    nextFloor = f;
                    break;
                }
                if (floorId.equals(f.getFloorId())) {
                    nextFloorId = "0";
                }
            }

            if (nextFloor == null) {
                return R.error("此楼层无法下移");
            }
        } else {
            log.error("SpaceFloorServiceImpl-moveFloor，移动状态错误，floorMove：{}", floorMove);
            throw BusinessException.of("移动状态错误！");
        }

        // 互换楼层号
        lambdaUpdate() //
                .set(SpaceFloor::getFloorNum, nextFloor.getFloorNum()) //
                .eq(SpaceFloor::getFloorId, floor.getFloorId()) //
                .update();
        lambdaUpdate() //
                .set(SpaceFloor::getFloorNum, floor.getFloorNum()) //
                .eq(SpaceFloor::getFloorId, nextFloor.getFloorId()) //
                .update();

        return R.ok();
    }

}
