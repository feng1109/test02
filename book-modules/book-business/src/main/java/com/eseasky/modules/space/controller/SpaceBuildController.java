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
import com.eseasky.modules.space.service.SpaceBuildService;
import com.eseasky.modules.space.vo.SpaceBuildVO;
import com.eseasky.modules.space.vo.request.EditBuildStateBatchVO;
import com.eseasky.modules.space.vo.request.QueryBuildParam;
import com.eseasky.modules.space.vo.request.QueryMobileBuildParam;
import com.eseasky.modules.space.vo.request.QueryUseRuleVO;
import com.eseasky.modules.space.vo.response.BuildListVO;
import com.eseasky.modules.space.vo.response.BuildMapVO;
import com.eseasky.modules.space.vo.response.PageListVO;
import com.eseasky.modules.space.vo.response.QueryMobileBulidList;
import com.eseasky.modules.space.vo.response.SubAdvanceDayVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * <p>
 * 综合楼服务
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Api(value = "综合楼服务", tags = "综合楼服务")
@RestController
@RequestMapping("/space/spaceBuild")
public class SpaceBuildController {

    @Autowired
    private SpaceBuildService buildService;

    @Log(value = "综合楼管理：新增综合楼", type = 0)
    @ApiOperation(value = "综合楼管理：新增综合楼", notes = "综合楼管理：新增综合楼")
    @PostMapping(value = "addBuild", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceBuild:addBuild')")
    public R<String> addBuild(@RequestBody @Valid SpaceBuildVO spaceBuildVO) {
        return buildService.addBuild(spaceBuildVO);
    }

    @Log(value = "综合楼管理：删除综合楼", type = 0)
    @ApiOperation(value = "综合楼管理：删除综合楼", notes = "综合楼管理：删除综合楼")
    @GetMapping("deleteBuild")
    @PreAuthorize("hasAuthority('space:spaceBuild:deleteBuild')")
    public R<String> deleteBuild(@RequestParam String buildId) {
        return buildService.deleteBuild(buildId);
    }

    @Log(value = "综合楼管理：删除多个综合楼", type = 0)
    @ApiOperation(value = "综合楼管理：删除多个综合楼", notes = "综合楼管理：删除多个综合楼")
    @ApiImplicitParams({@ApiImplicitParam(name = "buildIdList", value = "综合楼id集合", dataType = "List", required = true)})
    @PostMapping(value = "deleteBuildBatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceBuild:deleteBuildBatch')")
    public R<String> deleteBuildBatch(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return buildService.deleteBuildBatch(param);
    }

    @Log(value = "综合楼管理：修改综合楼", type = 0)
    @ApiOperation(value = "综合楼管理：修改综合楼", notes = "综合楼管理：修改综合楼")
    @PostMapping(value = "editBuild", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceBuild:editBuild')")
    public R<String> editBuild(@RequestBody @Valid SpaceBuildVO spaceBuildVO) {
        return buildService.editBuild(spaceBuildVO);
    }

    @Log(value = "综合楼管理：查询单个综合楼", type = 0)
    @ApiOperation(value = "综合楼管理：查询单个综合楼", notes = "综合楼管理：查询单个综合楼")
    @GetMapping("findOneBuild")
    public R<SpaceBuildVO> findOneBuild(@RequestParam String buildId) {
        return buildService.findOneBuild(buildId);
    }

    @Log(value = "综合楼管理：查询综合楼列表", type = 0)
    @ApiOperation(value = "综合楼管理：查询综合楼列表", notes = "综合楼管理：查询综合楼列表")
    @PostMapping(value = "getSpaceBuildList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceBuild:getSpaceBuildList')")
    public R<PageListVO<SpaceBuildVO>> getSpaceBuildList(@RequestBody @Valid QueryBuildParam param) {
        return buildService.getSpaceBuildList(param);
    }

    @Log(value = "综合楼管理：修改综合楼配置规则", type = 0)
    @ApiOperation(value = "综合楼管理：修改综合楼配置规则", notes = "综合楼管理：修改综合楼配置规则")
    @ApiImplicitParams({//
            @ApiImplicitParam(name = "buildId", value = "综合楼id", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "confId", value = "配置规则id", dataTypeClass = String.class, required = true)})
    @PostMapping(value = "modifyConfId", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceBuild:modifyConfId')")
    public R<String> modifyConfId(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return buildService.modifyConfId(param);
    }


    @Log(value = "综合楼管理：场馆使用规则修改", type = 0)
    @ApiOperation(value = "综合楼管理：场馆使用规则修改", notes = "综合楼管理：场馆使用规则修改")
    @PostMapping(value = "addUseRule", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceBuild:addUseRule')")
    public R<String> addUseRule(@RequestBody @Valid QueryUseRuleVO param) {
        return buildService.addUseRule(param);
    }


    @Log(value = "综合楼管理：场馆使用规则查看", type = 0)
    @ApiOperation(value = "综合楼管理：场馆使用规则查看", notes = "综合楼管理：场馆使用规则查看")
    @GetMapping(value = "getUseRule")
    @PreAuthorize("hasAuthority('space:spaceBuild:getUseRule')")
    public R<QueryUseRuleVO> getUseRule(@RequestParam("buildId") String buildId) {
        return buildService.getUseRule(buildId);
    }


    @Log(value = "综合楼管理：批量修改场馆的状态和所属组织", type = 0)
    @ApiOperation(value = "综合楼管理：批量修改场馆的状态和所属组织", notes = "综合楼管理：批量修改场馆的状态和所属组织")
    @PostMapping(value = "editBuildStateBatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceBuild:editBuildStateBatch')")
    public R<String> editBuildStateBatch(@RequestBody @Valid EditBuildStateBatchVO param) {
        return buildService.editBuildStateBatch(param);
    }


    /***************** 以下是手机端接口 *****************/

    // @Log(value = "手机预约界面：场馆选择列表", type = 1)
    @ApiOperation(value = "手机预约界面：场馆选择列表", notes = "手机预约界面：场馆选择列表")
    @PostMapping(value = "getMobileBuildList", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<BuildListVO<QueryMobileBulidList>> getMobileBuildList(@RequestBody @Valid QueryMobileBuildParam param) {
        return buildService.getMobileBuildList(param);
    }

    // @Log(value = "手机预约界面：可提前日期数组", type = 1)
    @ApiOperation(value = "手机预约界面：可提前日期数组", notes = "手机预约界面：可提前日期数组")
    @GetMapping(value = "getSubAdvanceDay")
    public R<SubAdvanceDayVO> getSubAdvanceDay() {
        return buildService.getSubAdvanceDay();
    }

    // @Log(value = "手机预约界面：为地图获取综合楼列表", type = 1)
    @ApiOperation(value = "手机预约界面：为地图获取综合楼列表", notes = "手机预约界面：为地图获取综合楼列表")
    @GetMapping(value = "getBuildListForMap")
    public R<List<BuildMapVO>> getBuildListForMap() {
        return buildService.getBuildListForMap();
    }


    // @Log(value = "手机预约界面：场馆使用规则查看", type = 0)
    @ApiOperation(value = "手机预约界面：场馆使用规则查看", notes = "手机预约界面：场馆使用规则查看")
    @GetMapping(value = "getMobileUseRule")
    public R<QueryUseRuleVO> getMobileUseRule(@RequestParam("buildId") String buildId) {
        return buildService.getMobileUseRule(buildId);
    }

}
