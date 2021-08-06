package com.eseasky.codegen.code;

import com.eseasky.codegen.generate.impl.CodeGenerateOneToMany;
import com.eseasky.codegen.generate.pojo.onetomany.MainTableVo;
import com.eseasky.codegen.generate.pojo.onetomany.SubTableVo;

import java.util.ArrayList;
import java.util.List;

/**
 *代码生成器入口【一对多】
 */
public class OneToMany {


    /**
     * 一对多(父子表)数据模型，生成方法
     * @param args
     */
    public static void main(String[] args) {
        //第一步：设置主表配置
        MainTableVo mainTable = new MainTableVo();
        mainTable.setTableName("t_yw_fees_xm_price_detail");//表名
        mainTable.setEntityName("FeesXm");	 //实体名
        mainTable.setEntityPackage("fee");	 //包名
        mainTable.setFtlDescription("费项表");	 //描述

        //第二步：设置子表集合配置
        List<SubTableVo> subTables = new ArrayList<SubTableVo>();
        //[1].子表一
        SubTableVo po = new SubTableVo();
        po.setTableName("t_yw_fees_xm_detail");//表名
        po.setEntityName("FeesXmPrice");	    //实体名
        po.setEntityPackage("fee");	        //包名
        po.setFtlDescription("费项价格明细");       //描述
        //子表外键参数配置
        /*说明:
         * db_type) 子表引用主表主键ID作为外键，外键字段必须以_ID结尾;
         * diver_name) 主表和子表的外键字段名字，必须相同（除主键ID外）;
         * db_url) 多个外键字段，采用逗号分隔;
         */
        po.setForeignKeys(new String[]{"fee_id"});
        subTables.add(po);
        //[2].子表二
        SubTableVo po2 = new SubTableVo();
        po2.setTableName("t_yw_fees");		//表名
        po2.setEntityName("Fees");			//实体名
        po2.setEntityPackage("fee"); 				//包名
        po2.setFtlDescription("用水性质");			//描述
        //子表外键参数配置
        /*说明:
         * db_type) 子表引用主表主键ID作为外键，外键字段必须以_ID结尾;
         * diver_name) 主表和子表的外键字段名字，必须相同（除主键ID外）;
         * db_url) 多个外键字段，采用逗号分隔;
         */
        po2.setForeignKeys(new String[]{"fee_id"});
        subTables.add(po2);
        mainTable.setSubTables(subTables);

        //第三步：一对多(父子表)数据模型,代码生成
        try {
            new CodeGenerateOneToMany(mainTable,subTables).generateCodeFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
