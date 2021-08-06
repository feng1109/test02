package com.eseasky.modules.order.config;


import jodd.util.StringUtil;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @describe: redisson配置类
 * @title: RedissonConfig
 * @Author lc
 * @Date: 2021/4/26
 */
@Data
@Configuration
public class RedissonConfig {

    public static Redisson redisson;
    protected static Config config;


    @Value("${spring.redis.host}")
    private   String host;

    @Value("${spring.redis.port}")
    private  String port;

    @Value("${spring.redis.database}")
    private  Integer  dataBase;

    @Value("${spring.redis.password}")
    private  String password;


    @Value("${spring.profiles.active}")
    private String activeProfile;


    @Bean
    public Redisson getRedisson(){
        config = new Config();
        if (activeProfile.equals("mydev")) {
            config.useSingleServer().setAddress("redis://" + host + ":" + port).setDatabase(dataBase);
        }else {
            SingleServerConfig singleServerConfig = config.useSingleServer();
            singleServerConfig.setAddress("redis://" + host + ":" + port).setDatabase(dataBase);
            if(StringUtil.isNotEmpty(password)){
                singleServerConfig.setPassword(password);
            }
        }

        redisson= (Redisson)Redisson.create(config);
        return redisson;
    }


}