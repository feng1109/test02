package com.eseasky.codegen.code;

import cn.hutool.core.io.IoUtil;
import com.eseasky.codegen.GenUtils;
import com.eseasky.codegen.entity.GenConfig;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * 单表生成
 */
public class GenOne {


    //表名称
    private static final String tableName = "order_approval";
    //包名(可为空，系统加载默认配置)
    private static final String packageName = "com.eseasky";
    //模块(可为空，系统加载默认配置)
    private static final String moduleName = "common";
    //作者(可为空，系统加载默认配置)
    private static final String author = "";
    //注释(可为空，系统加载默认配置)
    private static final String comments = "";
    //表前缀(可为空，系统加载默认配置)
    private static final String tablePrefix = "";

    public static void main(String[] args) throws Exception{
        GenConfig genConfig = new GenConfig();
        genConfig.setTableName(tableName);
        genConfig.setPackageName(packageName);
        genConfig.setAuthor(author);
        genConfig.setModuleName(moduleName);
        genConfig.setTablePrefix(tablePrefix);
        genConfig.setComments(comments);
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\JeecgCodeGenerate\\"+tableName+".zip");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(fileOutputStream);
        //查询表信息
        Map<String, String> table = queryTable(tableName);
        //查询列信息
        List<Map<String, String>> columns = queryColumns(tableName);
        //生成代码
        GenUtils.generatorCode(genConfig, table, columns, zip);
        IoUtil.close(zip);
        System.out.println("生成代码成功！");
    }

    private static Map<String, String> queryTable(String tableName){
        Connection con = null;
        Map<String, String> map = new HashMap<>();
        try {
            //配置信息
            Configuration config = getConfig();
            Class.forName(config.getString("diver_name")).newInstance();
            con = DriverManager.getConnection(config.getString("url"), config.getString("username"), config.getString("password"));
            String tablesql = "select table_name tableName, engine, table_comment tableComment, create_time createTime from information_schema.tables where table_schema = (select database()) and table_name = '"+tableName+"'";
            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet res = stmt.executeQuery(tablesql);
            res.last();
            int rownum = res.getRow();
            if (rownum <= 0) {
                throw new Exception("该表不存在或者表中没有字段");
            }

            map.put("tableName", res.getString(1));
            map.put("engine", res.getString(2));
            map.put("tableComment", res.getString(3));
            map.put("createTime", res.getString(4));
            con.close();
        } catch (Exception e) {
            System.out.print("生成代码异常:" + e.getMessage());
        }
        return map;
    }

    private static List<Map<String, String>> queryColumns(String tableName){
        Connection con = null;
        List<Map<String, String>> list = new ArrayList<>();
        try {
            //配置信息
            Configuration config = getConfig();
            Class.forName(config.getString("diver_name")).newInstance();
            con = DriverManager.getConnection(config.getString("url"), config.getString("username"), config.getString("password"));
            String tablesql = "select column_name columnName, data_type dataType, column_comment columnComment, column_key columnKey, extra from information_schema.columns where table_name = '"+tableName+"' and table_schema = (select database()) order by ordinal_position";
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery(tablesql);
            while (res.next()){
                Map<String, String> map = new HashMap<>();
                map.put("columnName", res.getString(1));
                map.put("dataType", res.getString(2));
                map.put("columnComment", res.getString(3));
                map.put("columnKey", res.getString(4));
                map.put("extra", res.getString(5));
                list.add(map);
            }
            if (list.isEmpty()) {
                throw new Exception("该表不存在或者表中没有字段");
            }
            con.close();
        } catch (Exception e) {
            System.out.print("生成代码异常:" + e.getMessage());
        }
        return list;
    }

    /**
     * 获取数据源信息
     */
    private static Configuration getConfig() throws Exception{
        try {
            return new PropertiesConfiguration("codegen/database.properties");
        } catch (ConfigurationException e) {
            throw new Exception("获取配置文件失败，", e);
        }
    }


}
