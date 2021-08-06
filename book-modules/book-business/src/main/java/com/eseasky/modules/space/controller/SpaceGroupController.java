package com.eseasky.modules.space.controller;


import java.util.List;

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
import com.eseasky.modules.space.service.SpaceGroupService;
import com.eseasky.modules.space.vo.SpaceGroupVO;
import com.eseasky.modules.space.vo.request.QueryGroupParam;
import com.eseasky.modules.space.vo.request.QueryMobileSeatParam;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.PageListVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * 座位分组服务
 * </p>
 *
 * @author
 * @since 2021-06-18
 */
@Api(value = "座位分组服务", tags = "座位分组服务")
@RestController
@RequestMapping("/space/spaceGroup")
public class SpaceGroupController {

    @Autowired
    private SpaceGroupService groupService;


    @Log(value = "座位分组：新增分组", type = 0)
    @ApiOperation(value = "座位分组：新增分组", notes = "座位分组：新增分组")
    @PostMapping(value = "addGroup", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceGroup:addGroup')")
    public R<String> addGroup(@RequestBody @Valid SpaceGroupVO param) {
        return groupService.addGroup(param);
    }

    @Log(value = "座位分组：修改分组", type = 0)
    @ApiOperation(value = "座位分组：修改分组", notes = "座位分组：修改分组")
    @PostMapping(value = "editGroup", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceGroup:editGroup')")
    public R<String> editGroup(@RequestBody @Valid SpaceGroupVO param) {
        return groupService.editGroup(param);
    }

    @Log(value = "座位分组：删除分组", type = 0)
    @ApiOperation(value = "座位分组：删除分组", notes = "座位分组：删除分组")
    @GetMapping(value = "deleteGroup")
    @PreAuthorize("hasAuthority('space:spaceGroup:deleteGroup')")
    public R<String> deleteGroup(@RequestParam("groupId") String groupId) {
        return groupService.deleteGroup(groupId);
    }

    @Log(value = "座位分组：获取空间分组列表", type = 0)
    @ApiOperation(value = "座位分组：获取空间分组列表", notes = "座位分组：获取空间分组列表")
    @PostMapping(value = "getSpaceGroupList")
    @PreAuthorize("hasAuthority('space:spaceGroup:getSpaceGroupList')")
    public R<PageListVO<SpaceGroupVO>> getSpaceGroupList(@RequestBody @Valid QueryGroupParam param) {
        return groupService.getSpaceGroupList(param);
    }

    @Log(value = "座位分组：获取分组页面的配置规则下拉框", type = 0)
    @ApiOperation(value = "座位分组：获取分组页面的配置规则下拉框", notes = "座位分组：获取分组页面的配置规则下拉框")
    @GetMapping(value = "getGroupConfDropDown")
    public R<List<DropDownVO>> getGroupConfDropDown() {
        return groupService.getGroupConfDropDown();
    }


    /***************** 以下是手机端接口 *****************/

    @Log(value = "座位分组：获取手机端分组列表", type = 0)
    @ApiOperation(value = "座位分组：获取手机端分组列表", notes = "座位分组：获取手机端分组列表")
    @PostMapping(value = "getMobileGroupList", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<List<SpaceGroupVO>> getMobileGroupList(@RequestBody @Valid QueryMobileSeatParam param) {
        return groupService.getMobileGroupList(param);
    }
}

