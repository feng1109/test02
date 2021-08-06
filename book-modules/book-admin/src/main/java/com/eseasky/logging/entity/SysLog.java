/**
 * Copyright (C) 2018-2020
 * All rights reserved, Designed By www.yixiang.co

 */
package com.eseasky.logging.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_log")
@NoArgsConstructor
public class SysLog extends Model<SysLog> {

    @TableId
    private Long id;

    /** 操作用户 */
    private String username;

    @TableField(exist = false)
    private String nickname;

    /** 描述 */
    private String description;

    /** 方法名 */
    private String method;

    private String uid;

    private Integer type;

    /** 参数 */
    private String params;

    /** 日志类型 */
    private String logType;

    /** 请求ip */
    private String requestIp;

    /** 地址 */
    private String address;

    /** 浏览器  */
    private String browser;

    private String userNo;

    /** 请求耗时 */
    private Long time;

    /** 异常详细  */
    private byte[] exceptionDetail;


    private LocalDateTime createTime;


    public SysLog(String logType, Long time) {
        this.logType = logType;
        this.time = time;
    }
}
