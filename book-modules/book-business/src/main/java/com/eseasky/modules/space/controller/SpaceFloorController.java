package com.eseasky.modules.space.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.service.SpaceFloorService;
import com.eseasky.modules.space.vo.SpaceFloorVO;
import com.eseasky.modules.space.vo.response.DropDownVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * <p>
 * 楼层服务
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Api(value = "楼层服务", tags = "楼层服务")
@RestController
@RequestMapping("/space/spaceFloor")
public class SpaceFloorController {

    @Autowired
    private SpaceFloorService floorService;

    @Log(value = "楼层管理：新增顶楼", type = 0)
    @ApiOperation(value = "楼层管理：新增顶楼", notes = "楼层管理：新增顶楼")
    @ApiImplicitParams({@ApiImplicitParam(name = "buildId", value = "综合楼id", dataTypeClass = String.class, required = true)})
    @PostMapping(value = "addFloor", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceFloor:addFloor')")
    public R<String> addFloor(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return floorService.addFloor(param);
    }

    @Log(value = "楼层管理：删除楼层", type = 0)
    @ApiOperation(value = "楼层管理：删除楼层", notes = "楼层管理：删除楼层")
    @GetMapping("deleteFloor")
    @PreAuthorize("hasAuthority('space:spaceFloor:deleteFloor')")
    public R<String> deleteFloor(@RequestParam String floorId) {
        return floorService.deleteFloor(floorId);
    }

    @Log(value = "楼层管理：修改楼层名称", type = 0)
    @ApiOperation(value = "楼层管理：修改楼层名称", notes = "楼层管理：修改楼层名称")
    @ApiImplicitParams({//
            @ApiImplicitParam(name = "floorId", value = "楼层id", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "floorName", value = "楼层名称", dataTypeClass = String.class, required = true)})
    @PostMapping(value = "editFloor", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceFloor:editFloor')")
    public R<String> editFloor(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return floorService.editFloor(param);
    }

    @Log(value = "楼层管理：移动楼层", type = 0)
    @ApiOperation(value = "楼层管理：移动楼层", notes = "楼层管理：移动楼层")
    @ApiImplicitParams({//
            @ApiImplicitParam(name = "floorId", value = "楼层id", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "floorMove", value = "楼层移动，1上移，2下移", dataTypeClass = Integer.class, required = true)})
    @PostMapping(value = "moveFloor", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceFloor:moveFloor')")
    public R<String> moveFloor(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return floorService.moveFloor(param);
    }

    @Log(value = "楼层管理：查询楼层列表", type = 0)
    @ApiOperation(value = "楼层管理：查询楼层列表", notes = "楼层管理：查询楼层列表")
    @GetMapping(value = "findFloorList")
    @PreAuthorize("hasAuthority('space:spaceFloor:findFloorList')")
    public R<List<SpaceFloorVO>> findFloorList(@RequestParam String buildId) {
        return floorService.findFloorList(buildId);
    }

    @Log(value = "楼层管理：根据综合楼id获取楼层下拉框", type = 0)
    @ApiOperation(value = "楼层管理：根据综合楼id获取楼层下拉框", notes = "楼层管理：根据综合楼id获取楼层下拉框")
    @GetMapping("getFloorDropDown")
    public R<List<DropDownVO>> getFloorDropDown(@RequestParam String buildId) {
        return floorService.getFloorDropDown(buildId);
    }


    @Log(value = "楼层管理：修改楼层配置规则", type = 0)
    @ApiOperation(value = "楼层管理：修改楼层配置规则", notes = "楼层管理：修改楼层配置规则")
    @ApiImplicitParams({//
            @ApiImplicitParam(name = "floorId", value = "楼层id", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "confId", value = "配置规则id", dataTypeClass = String.class, required = true)})
    @PostMapping(value = "modifyConfId", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceFloor:modifyConfId')")
    public R<String> modifyConfId(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return floorService.modifyConfId(param);
    }
}
