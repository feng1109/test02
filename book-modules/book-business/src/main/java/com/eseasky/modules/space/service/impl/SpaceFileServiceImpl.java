package com.eseasky.modules.space.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.ExcelUtils;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.common.entity.SysTenant;
import com.eseasky.common.service.SysTenantService;
import com.eseasky.modules.space.config.DrawQRImage;
import com.eseasky.modules.space.config.UploadProp;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.mapper.SpaceFloorMapper;
import com.eseasky.modules.space.mapper.SpaceRoomMapper;
import com.eseasky.modules.space.service.SpaceFileService;
import com.eseasky.modules.space.service.SpaceFloorService;
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.excel.ExcelOneBOneF;
import com.eseasky.modules.space.vo.excel.ExcelRoomVO;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.google.common.collect.Lists;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpMode;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SpaceFileServiceImpl implements SpaceFileService {

    @Autowired
    private DrawQRImage drawQRImage;
    @Autowired
    private SysTenantService sysTenantService;
    @Autowired
    private SpaceRoomService roomService;
    @Autowired
    private SpaceFloorService floorService;
    @Autowired
    private UploadProp prop;
    @Autowired
    private SpaceSeatService seatService;

    private final String imgType = ".png";
    private final String zipName = "QRCode.zip";
    private final String TENANT = "tenant";

    @Override
    @Transactional
    public void qrCode(String roomId, HttpServletResponse response) {
        qrCodeBatch(Lists.newArrayList(roomId), response);
    }


    @Override
    @Transactional
    public void qrCodeBatch(List<String> roomIdList, HttpServletResponse response) {
        SysTenant tenant = sysTenantService.getSysTenantByCode(SecurityUtils.getUser().getTenantCode());
        if (tenant == null) {
            log.error("SpaceRoomServiceImpl-qrCodeDownload，获取租户失败，tenant：{}", SecurityUtils.getUser().getTenantCode());
            throw BusinessException.of("获取租户失败！");
        }

        // 为房间下的每个座位创建二维码
        for (String roomId : roomIdList) {
            create(roomId, tenant);
        }

        // 将qrCodePath路径下的二维码统一打包成zip，压缩包名字自定义
        String zipPath = prop.getZipPath();// 压缩包保存路径，没有压缩包名字和后缀。对qrCodePath下的图片压缩，不能是qrCodePath子路径或同级路径。
        String qrCodePath = prop.getQrCodePath();
        File zipDest = new File(zipPath);
        File qrDest = new File(qrCodePath);
        if (!zipDest.getAbsoluteFile().exists()) {
            zipDest.getAbsoluteFile().mkdirs();
        }
        if (!qrDest.getAbsoluteFile().exists()) {
            qrDest.getAbsoluteFile().mkdirs();
        }
        File zip = ZipUtil.zip(qrCodePath, zipPath + zipName);

        ServletOutputStream outputStream = null;
        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/x-zip-compressed");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(zipName, "UTF-8"));

            outputStream = response.getOutputStream();
            IoUtil.copy(new FileInputStream(zip), outputStream);
        } catch (Exception e) {
            log.error("SpaceFileServiceImpl-qrCodeBatch，二维码下载失败！e：{}", e);
        } finally {
            // 将临时文件删除
            IoUtil.close(outputStream);
            FileUtil.del(qrCodePath);
        }
    }

    private void create(String roomId, SysTenant tenant) {

        OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(roomId);
        if (room == null) {
            log.error("SpaceRoomServiceImpl-qrCodeDownload，获取空间信息失败，roomId：{}", roomId);
            throw BusinessException.of("获取空间信息失败！");
        }


        String qrCodePath = prop.getQrCodePath();// 生成的二维码保存路径
        if (StringUtils.isBlank(qrCodePath)) {
            log.error("SpaceRoomServiceImpl-qrCodeDownload，获取二维码路径失败，qrCodePath：{}", qrCodePath);
            throw BusinessException.of("获取二维码路径失败！");
        }

        // 创建文件夹路径，此时的qrCodePath为临时文件夹，下载完毕直接删除
        qrCodePath = qrCodePath + room.getRoomName() + "/";
        File qrDest = new File(qrCodePath);
        if (!qrDest.getAbsoluteFile().exists()) {
            qrDest.getAbsoluteFile().mkdirs();
        }

        // 获取二维码属性
        int qrCodeWidth = prop.getQrCodeWidth() == 0 ? 300 : prop.getQrCodeWidth();
        int qrCodeHeight = prop.getQrCodeHeight() == 0 ? 300 : prop.getQrCodeHeight();

        // 循环创建二维码
        List<SpaceSeat> seatList = seatService.lambdaQuery() //
                .select(SpaceSeat::getSeatId, SpaceSeat::getSeatNum) //
                .eq(SpaceSeat::getRoomId, roomId) //
                .list();
        for (SpaceSeat seat : seatList) {

            // 二维码
            File qr = new File(qrCodePath + seat.getSeatNum() + imgType);

            // 生成二维码
            QrConfig config = new QrConfig(qrCodeWidth, qrCodeHeight);
            config.setMargin(0);
            QrCodeUtil.generate(JSON.toJSONString(seat), config, FileUtil.file(qr));

            // 用二维码画出图片
            try {
                drawQRImage.creat( //
                        new File(qrCodePath + seat.getSeatNum() + imgType), //
                        qr, //
                        tenant.getTenantName(), //
                        room.getBuildName() + room.getFloorName() + room.getRoomName(), //
                        seat.getSeatNum());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 批量导入房间
     */
    @Override
    @Transactional
    public R<String> roomUpload(MultipartFile file) {
        Integer titleRows = 2, headerRows = 1;
        List<ExcelRoomVO> list = null;
        try {
            list = ExcelUtils.importExcel(file.getInputStream(), titleRows, headerRows, ExcelRoomVO.class);
        } catch (IOException e) {
            log.error("SpaceFileController-roomUpload， e：{}", e);
            return R.error("解析Excel失败！");
        }

        if (CollectionUtils.isEmpty(list)) {
            return R.error("获取Excel数据失败！");
        }

        List<ExcelOneBOneF> forExcel = ((SpaceFloorMapper) floorService.getBaseMapper()).getBuildAndFloorForExcel();
        if (CollectionUtils.isEmpty(forExcel)) {
            return R.error("还未录入场馆或楼层！");
        }


        Set<String> buildNameSet = new HashSet<>();
        Map<String, ExcelOneBOneF> buildNameFloorNameAndObj = new HashMap<>();
        for (ExcelOneBOneF excel : forExcel) {
            buildNameSet.add(excel.getBuildName());
            buildNameFloorNameAndObj.put(excel.getBuildName() + excel.getFloorName(), excel);
        }


        Date now = new Date();
        String userId = SecurityUtils.getUser().getSysUserDTO().getId();
        List<SpaceRoom> roomToSave = new ArrayList<SpaceRoom>();

        long line = titleRows + headerRows;
        for (ExcelRoomVO vo : list) {
            line++;
            String buildName = vo.getBuildName();
            String floorName = vo.getFloorName();

            // 判断场馆名称
            if (StringUtils.isBlank(buildName)) {
                throw BusinessException.of("第" + line + "行，所属场馆未填写！");
            }
            if (!buildNameSet.contains(buildName)) {
                throw BusinessException.of("第" + line + "行，所属场馆不存在！");
            }

            // 判断楼层
            if (StringUtils.isBlank(floorName)) {
                throw BusinessException.of("第" + line + "行，所属楼层未填写！");
            }
            ExcelOneBOneF tem = buildNameFloorNameAndObj.get(buildName + floorName);
            if (tem == null) {
                throw BusinessException.of("第" + line + "行，所属楼层不存在！");
            }

            SpaceRoom room = new SpaceRoom();
            room.setBuildId(tem.getBuildId());
            room.setFloorId(tem.getFloorId());
            room.setCreateTime(now);
            room.setCreateUser(userId);
            room.setUpdateTime(now);
            room.setUpdateUser(userId);
            room.setRoomName(vo.getRoomName());
            room.setRoomNum(vo.getRoomNum());
            room.setArea(vo.getArea());
            roomToSave.add(room);
        }
        roomService.saveBatch(roomToSave);
        return R.ok("成功导入数据：" + roomToSave.size() + "条！");
    }


    @Override
    public R<String> imageUpload(MultipartFile file) {
        SysTenant tenant = sysTenantService.getSysTenantByCode(SecurityUtils.getUser().getTenantCode());
        if (tenant == null) {
            log.error("SpaceFileServiceImpl-imageUpload，获取租户失败，tenant：{}", SecurityUtils.getUser().getTenantCode());
            throw BusinessException.of("获取租户失败！");
        }

        if (file == null) {
            return R.error("获取图片失败！");
        }

        // 判断图片后缀
        String originalFilename = file.getOriginalFilename();
        if (!CollectionUtils.isEmpty(prop.getImageType())) {
            String substring = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if (!prop.getImageType().contains(substring)) {
                return R.error("图片格式不支持：" + substring);
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        originalFilename = sdf.format(new Date()) + "_" + originalFilename;

        // 判断上传路径
        String imagePath = prop.getImagePath();
        if (StringUtils.isEmpty(imagePath)) {
            return R.error("上传路径异常！");
        }
        // 上传到租户对应的文件
        imagePath = imagePath.replace(TENANT, tenant.getTenantCode());

        Ftp ftp = getFtp();
        try {
            // 以租户为单位动态创建文件夹
            if (!ftp.exist(imagePath)) {
                ftp.mkDirs(imagePath);
            }
            ftp.cd(imagePath);
            boolean upload = ftp.upload(imagePath, originalFilename, file.getInputStream());
            if (!upload) {
                return R.error("上传失败！");
            }
        } catch (Exception e) {
            log.error("SpaceFileServiceImpl-imageUpload，通过ftp上传文件失败！，e：{}", e);
        } finally {
            IoUtil.close(ftp);
        }

        // ip + port + 代理路径 + 图片名称
        String imageProxyPath = prop.getImageProxyPath();
        imageProxyPath = imageProxyPath.replace(TENANT, tenant.getTenantCode());
        return R.ok(prop.getIpPort() + imageProxyPath + originalFilename);
    }


    /**
     * 下载房间模板
     */
    @Override
    public R<String> downloadRoomTemplate(HttpServletResponse response) {
        SysTenant tenant = sysTenantService.getSysTenantByCode(SecurityUtils.getUser().getTenantCode());
        if (tenant == null) {
            log.error("SpaceFileServiceImpl-downloadRoomTemplate，获取租户失败，tenant：{}", SecurityUtils.getUser().getTenantCode());
            throw BusinessException.of("获取租户失败！");
        }

        String roomTemplatePath = prop.getRoomTemplatePath();
        String roomTemplateName = prop.getRoomTemplateName();
        if (StringUtils.isBlank(roomTemplatePath) || StringUtils.isBlank(roomTemplateName)) {
            return R.error("获取模板信息失败！");
        }
        roomTemplatePath = roomTemplatePath.replace(TENANT, tenant.getTenantCode());


        // 在本地新建临时文件夹，用来存放ftp下载的文件
        String temPath = "/home/temFileForRoom/";
        File temFile = new File(temPath);
        if (!temFile.getAbsoluteFile().exists()) {
            temFile.getAbsoluteFile().mkdirs();
        }


        // 从远程服务器下载到本地
        Ftp ftp = getFtp();
        try {
            boolean existFile = ftp.existFile(roomTemplatePath + roomTemplateName);
            if (!existFile) {
                return R.error("该模板还未上传到服务器！");
            }
            ftp.download(roomTemplatePath + roomTemplateName, temFile);
            temFile = new File(temPath + roomTemplateName);
        } catch (Exception e) {
            log.error("SpaceFileServiceImpl-downloadRoomTemplate，模板传输失败！，e：{}", e);
            return R.error("模板传输失败！");
        } finally {
            IoUtil.close(ftp);
        }


        // 以流的方式传输到客户端
        ServletOutputStream outputStream = null;
        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(roomTemplateName, "UTF-8"));
            response.setContentType("application/vnd.ms-excel");
            outputStream = response.getOutputStream();
            IoUtil.copy(new FileInputStream(temFile), outputStream);
        } catch (Exception e) {
            log.error("SpaceFileServiceImpl-downloadRoomTemplate，模板下载失败！，e：{}", e);
            return R.error("模板下载失败！");
        } finally {
            IoUtil.close(outputStream);
            FileUtil.del(temPath);
        }
        return null;
    }

    /** 开启ftp */
    private Ftp getFtp() {
        Ftp ftp = new Ftp(prop.getFtpIp(), prop.getFtpPort(), prop.getFtpUser(), prop.getFtpPassword());
        ftp.setMode(FtpMode.Passive);
        return ftp;
    }

}
