package com.eseasky.modules.space.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProp {

    /** ftp */
    private String ftpIp; //
    private int ftpPort; //
    private String ftpUser; //
    private String ftpPassword; //

    /** 图片上传 */
    private String ipPort;
    private String imagePath; // 上传路径
    private String imageProxyPath;
    private List<String> imageType;

    /** 二维码 */
    private String qrCodePath;
    private int qrCodeWidth;
    private int qrCodeHeight;

    /** 压缩包保存路径，没有压缩包名字和后缀。对qrCodePath下的图片压缩，不能是qrCodePath子路径或同级路径。 */
    private String zipPath;

    /** 房间模板 */
    private String roomTemplatePath;
    private String roomTemplateName;

}
