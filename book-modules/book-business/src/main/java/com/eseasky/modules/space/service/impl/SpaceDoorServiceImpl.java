package com.eseasky.modules.space.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.space.entity.SpaceBuild;
import com.eseasky.modules.space.entity.SpaceDoor;
import com.eseasky.modules.space.mapper.SpaceDoorMapper;
import com.eseasky.modules.space.mapper.SpaceFloorMapper;
import com.eseasky.modules.space.mapper.SpaceRoomMapper;
import com.eseasky.modules.space.service.SpaceBuildService;
import com.eseasky.modules.space.service.SpaceDoorService;
import com.eseasky.modules.space.service.SpaceFloorService;
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.vo.SpaceDoorVO;
import com.eseasky.modules.space.vo.request.QueryDoorParam;
import com.eseasky.modules.space.vo.response.OneBOneF;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.eseasky.modules.space.vo.response.PageListVO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 门禁服务实现类
 * </p>
 *
 * @author
 * @since 2021-04-14
 */
@Slf4j
@Service
public class SpaceDoorServiceImpl extends ServiceImpl<SpaceDoorMapper, SpaceDoor> implements SpaceDoorService {

    @Autowired
    private SpaceRoomService roomService;
    @Autowired
    private SpaceFloorService floorService;
    @Autowired
    private SpaceBuildService buildService;

    @Override
    @Transactional
    public R<String> addDoor(SpaceDoorVO spaceDoorVO) {

        SpaceDoor door = JSONObject.parseObject(JSON.toJSONString(spaceDoorVO), SpaceDoor.class);
        door.setCreateTime(new Date());
        door.setCreateUser(SecurityUtils.getUser().getSysUserDTO().getId());

        if (!save(door)) {
            log.error("SpaceDoorServiceImpl-addDoor，门禁保存失败，spaceDoorVO：{}", JSON.toJSONString(spaceDoorVO));
            throw BusinessException.of("门禁保存失败！");
        }

        return R.ok("保存成功！");
    }

    @Override
    @Transactional
    public R<String> deleteDoor(String doorId) {

        SpaceDoor oldDoor = getById(doorId);
        if (oldDoor == null) {
            log.error("SpaceDoorServiceImpl-deleteDoor，该门禁不存在，doorId：{}", doorId);
            throw BusinessException.of("该门禁不存在！");
        }

        if (!removeById(doorId)) {
            log.error("SpaceDoorServiceImpl-deleteDoor，门禁删除失败，doorId：{}", doorId);
            throw BusinessException.of("门禁删除失败！");
        }

        // TODO 日志

        return R.ok("删除成功！");
    }

    @Override
    @Transactional
    public R<String> editDoor(SpaceDoorVO spaceDoorVO) {

        SpaceDoor oldDoor = getById(spaceDoorVO.getDoorId());
        if (oldDoor == null) {
            log.error("SpaceDoorServiceImpl-editDoor，获取门禁信息失败，spaceDoorVO：{}", JSON.toJSONString(spaceDoorVO));
            throw BusinessException.of("获取门禁信息失败！");
        }

        SpaceDoor door = JSONObject.parseObject(JSON.toJSONString(spaceDoorVO), SpaceDoor.class);
        door.setCreateTime(oldDoor.getCreateTime());
        door.setCreateUser(oldDoor.getCreateUser());
        door.setUpdateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        door.setUpdateTime(new Date());

        if (!updateById(door)) {
            log.error("SpaceDoorServiceImpl-editDoor，门禁修改失败，spaceDoorVO：{}", JSON.toJSONString(spaceDoorVO));
            throw BusinessException.of("门禁修改失败！");
        }

        // TODO 日志

        return R.ok("门禁修改成功！");
    }

    @Override
    @Transactional
    public R<PageListVO<SpaceDoorVO>> findDoorList(QueryDoorParam param) {
        long pageSize = param.getPageSize();
        long pageNum = param.getPageNum();
        Integer doorState = param.getDoorState();
        String doorName = param.getDoorName();

        // 开始查询SpaceDoor表
        LambdaQueryChainWrapper<SpaceDoor> wrapper = lambdaQuery();
        if (doorState != null) {
            wrapper.eq(SpaceDoor::getDoorState, doorState);
        }
        if (!StringUtils.isEmpty(doorName)) {
            wrapper.like(SpaceDoor::getDoorName, doorName);
        }
        wrapper.orderByDesc(SpaceDoor::getCreateTime); // 创建时间，倒序
        Page<SpaceDoor> page = new Page<SpaceDoor>(pageNum, pageSize);
        wrapper.page(page); // 分页

        List<SpaceDoor> list = page.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            PageListVO<SpaceDoorVO> result = new PageListVO<SpaceDoorVO>();
            result.setList(new ArrayList<SpaceDoorVO>());
            result.setTotal(0);
            result.setPages(0);
            result.setPageSize(pageSize);
            result.setPageNum(pageNum);
            return R.ok(result);
        }

        // 循环处理，获取各种id对应的中文
        SpaceRoomMapper roomMapper = (SpaceRoomMapper) roomService.getBaseMapper();
        SpaceFloorMapper floorMapper = (SpaceFloorMapper) floorService.getBaseMapper();
        List<SpaceDoorVO> array = JSONArray.parseArray(JSON.toJSONString(list), SpaceDoorVO.class);
        for (SpaceDoorVO vo : array) {
            if (!StringUtils.isEmpty(vo.getRoomId())) {
                OneBOneFOneR one = roomMapper.getOneBuildOneFloorOneRoom(vo.getRoomId());
                if (one == null) {
                    continue;
                }
                vo.setBuildName(one.getBuildName());
                vo.setFloorName(one.getFloorName());
                vo.setRoomName(one.getRoomName());
            } else if (!StringUtils.isEmpty(vo.getFoorId())) {
                OneBOneF one = floorMapper.getOneBuildOneFloor(vo.getFoorId());
                if (one == null) {
                    continue;
                }
                vo.setBuildName(one.getBuildName());
                vo.setFloorName(one.getFloorName());
            } else if (!StringUtils.isEmpty(vo.getBuildId())) {
                SpaceBuild one = buildService.getById(vo.getBuildId());
                if (one == null) {
                    continue;
                }
                vo.setBuildName(one.getBuildName());
            }
        }

        PageListVO<SpaceDoorVO> result = new PageListVO<SpaceDoorVO>();
        result.setList(array);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return R.ok(result);
    }

    @Override
    @Transactional
    public R<SpaceDoorVO> findOneDoor(String doorId) {
        SpaceDoor door = getById(doorId);
        if (door == null) {
            log.error("SpaceDoorServiceImpl-findOneDoor，获取单个门禁失败，doorId：{}", doorId);
            throw BusinessException.of("获取单个门禁失败！");
        }

        SpaceDoorVO vo = JSONObject.parseObject(JSON.toJSONString(door), SpaceDoorVO.class);
        return R.ok(vo);
    }

}
