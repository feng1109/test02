package com.eseasky.codegen.generate.impl;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NairCodeGeneratorNew {

    //需要生成的表名
    static String[] tables = {"order_meeting_list"};

    //数据库地址
    static String url = "jdbc:mysql://localhost:3306/book_whu?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";

    //数据库用户名
    static String username = "root";

    //数据库密码
    static String password = "root";


    public static void main(String[] args) {
        String[] models = {"v"};
        for (String model : models) {
        	shell(model);
        }
    }

    private static void shell(String model) {
        File file = new File(model);
        String path = file.getAbsolutePath();
        //path = path.substring(0, path.lastIndexOf(File.separator));
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir(path + "/src/main/java");
        gc.setFileOverride(true);
        gc.setActiveRecord(true);
        gc.setEnableCache(false);// XML 二级缓存
        gc.setBaseResultMap(true);// XML ResultMap
        gc.setBaseColumnList(false);// XML columList
        gc.setSwagger2(true); //实体属性 Swagger2 注解
        gc.setOpen(false);
        gc.setAuthor("");//作者

        // 自定义文件命名，注意 %s 会自动填充表实体属性！
        gc.setMapperName("%sMapper");
        gc.setXmlName("%sMapper");
        gc.setServiceName("%sService");
        gc.setServiceImplName("%sServiceImpl");
//      gc.setControllerName("%sController");
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(url);
        // dsc.setSchemaName("public");
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername(username);
        dsc.setPassword(password);
        mpg.setDataSource(dsc);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        // strategy.setCapitalMode(true);// 全局大写命名 ORACLE 注意
        // strategy.setTablePrefix(new String[] { "tlog_", "tsys_" });// 此处可以修改为您的表前缀
        strategy.setNaming(NamingStrategy.underline_to_camel);// 表名生成策略
        strategy.setInclude(tables); // 需要生成的表
        strategy.setEntityLombokModel(true);//设置lombok
        strategy.setLogicDeleteFieldName("D");//设置逻辑删除标志
        mpg.setStrategy(strategy);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.eseasky.modules.order");
        pc.setController("controller");
        pc.setEntity("entity");
        pc.setMapper("mapper");
        pc.setService("service");
        pc.setServiceImpl("service.impl");
//      pc.setXml(null);
        mpg.setPackageInfo(pc);

        // 注入自定义配置，可以在 VM 中使用 cfg.abc 【可无】
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
            }
        };

        List<FileOutConfig> focList = new ArrayList<FileOutConfig>();


        // 关闭默认 xml 生成，调整生成 至 根目录
        TemplateConfig tc = new TemplateConfig();
        focList.add(new FileOutConfig("/templates/mapper.xml.vm") {
        	@Override
        	public String outputFile(TableInfo tableInfo) {
        		return path + "/src/main/resources/mapper/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
        	}
        });
        cfg.setFileOutConfigList(focList);
        tc.setXml(null);
        mpg.setCfg(cfg);
        mpg.setTemplate(tc);


        // 执行生成
        mpg.execute();
    }


}
