package com.eseasky.modules.sys.init;

import cn.hutool.core.collection.CollectionUtil;
import com.eseasky.book.redis.starter.template.RedisRepository;
import com.eseasky.common.entity.SysTenant;
import com.eseasky.common.service.SysTenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.eseasky.common.code.constant.RedisConstant.TENANTKEY;

@Slf4j
@Component
public class SysTenantCache implements CommandLineRunner {

    @Autowired
    SysTenantService sysTenantService;

    @Autowired
    RedisRepository redisRepository;


    @Override
    public void run(String... args) throws Exception {
        log.debug("===================开始初始化缓存SysTenant===================");
        List<SysTenant> list = sysTenantService.list();
        if (CollectionUtil.isNotEmpty(list)) {
            Map<String, SysTenant> tenantMap = list.stream().collect(Collectors.toMap(SysTenant::getTenantCode, sysTenant -> sysTenant));
            redisRepository.set(TENANTKEY, tenantMap,-1);
        }

        log.debug("===================初始化缓存SysTenant结束===================");
    }


}
