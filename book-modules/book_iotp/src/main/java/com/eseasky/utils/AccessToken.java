package com.eseasky.utils;

import com.alibaba.fastjson.JSONObject;
import com.eseasky.constant.IotpConstant;
import com.eseasky.vo.ReAccessToken;
import okhttp3.Response;

import java.io.IOException;

/**
 * 获取accessToken
 */
public class AccessToken {
    //根据token失效时间，设置的重新获取token的时间
    private static Long validTimeDeadline = null;
    //accessToken
    private static String accessToken = null;

    /**
     * 获取accessToken
     * @return
     */
    public static String getAccessToken() {
        //判断token时间是否过期，如果没有过期则使用之前获得的token，过期了则重新获取
        if (validTimeDeadline != null && validTimeDeadline > System.currentTimeMillis()) {
            return accessToken;
        }
        //设置请求参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("api_token", IotpConstant.API_TOKEN);
        String jsonBody = jsonObject.toJSONString();
        //获取响应
        Response response = HttpsCallForJson.call(IotpConstant.ACCESSTOKENURL,jsonBody);
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
        //转换成自定义token响应实体类数据
        ReAccessToken reAccessToken;
        try {
            reAccessToken = JSONObject.parseObject(response.body().string(), ReAccessToken.class);
        } catch (IOException e) {
            System.out.println("parse body fail");
            return null;
        } finally {
            response.close();//使用完response需要关闭流资源，否则会造成资源浪费
        }
        //取出accessToken
        accessToken = reAccessToken.getAccessToken();
        //根据token的过期时间，设置需要重新获取token的时间
        validTimeDeadline = System.currentTimeMillis() + (reAccessToken.getExpiresIn() - 5) * 1000L;
        return accessToken;
    }
}
