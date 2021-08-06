package com.eseasky.modules.space.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.service.SpaceFileService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * <p>
 * 上传下载服务
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Api(value = "上传下载服务", tags = "上传下载服务")
@RestController
@RequestMapping("/space")
public class SpaceFileController {

    @Autowired
    private SpaceFileService fileService;

    @Log(value = "图片上传", type = 0)
    @ApiOperation(value = "图片上传", notes = "图片上传")
    @PostMapping(value = "/image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> imageUpload(@RequestParam("file") MultipartFile file) throws Exception {
        return fileService.imageUpload(file);
    }

    @Log(value = "下载单个房间二维码", type = 0)
    @ApiOperation(value = "下载单个房间二维码", notes = "下载单个房间二维码")
    @ApiImplicitParams({@ApiImplicitParam(name = "roomId", value = "房间id", dataTypeClass = String.class, required = true)})
    @PostMapping(value = "/qrCode/download", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:qrCode:download')")
    public void qrCode(@RequestBody @ApiParam(hidden = true) JSONObject param, HttpServletResponse response) {
        String roomId = param.getString("roomId");
        fileService.qrCode(roomId, response);
    }

    @Log(value = "下载多个房间二维码", type = 0)
    @ApiOperation(value = "下载多个房间二维码", notes = "下载多个房间二维码")
    @ApiImplicitParams({@ApiImplicitParam(name = "roomIdList", value = "房间id集合", dataTypeClass = List.class, required = true)})
    @PostMapping(value = "/qrCodeBatch/download", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:qrCodeBatch:download')")
    public void qrCodeBatch(@RequestBody @ApiParam(hidden = true) JSONObject param, HttpServletResponse response) {
        List<String> roomIdList = JSONArray.parseArray(param.getJSONArray("roomIdList").toJSONString(), String.class);
        fileService.qrCodeBatch(roomIdList, response);
    }

    @Log(value = "批量上传房间信息", type = 0)
    @ApiOperation(value = "批量上传房间信息", notes = "批量上传房间信息")
    @PostMapping(value = "/room/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('space:room:upload')")
    public R<String> roomUpload(@RequestParam("file") MultipartFile file) {
        return fileService.roomUpload(file);
    }

    @Log(value = "下载房间批量导入模板", type = 0)
    @ApiOperation(value = "下载房间批量导入模板", notes = "下载房间批量导入模板")
    @PostMapping("/room/downloadRoomTemplate")
    @PreAuthorize("hasAuthority('space:room:downloadRoomTemplate')")
    public R<String> downloadRoomTemplate(HttpServletResponse response) {
        return fileService.downloadRoomTemplate(response);
    }

}
