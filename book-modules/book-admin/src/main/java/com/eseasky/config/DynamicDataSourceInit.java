package com.eseasky.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.eseasky.common.code.utils.SpringContextUtils;
import com.eseasky.common.entity.SysTenant;
import com.eseasky.common.service.SysTenantService;
import com.eseasky.datasource.config.DynamicDataSource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态数据源初始化
 */
@Slf4j
@Configuration
public class DynamicDataSourceInit {

    @Autowired
    private SysTenantService sysTenantService;

    @Bean
    public void initDataSource() throws SQLException {
        log.info("======初始化动态数据源=====");
        DynamicDataSource dynamicDataSource =  SpringContextUtils.getBean("dynamicDataSource");
        Map<Object, Object> dataSourceMap = new HashMap<>();
        List<SysTenant> tenantList = sysTenantService.list();
        for (SysTenant sysTenant : tenantList) {
            log.info(sysTenant.toString());
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setDbType("mysql");
            druidDataSource.setName(sysTenant.getTenantCode());
            druidDataSource.setDriverClassName(sysTenant.getDatasourceDriver());
            druidDataSource.setUrl(sysTenant.getDatasourceUrl());
            druidDataSource.setUsername(sysTenant.getDatasourceUsername());
            druidDataSource.setPassword(sysTenant.getDatasourcePassword());
            druidDataSource.setMaxWait(60000);
            druidDataSource.setFilters("stat");
            druidDataSource.init();
            dataSourceMap.put(sysTenant.getTenantCode(), druidDataSource);
        }
        // 设置数据源
        dynamicDataSource.setDataSources(dataSourceMap);
        /**
         * 必须执行此操作，才会重新初始化AbstractRoutingDataSource 中的 resolvedDataSources，也只有这样，动态切换才会起效
         */
        dynamicDataSource.afterPropertiesSet();
    }
}
