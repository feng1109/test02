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

import com.alibaba.fastjson.JSONObject;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.service.SpaceConfService;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.request.DeleteConfUsedParam;
import com.eseasky.modules.space.vo.request.QueryConfParam;
import com.eseasky.modules.space.vo.request.QueryConfUsedParam;
import com.eseasky.modules.space.vo.response.ConfOrderTypeVO;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.PageListVO;
import com.eseasky.modules.space.vo.response.QueryConfResult;
import com.eseasky.modules.space.vo.response.QueryConfUsedVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * <p>
 * 配置规则服务
 * </p>
 *
 * @author
 * @since 2021-04-14
 */
@Api(value = "配置规则服务", tags = "配置规则服务")
@RestController
@RequestMapping("/space/spaceConf")
public class SpaceConfController {


    @Autowired
    private SpaceConfService confService;

    @Log(value = "配置规则：新增配置规则", type = 0)
    @ApiOperation(value = "配置规则：新增配置规则", notes = "配置规则：新增配置规则")
    @PostMapping(value = "addConf", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceConf:addConf')")
    public R<String> addConf(@RequestBody @Valid SpaceConfVO spaceConfVO) {
        return confService.addConf(spaceConfVO);
    }

    @Log(value = "配置规则：删除多个配置规则", type = 0)
    @ApiOperation(value = "配置规则：删多个除配置规则", notes = "配置规则：删除多个配置规则")
    @ApiImplicitParams({@ApiImplicitParam(name = "confIdList", value = "配置规则id集合", dataType = "List", required = true)})
    @PostMapping(value = "deleteConfBatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceConf:deleteConfBatch')")
    public R<String> deleteConfBatch(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return confService.deleteConfBatch(param);
    }

    @Log(value = "配置规则：修改配置规则", type = 0)
    @ApiOperation(value = "配置规则：修改配置规则", notes = "配置规则：修改配置规则")
    @PostMapping(value = "editConf", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceConf:editConf')")
    public R<String> editConf(@RequestBody @Valid SpaceConfVO spaceConfVO) {
        return confService.editConf(spaceConfVO);
    }

    @Log(value = "配置规则：查询单个配置规则", type = 0)
    @ApiOperation(value = "配置规则：查询单个配置规则", notes = "配置规则：查询单个配置规则")
    @GetMapping("findOneConf")
    @PreAuthorize("hasAuthority('space:spaceConf:findOneConf')")
    public R<SpaceConfVO> findOneConf(@RequestParam String confId) {
        return confService.findOneConf(confId);
    }

    @Log(value = "配置规则：查询配置规则列表", type = 0)
    @ApiOperation(value = "配置规则：查询配置规则列表", notes = "配置规则：查询配置规则列表")
    @PostMapping(value = "findConfList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceConf:findConfList')")
    public R<PageListVO<QueryConfResult>> findConfList(@RequestBody @Valid QueryConfParam param) {
        return confService.findConfList(param);
    }

    @Log(value = "配置规则：查询某个规则已应用列表", type = 0)
    @ApiOperation(value = "配置规则：查询某个规则已应用列表", notes = "配置规则：查询某个规则已应用列表")
    @PostMapping(value = "findConfUsedList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceConf:findConfUsedList')")
    public R<PageListVO<QueryConfUsedVO>> findConfUsedList(@RequestBody @Valid QueryConfUsedParam param) {
        return confService.findConfUsedList(param);
    }

    @Log(value = "配置规则：根据规则批量删除已应用的单位", type = 0)
    @ApiOperation(value = "配置规则：根据规则批量删除已应用的单位", notes = "配置规则：根据规则批量删除已应用的单位")
    @PostMapping(value = "deleteConfUsedList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceConf:deleteConfUsedList')")
    public R<String> deleteConfUsedList(@RequestBody List<DeleteConfUsedParam> param) {
        return confService.deleteConfUsedList(param);
    }

    @Log(value = "配置规则：查询配置规则下拉框", type = 0)
    @ApiOperation(value = "配置规则：查询配置规则下拉框", notes = "配置规则：查询配置规则下拉框")
    @GetMapping(value = "getConfDropDown")
    public R<List<DropDownVO>> getConfDropDown() {
        return confService.getConfDropDownByCurrentUser();
    }

    @Log(value = "配置规则：配置规则菜单", type = 0)
    @ApiOperation(value = "配置规则：配置规则菜单", notes = "配置规则：配置规则菜单")
    @GetMapping(value = "getConfMenuList")
    public R<ConfOrderTypeVO> getConfMenuList() {
        return confService.getConfMenuList();
    }

    @Log(value = "配置规则，测试使用：根据预约参数判断规则", type = 1)
    @ApiOperation(value = "配置规则，测试使用：根据预约参数判断规则", notes = "配置规则，测试使用：根据预约参数判断规则")
    @GetMapping(value = "judgeConf")
    public R<JSONObject> judgeConf( //
            @RequestParam("orderType") Integer orderType, //
            @RequestParam("startDate") String sDate, //
            @RequestParam("endDate") String eDate, //
            @RequestParam("startTime") String startTime, //
            @RequestParam("endTime") String endTime) {
        return confService.judgeConf(orderType, sDate, eDate, startTime, endTime);
    }

}

