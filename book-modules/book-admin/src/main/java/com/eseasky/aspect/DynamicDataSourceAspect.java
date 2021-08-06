package com.eseasky.aspect;

import com.eseasky.common.code.entity.UserDetailsImpl;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.datasource.config.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

/**
 * 动态数据源切面拦截
 *
 */
@Slf4j
@Aspect
@Component
//@Order(1) // 请注意：这里order一定要小于tx:annotation-driven的order，即先执行DynamicDataSourceAspectAdvice切面，再执行事务切面，才能获取到最终的数据源
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class DynamicDataSourceAspect {

    @Around("execution(* com.eseasky.modules.*.controller.*.*(..))||execution(* com.eseasky.common.security.controller.*.*(..))")
    public Object doAround(ProceedingJoinPoint jp) throws Throwable {
        Object result = null;
        try {
            UserDetailsImpl user = SecurityUtils.getUser();
            if(user!=null){
                String tenantCode = user.getTenantCode();
                log.info("当前租户Id:{}", tenantCode);
                if (!StringUtils.isEmpty(tenantCode)) {
                    DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
                }
            }
            result = jp.proceed();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
            //result =  R.error("系统异常");
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
        return result;
    }

}
