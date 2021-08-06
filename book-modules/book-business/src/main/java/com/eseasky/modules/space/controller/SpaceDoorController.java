package com.eseasky.modules.space.controller;


import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.service.SpaceDoorService;
import com.eseasky.modules.space.vo.SpaceDoorVO;
import com.eseasky.modules.space.vo.request.QueryDoorParam;
import com.eseasky.modules.space.vo.response.PageListVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * 门禁服务
 * </p>
 *
 * @author
 * @since 2021-04-14
 */
@Api(value = "门禁服务", tags = "门禁服务")
@RestController
@RequestMapping("/space/spaceDoor")
public class SpaceDoorController {


    @Autowired
    private SpaceDoorService doorService;

    @Log(value = "门禁：新增门禁", type = 0)
    @ApiOperation(value = "门禁：新增门禁", notes = "门禁：新增门禁")
    @PostMapping(value = "addDoor", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceDoor:addDoor')")
    public R<String> addDoor(@RequestBody @Valid SpaceDoorVO spaceDoorVO) {
        return doorService.addDoor(spaceDoorVO);
    }

    @Log(value = "门禁：删除门禁", type = 0)
    @ApiOperation(value = "门禁：删除门禁", notes = "门禁：删除门禁")
    @GetMapping("deleteDoor")
    @PreAuthorize("hasAuthority('space:spaceDoor:deleteDoor')")
    public R<String> deleteDoor(@RequestParam String doorId) {
        return doorService.deleteDoor(doorId);
    }

    @Log(value = "门禁：修改门禁", type = 0)
    @ApiOperation(value = "门禁：修改门禁", notes = "门禁：修改门禁")
    @PostMapping(value = "editDoor", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceDoor:editDoor')")
    public R<String> editDoor(@RequestBody @Valid SpaceDoorVO spaceDoorVO) {
        return doorService.editDoor(spaceDoorVO);
    }

    @Log(value = "门禁：查询单个门禁", type = 0)
    @ApiOperation(value = "门禁：查询单个门禁", notes = "门禁：查询单个门禁")
    @GetMapping("findOneDoor")
    @PreAuthorize("hasAuthority('space:spaceDoor:findOneDoor')")
    public R<SpaceDoorVO> findOneDoor(@RequestParam String doorId) {
        return doorService.findOneDoor(doorId);
    }

    @Log(value = "门禁：查询门禁列表", type = 0)
    @ApiOperation(value = "门禁：查询门禁列表", notes = "门禁：查询门禁列表")
    @PostMapping(value = "findDoorList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceDoor:findDoorList')")
    public R<PageListVO<SpaceDoorVO>> findDoorList(@RequestBody @Valid QueryDoorParam param) {
        return doorService.findDoorList(param);
    }

}

