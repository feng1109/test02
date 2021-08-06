/**
 * Copyright (C) 2018-2020
 * All rights reserved, Designed By www.yixiang.co

 */
package com.eseasky.logging.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.logging.entity.SysLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletRequest;

/**
 */
public interface LogService extends IService<SysLog> {





    /**
     * 保存日志数据
     * @param username 用户
     * @param ip 请求IP
     * @param joinPoint /
     * @param log 日志实体
     */
    @Async
    void save(String username, String ip, ProceedingJoinPoint joinPoint, SysLog log, String uid, String tenantCode, String userNo, HttpServletRequest request);

}
