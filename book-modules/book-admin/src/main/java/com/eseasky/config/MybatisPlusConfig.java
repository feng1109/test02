package com.eseasky.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.eseasky.datasource.config.DynamicDataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * MyBatisPlus配置
 */
@EnableTransactionManagement
@Configuration
@MapperScan({"com.eseasky.*.dao","com.eseasky.modules.*.mapper","com.eseasky.common.security.mapper"})
public class MybatisPlusConfig {

    @Autowired
    SurveyMetaObjectHandle surveyMetaObjectHandle;

    @Bean("book")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource book() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean("dynamicDataSource")
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("book", book());
        // 将 master 数据源作为默认指定的数据源
        dynamicDataSource.setDefaultDataSource(book());
        // 将 master 和 slave 数据源作为指定的数据源
        dynamicDataSource.setDataSources(dataSourceMap);
        return dynamicDataSource;
    }

    @Bean
    public MybatisSqlSessionFactoryBean sqlSessionFactoryBean() throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        /**
         * 重点，使分页插件生效
         */
        Interceptor[] plugins = new Interceptor[1];
        plugins[0] = mybatisPlusInterceptor();
        sessionFactory.setPlugins(plugins);
        //配置数据源，此处配置为关键配置，如果没有将 dynamicDataSource作为数据源则不能实现切换
        sessionFactory.setDataSource(dynamicDataSource());
        // 扫描Model
        sessionFactory.setTypeAliasesPackage("com.eseasky.modules.*.entity,com.eseasky.common.code.entity,com.eseasky.common.security.entity," +
                "com.eseasky.modules.order.*");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // 扫描映射文件
        sessionFactory.setMapperLocations(resolver.getResources("classpath*:mapper/*.xml"));
        sessionFactory.setGlobalConfig(globalConfig());
        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        // 配置事务管理, 使用事务时在方法头部添加@Transactional注解即可
        return new DataSourceTransactionManager(dynamicDataSource());
    }

   /* @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();

        List<ISqlParser> sqlParserList = new ArrayList<>();
        // 攻击 SQL 阻断解析器、加入解析链
        sqlParserList.add(new BlockAttackSqlParser());
        paginationInterceptor.setSqlParserList(sqlParserList);
        return paginationInterceptor;
    }*/



    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
    
   /* @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.setUseDeprecatedExecutor(false);
            // 驼峰命名
            configuration.setObjectWrapperFactory(new MybatisMapWrapperFactory());
        };
    }*/

    @Bean
    public GlobalConfig globalConfig(){
        GlobalConfig globalConfig = new GlobalConfig();
        SurveyMetaObjectHandle surveyMetaObjectHandle = new SurveyMetaObjectHandle();
        globalConfig.setMetaObjectHandler(surveyMetaObjectHandle);
        return globalConfig;
    }



}
