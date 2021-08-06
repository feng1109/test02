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
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.vo.SpaceFloorVO;
import com.eseasky.modules.space.vo.SpaceRoomVO;
import com.eseasky.modules.space.vo.request.EditRoomStateBatchVO;
import com.eseasky.modules.space.vo.request.ImportRoomImageVO;
import com.eseasky.modules.space.vo.request.QueryMobileRoomParam;
import com.eseasky.modules.space.vo.request.QueryRoomParam;
import com.eseasky.modules.space.vo.request.QueryStatisticsRoomParam;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.eseasky.modules.space.vo.response.PageListVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * <p>
 * 房间服务
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Api(value = "房间服务", tags = "房间服务")
@RestController
@RequestMapping("/space/spaceRoom")
public class SpaceRoomController {

    @Autowired
    private SpaceRoomService roomService;

    @Log(value = "房间管理：新增房间", type = 0)
    @ApiOperation(value = "房间管理：新增房间", notes = "房间管理：新增房间")
    @PostMapping(value = "addRoom", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceRoom:addRoom')")
    public R<String> addRoom(@RequestBody @Valid SpaceRoomVO spaceRoomVO) {
        return roomService.addRoom(spaceRoomVO);
    }

    @Log(value = "房间管理：删除房间", type = 0)
    @ApiOperation(value = "房间管理：删除房间", notes = "房间管理：删除房间")
    @GetMapping("deleteRoom")
    @PreAuthorize("hasAuthority('space:spaceRoom:deleteRoom')")
    public R<String> deleteRoom(@RequestParam String roomId) {
        return roomService.deleteRoom(roomId);
    }

    @Log(value = "房间管理：删除多个房间", type = 0)
    @ApiOperation(value = "房间管理：删除多个房间", notes = "房间管理：删除多个房间")
    @ApiImplicitParams({@ApiImplicitParam(name = "roomIdList", value = "房间id集合", dataType = "List", required = true)})
    @PostMapping(value = "deleteRoomBatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceRoom:deleteRoomBatch')")
    public R<String> deleteRoomBatch(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return roomService.deleteRoomBatch(param);
    }

    @Log(value = "房间管理：修改房间", type = 0)
    @ApiOperation(value = "房间管理：修改房间", notes = "房间管理：修改房间")
    @PostMapping(value = "editRoom", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceRoom:editRoom')")
    public R<String> editRoom(@RequestBody @Valid SpaceRoomVO spaceRoomVO) {
        return roomService.editRoom(spaceRoomVO);
    }

    @Log(value = "房间管理：查询单个房间", type = 0)
    @ApiOperation(value = "房间管理：查询单个房间", notes = "房间管理：查询单个房间")
    @GetMapping("findOneRoom")
    @PreAuthorize("hasAuthority('space:spaceRoom:findOneRoom')")
    public R<SpaceRoomVO> findOneRoom(@RequestParam String roomId) {
        return roomService.findOneRoom(roomId);
    }

    @Log(value = "房间管理：查询房间列表", type = 0)
    @ApiOperation(value = "房间管理：查询房间列表", notes = "房间管理：查询房间列表")
    @PostMapping(value = "getSpaceRoomList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceRoom:getSpaceRoomList')")
    public R<PageListVO<OneBOneFOneR>> getSpaceRoomList(@RequestBody @Valid QueryRoomParam param) {
        return roomService.getSpaceRoomList(param);
    }

    @Log(value = "房间管理：修改房间配置规则", type = 0)
    @ApiOperation(value = "房间管理：修改房间配置规则", notes = "房间管理：修改房间配置规则")
    @ApiImplicitParams({//
            @ApiImplicitParam(name = "roomId", value = "房间id", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "confId", value = "配置规则id", dataTypeClass = String.class, required = true)})
    @PostMapping(value = "modifyConfId", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceRoom:modifyConfId')")
    public R<String> modifyConfId(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return roomService.modifyConfId(param);
    }

    @Log(value = "房间管理：批量修改房间的状态", type = 0)
    @ApiOperation(value = "房间管理：批量修改房间的状态", notes = "房间管理：批量修改房间的状态")
    @PostMapping(value = "editRoomStateBatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceRoom:editRoomStateBatch')")
    public R<String> editRoomStateBatch(@RequestBody @Valid EditRoomStateBatchVO param) {
        return roomService.editRoomStateBatch(param);
    }

    @Log(value = "房间管理：为房间导入空间平面图", type = 0)
    @ApiOperation(value = "房间管理：为房间导入空间平面图", notes = "房间管理：为房间导入空间平面图")
    @PostMapping(value = "importRoomImage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceRoom:importRoomImage')")
    public R<String> importRoomImage(@RequestBody @Valid ImportRoomImageVO param) {
        return roomService.importRoomImage(param);
    }

    /***************** 统计中心接口 *****************/

    @Log(value = "统计中心：房间展示列表", type = 0)
    @ApiOperation(value = "统计中心：房间展示列表", notes = "统计中心：房间展示列表")
    @PostMapping(value = "getStatisticRoomList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceRoom:getStatisticRoomList')")
    public R<PageListVO<SpaceRoomVO>> getStatisticRoomList(@RequestBody @Valid QueryStatisticsRoomParam param) {
        return roomService.getStatisticRoomList(param);
    }

    @Log(value = "统计中心：综合楼和楼层联动下拉框", type = 0)
    @ApiOperation(value = "统计中心：综合楼和楼层联动下拉框", notes = "统计中心：综合楼和楼层联动下拉框")
    @GetMapping(value = "getStatisticDropDown")
    public R<List<DropDownVO>> getStatisticDropDown() {
        return roomService.getStatisticDropDown();
    }

    /***************** 手机端接口 *****************/

    // @Log(value = "手机预约界面：房间选择列表", type = 1)
    @ApiOperation(value = "手机预约界面：房间选择列表", notes = "手机预约界面：房间选择列表")
    @PostMapping(value = "getMobileRoomList", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<List<SpaceFloorVO>> getMobileRoomList(@RequestBody @Valid QueryMobileRoomParam param) {
        return roomService.getMobileRoomList(param);
    }

    // @Log(value = "手机预约界面：获取房间信息", type = 1)
    @ApiOperation(value = "手机预约界面：获取房间信息", notes = "手机预约界面：获取房间信息")
    @GetMapping("getOneRoom")
    public R<OneBOneFOneR> getOneRoom(@RequestParam String roomId) {
        return roomService.getOneRoom(roomId);
    }

}
