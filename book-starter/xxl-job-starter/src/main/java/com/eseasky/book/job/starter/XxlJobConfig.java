package com.eseasky.book.job.starter;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxj-job定时任务配置
 */
@Slf4j
@Configuration
/*@EnableConfigurationProperties(value = XxlJobProperties.class)
@ConditionalOnProperty(value = "book.xxljob.enabled", havingValue = "true", matchIfMissing = true)*/
public class XxlJobConfig {


  /*  @Autowired
    private XxlJobProperties xxlJobProperties;*/

    @Value("${book.xxljob.admin.addresses}")
    private String adminAddresses;

    @Value("${book.xxljob.executor.appName}")
    private String appName;

    @Value("${book.xxljob.executor.ip}")
    private String ip;

    @Value("${book.xxljob.executor.port}")
    private int port;

    @Value("${book.xxljob.accessToken}")
    private String accessToken;

    @Value("${book.xxljob.executor.logpath}")
    private String logPath;

    @Value("${book.xxljob.executor.logretentiondays}")
    private int logRetentionDays;

    //@Bean(initMethod = "start", destroyMethod = "destroy")
    @Bean
    @ConditionalOnClass()
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job com.eseasky.common.code.config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appName);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        return xxlJobSpringExecutor;
    }

}
