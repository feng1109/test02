package com.eseasky.modules.sys.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class OrgExcelVO {

    @Excel(name = "部门名称", width = 15)
    private  String  orgName;

    @Excel(name = "上级部门名称", width = 15)
    private  String  prantOrgName;
}
