package com.eseasky.modules.sys.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class SysOrgExcel {

    @Excel(name = "部门名称", width = 15)
    private String name;

    @Excel(name = "上级部门名称", width = 15)
    private String parentName;

    private String id;

    private String pid;

}
