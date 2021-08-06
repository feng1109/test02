package com.eseasky.utils;

import com.alibaba.fastjson.JSONObject;
import okhttp3.Response;

import java.io.IOException;

/**
 * @Author YINJUN
 * @Date 2021年07月29日 12:25
 * @Description:其他API通用post调用方式
 */
public class OthersAPI {
    /**
     * API通用post调用方式
     * @param accessToken    accessToken
     * @param baseUrl
     * @param jsonObject    请求参数
     * @return
     */
    public static String getResponseStr(String baseUrl,String accessToken,JSONObject jsonObject){
        //将token封装进地址参数中
        String url = String.format("%s%s",baseUrl,accessToken);
        //转换成json字符串
        String jsonBody = jsonObject.toJSONString();
        //调用第三方API,获取响应结果，HttpsCallForJson中的response不能关闭流资源，否则运行会报错
        Response response = HttpsCallForJson.call(url, jsonBody);
        //对响应结果做判断
        if (response == null) {
            System.out.println("response is ull");
            return null;
        }
        if (response.code() != HttpStatus.OK.getStatusCode()) {
            System.out.println("response code is not ok:" + response.message());
            return null;
        }
        if (response.body() == null) {
            System.out.println("get response body fail: no data in body");
            return null;
        }
        //将结果转换成自定义实体类，目前没实现
        String responseStr = null;
        try {
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            response.close();//使用完response需要关闭流资源，否则会造成资源浪费
        }
        return responseStr;
    }
}
