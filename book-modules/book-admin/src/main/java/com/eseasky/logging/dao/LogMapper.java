/**
 * Copyright (C) 2018-2020
 * All rights reserved, Designed By www.yixiang.co

 */
package com.eseasky.logging.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eseasky.logging.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface LogMapper extends BaseMapper<SysLog> {

}
