package com.eseasky.constant;

/**
 * @Author YINJUN
 * @Date 2021年08月05日 11:02
 * @Description:
 */
public class IotpConstant {
    //ip
    private static final String IP = "192.168.1.253";
    //端口
    private static final int PORT = 443;
    //平台上的 API token
    public static final String API_TOKEN = "35cabaee6de616f748e86de7c57d9c27";
    //格式化地址模板
    private static final String URL_TEMPLATE = "https://%s:%d%s";
    //获取accessToken的url
    public static final String ACCESSTOKENURL = String.format(URL_TEMPLATE, IP, PORT,"/api/v1/accessToken");
    //获取应用列表的url
    public static final String GETAPPLISTURL = String.format(URL_TEMPLATE, IP, PORT,"/api/v1/application/getAppList?access_token=");
    //获取空间结构的url
    public static final String GETSPACEURL = String.format(URL_TEMPLATE, IP, PORT,"/api/v1/space/get?access_token=");
    //获取设备类型列表的url
    public static final String GETAllVIRDEVTYPEURL = String.format(URL_TEMPLATE, IP, PORT,"/api/v1/device/getAllVirDevType?access_token=");
    //获取设备类型定义详情的url
    public static final String GETVIRDEVTYPEURL = String.format(URL_TEMPLATE, IP, PORT,"/api/v1/device/getVirDevType?access_token=");
    //获取设备列表URL
    public static final String GETGROUPURL = String.format(URL_TEMPLATE, IP, PORT,"/api/v1/device/getGroup?access_token=");
    //获取设备状态URL
    public static final String GETSTATUSURL = String.format(URL_TEMPLATE, IP, PORT,"/api/v1/device/getStatus?access_token=");
    //控制设备状态URL
    public static final String SETSTATUSURL = String.format(URL_TEMPLATE, IP, PORT,"/api/v1/device/setStatus?access_token=");
}
