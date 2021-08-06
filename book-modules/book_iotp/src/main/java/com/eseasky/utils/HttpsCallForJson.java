package com.eseasky.utils;

import okhttp3.*;

import java.io.IOException;

public class HttpsCallForJson {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client = null;

    /**
     * post方式请求
     * @param url
     * @param jsonBody
     * @return
     */
    public static Response call(String url, String jsonBody) {
        if (url == null || jsonBody == null) {
            System.out.println("url or jsonBody has a null value");
            return null;
        }
        //设置post请求的请求体数据格式为JSON数据格式
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        //封装post请求，请求头为Content-Type: application/x-www-form
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type","application/x-www-form")
                .post(requestBody)
                .build();
        //https认证
        if (client == null) {
            client = UnsafeOkHttpsClient.getUnsafeOkHttpsClient();
            if (client == null) {
                System.out.println("call " + url + " fail: getUnsafeOkHttpClient");
                return null;
            }
        }
        //使用https协议执行请求，获取响应，此处不能关闭流资源，否则返回的response为close状态，后面后面不能获取response中的数据
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            System.out.println("call " + url + " has a exception: " + e.getMessage());
            return null;
        }
        return response;
    }
}
