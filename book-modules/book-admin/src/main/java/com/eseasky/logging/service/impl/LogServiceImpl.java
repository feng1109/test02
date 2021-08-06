/**
 * Copyright (C) 2018-2020
 * All rights reserved, Designed By www.yixiang.co

 */
package com.eseasky.logging.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.StringUtils;
import com.eseasky.datasource.config.DynamicDataSourceContextHolder;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.logging.dao.LogMapper;
import com.eseasky.logging.entity.SysLog;
import com.eseasky.logging.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static com.eseasky.common.code.utils.StringUtils.getBrowser;

/**
 */
@SuppressWarnings("unchecked")
@Slf4j
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, SysLog> implements LogService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LogServiceImpl.class);


    @Override
    @Transactional
    public void save(String username, String ip, ProceedingJoinPoint joinPoint,
                     SysLog log, String uid, String tenantCode, String userNo, HttpServletRequest request){
        try {
            DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log aopLog = method.getAnnotation(Log.class);

            // 方法路径
            String methodName = joinPoint.getTarget().getClass().getName()+"."+signature.getName()+"()";

            StringBuilder params = new StringBuilder("{");
            //参数值
            Object[] argValues = joinPoint.getArgs();
            //参数名称
            String[] argNames = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
            if(argValues != null){
                for (int i = 0; i < argValues.length; i++) {
                    params.append(" ").append(argNames[i]).append(": ").append(argValues[i]);
                }
            }
            // 描述
            if (log != null) {
                log.setDescription(aopLog.value());
            }
            //类型 0-后台 1-前台
            log.setType(aopLog.type());
            if(uid != null) {
                log.setUid(uid);
            }
            //assert log != null;
            log.setRequestIp(ip);

            String loginPath = "/user/login";
            if(loginPath.equals(signature.getName())){
                try {
                    if(argValues!=null){
                        username = new JSONObject(argValues[0]).get("username").toString();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            log.setAddress(StringUtils.getCityInfo(log.getRequestIp()));
            log.setMethod(methodName);
            log.setUsername(username);
            log.setParams(params.toString() + " }");
            log.setUserNo(userNo);
            log.setCreateTime(LocalDateTime.now());
            log.setBrowser(getBrowser(request));
            this.save(log);
        } catch (Exception e) {
            logger.error("日志保存异常",e);
            throw BusinessException.of("日志保存异常");
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }

}
