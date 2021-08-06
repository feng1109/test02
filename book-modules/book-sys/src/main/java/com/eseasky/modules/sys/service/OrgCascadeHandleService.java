package com.eseasky.modules.sys.service;

import java.util.List;

public interface OrgCascadeHandleService {

    /**
     * 级联删除组织相关信息
     * @param orgIds 组织id
     */
    void deleteOrgCascade(List<String> orgIds);

}
