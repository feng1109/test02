/**
 * Copyright (C) 2018-2020
 * All rights reserved, Designed By www.yixiang.co
 */
package com.eseasky.logging.aspect;

import com.eseasky.common.code.entity.UserDetailsImpl;
import com.eseasky.common.code.utils.RequestHolder;
import com.eseasky.common.code.utils.StringUtils;
import com.eseasky.common.code.utils.ThrowableUtil;
import com.eseasky.logging.entity.SysLog;
import com.eseasky.logging.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static com.eseasky.common.code.utils.SecurityUtils.getUser;

/**
 * 日志记录
 */
@Component
@Aspect
@Slf4j
public class LogAspect {

    @Autowired
    LogService logService;

    ThreadLocal<Long> currentTime = new ThreadLocal<>();


    /**
     * 配置切入点
     */
    @Pointcut("@annotation(com.eseasky.common.code.annotations.Log)")
    public void logPointcut() {
        // 该方法无方法体,主要为了让同类中其他方法使用此切入点
    }

    /**
     * 配置环绕通知,使用在方法logPointcut()上注册的切入点
     *
     * @param joinPoint join point for advice
     */
    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        UserDetailsImpl userDetails = getUser();
        Object result = null;
        if (userDetails != null) {
            currentTime.set(System.currentTimeMillis());
            result = joinPoint.proceed();
            SysLog log = new SysLog("INFO", System.currentTimeMillis() - currentTime.get());
            currentTime.remove();
            String tenantCode = userDetails.getSysUserDTO().getTenantCode();
            HttpServletRequest request = StringUtils.getRequest();
            logService.save(userDetails.getSysUserDTO().getUsername(),
                    StringUtils.getIp(RequestHolder.getHttpServletRequest()), joinPoint,
                    log, getUid(), tenantCode,userDetails.getSysUserDTO().getUserNo(),request);
        }
        return result;
    }

    /**
     * 配置异常通知
     *
     * @param joinPoint join point for advice
     * @param e         exception
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        UserDetailsImpl userDetails = getUser();
        if (userDetails != null) {
            SysLog log = new SysLog("ERROR", System.currentTimeMillis() - currentTime.get());
            currentTime.remove();
            log.setExceptionDetail(ThrowableUtil.getStackTrace(e).getBytes());
            String tenantCode = userDetails.getSysUserDTO().getTenantCode();
            HttpServletRequest request = StringUtils.getRequest();
            logService.save(userDetails.getSysUserDTO().getUsername(),
                    StringUtils.getIp(RequestHolder.getHttpServletRequest()),
                    (ProceedingJoinPoint) joinPoint, log, getUid(), tenantCode,userDetails.getSysUserDTO().getUserNo(),request);
        }
    }

  /*  public String getUsername() {
        try {
            return getUser() == null ? "" : getUser().getUsername();
        } catch (Exception e) {
            return "";
        }
    }*/


  /*  public String getTenantCode() {
        try {
            return getUser() == null ? "book" : getUser().getTenantCode();
        } catch (Exception e) {
            return "";
        }
    }*/

    public String getUid() {
        try {
            return getUser() == null ? "" : getUser().getSysUserDTO().getId();
        } catch (Exception e) {
            return null;
        }
    }
}
