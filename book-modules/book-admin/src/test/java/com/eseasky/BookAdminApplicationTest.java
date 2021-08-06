package com.eseasky;

import com.alibaba.fastjson.JSONObject;
import com.eseasky.common.code.fun.OrgEnvent;
import com.eseasky.common.entity.SysOrg;
import com.eseasky.common.entity.SysRole;
import com.eseasky.common.entity.SysRoleOrgData;
import com.eseasky.common.entity.SysUser;
import com.eseasky.common.service.SysOrgService;
import com.eseasky.common.service.SysRoleOrgDataService;
import com.eseasky.common.service.SysUserService;
import com.eseasky.datasource.config.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static com.eseasky.common.code.utils.SpringContextUtils.getApplicationContext;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class BookAdminApplicationTest {

    @Autowired
    SysUserService sysUserService;


    public static int clientTotal = 20;

    public static int threadTotal = 100;

    public static int count = 0;

    @Autowired
    SysRoleOrgDataService sysRoleOrgDataService;

    @Test
    public void beanTest(){
        String[] beanNamesForType = getApplicationContext().getBeanNamesForType(OrgEnvent.class);
        for(String s:beanNamesForType){
            OrgEnvent orgEnvent=  getApplicationContext().getBean(s,OrgEnvent.class);
            orgEnvent.deleteOrgCascade(null);
            //System.out.println(s);
        }
    }


    @Test
    public void test(){
        DynamicDataSourceContextHolder.setDataSourceKey("whu");
        SysUser sysUser = sysUserService.findByUserName("admin");
        System.out.println(JSONObject.toJSONString(sysUser));
        DynamicDataSourceContextHolder.clearDataSourceKey();
    }


    public void concurrencyTest() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Semaphore semaphore = new Semaphore(threadTotal);
        CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();

                    semaphore.release();
                } catch (Exception e) {
                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        System.out.println(count);



    }

    @Autowired
    SysOrgService sysOrgService;

    @Test
    public void roleorgTest(){
        DynamicDataSourceContextHolder.setDataSourceKey("whu");
        List<SysOrg> list = sysOrgService.list();
        for(SysOrg sysOrg:list){
            SysRoleOrgData sysRoleOrgData = new SysRoleOrgData();
            sysRoleOrgData.setRoleId("1");
            sysRoleOrgData.setOrgId(sysOrg.getId());
            sysRoleOrgDataService.save(sysRoleOrgData);
        }

        DynamicDataSourceContextHolder.clearDataSourceKey();
    }








}
