package com.eseasky.modules.sys.service.impl;

import com.eseasky.common.code.fun.OrgEnvent;
import com.eseasky.modules.sys.service.OrgCascadeHandleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.eseasky.common.code.utils.SpringContextUtils.getApplicationContext;

/**
 * 组织管理级联处理实现类
 */
@Service
public class OrgCascadeHandleServiceImpl implements OrgCascadeHandleService {


    /**
     * 级联删除组织相关信息
     *
     * @param orgIds 组织id
     */
    @Transactional
    @Override
    public void deleteOrgCascade(List<String> orgIds) {
        String[] beanNamesForType = getApplicationContext().getBeanNamesForType(OrgEnvent.class);        //删除组织与用户相关信息
        for (String event : beanNamesForType) {
            OrgEnvent orgEnvent = getApplicationContext().getBean(event, OrgEnvent.class);
            orgEnvent.deleteOrgCascade(orgIds);
        }

    }
}
