package com.eseasky.codegen.generate.impl;

import com.eseasky.codegen.config.Config;
import com.eseasky.codegen.database.DbReadTableUtil;
import com.eseasky.codegen.generate.IGenerate;
import com.eseasky.codegen.generate.pojo.ColumnVo;
import com.eseasky.codegen.generate.pojo.onetomany.MainTableVo;
import com.eseasky.codegen.generate.pojo.onetomany.SubTableVo;
import com.eseasky.codegen.generate.util.FileUtil;
import com.eseasky.codegen.generate.util.NonceUtils;
import com.eseasky.codegen.generate.util.TemplatePaths;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CodeGenerateOneToMany extends FileUtil
        implements IGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateOneToMany.class);
    private static String e;
    public static String ftl_mode_a = "A";
    public static String ftl_mode_b = "B";
    private MainTableVo mainTableVo;
    private List<ColumnVo> mainColumnVos;
    private List<ColumnVo> orgMainColumnVos;
    private List<SubTableVo> subTableVos;
    private static DbReadTableUtil dbReadTableUtil = new DbReadTableUtil();

    public CodeGenerateOneToMany(MainTableVo mainTableVo, List<SubTableVo> subTables) {
        this.subTableVos = subTables;
        this.mainTableVo = mainTableVo;
    }

    public CodeGenerateOneToMany(MainTableVo mainTableVo, List<ColumnVo> mainColums, List<ColumnVo> originalMainColumns, List<SubTableVo> subTables) {
        this.mainTableVo = mainTableVo;
        this.mainColumnVos = mainColums;
        this.orgMainColumnVos = originalMainColumns;
        this.subTableVos = subTables;
    }

    public Map<String, Object> getTableInfo()
            throws Exception {
        HashMap tableInfoMap = new HashMap();

        tableInfoMap.put("bussiPackage", Config.bussi_package);

        tableInfoMap.put("entityPackage", this.mainTableVo.getEntityPackage());

        tableInfoMap.put("entityName", this.mainTableVo.getEntityName());

        tableInfoMap.put("tableName", this.mainTableVo.getTableName());

        tableInfoMap.put("ftl_description", this.mainTableVo.getFtlDescription());

        tableInfoMap.put("primaryKeyField", Config.db_table_id);

        if (this.mainTableVo.getFieldRequiredNum() == null) {
            this.mainTableVo.setFieldRequiredNum(Integer.valueOf(StringUtils.isNotEmpty(Config.FieldRequiredNum) ? Integer.parseInt(Config.FieldRequiredNum) : -1));
        }

        if (this.mainTableVo.getSearchFieldNum() == null) {
            this.mainTableVo.setSearchFieldNum(Integer.valueOf(StringUtils.isNotEmpty(Config.SearchFieldNum) ? Integer.parseInt(Config.SearchFieldNum) : -1));
        }

        if (this.mainTableVo.getFieldRowNum() == null) {
            this.mainTableVo.setFieldRowNum(Integer.valueOf(Integer.parseInt(Config.FieldRowNum)));
        }

        tableInfoMap.put("tableVo", this.mainTableVo);
        try {
            if ((this.mainColumnVos == null) || (this.mainColumnVos.size() == 0)) {
                this.mainColumnVos = DbReadTableUtil.getMainTableColumns(this.mainTableVo.getTableName());
            }
            if ((this.orgMainColumnVos == null) || (this.orgMainColumnVos.size() == 0)) {
                this.orgMainColumnVos = DbReadTableUtil.getOrgMainTableColumns(this.mainTableVo.getTableName());
            }

            tableInfoMap.put("columns", this.mainColumnVos);

            tableInfoMap.put("originalColumns", this.orgMainColumnVos);

            //获取表主键
            Iterator localIterator = this.orgMainColumnVos.iterator();
            while (localIterator.hasNext()) {
                ColumnVo columnVo = (ColumnVo) localIterator.next();
                //自定义id存在，则以自定义为准
                if(columnVo.getFieldName().toLowerCase().equals(Config.db_table_id.toLowerCase())){
                    tableInfoMap.put("primaryKeyPolicy", columnVo.getFieldType());
                }
            }
            //子表循环获取值
            for(SubTableVo subTableVo:this.subTableVos){
                if (subTableVo.getColums() == null || subTableVo.getColums().size() == 0) {
                    List<ColumnVo> columnVoList = DbReadTableUtil.getMainTableColumns(subTableVo.getTableName());
                    subTableVo.setColums(columnVoList);
                }

                if (subTableVo.getOriginalColumns() == null || subTableVo.getOriginalColumns().size() == 0) {
                    List<ColumnVo> columnVoList = DbReadTableUtil.getOrgMainTableColumns(subTableVo.getTableName());
                    subTableVo.setOriginalColumns(columnVoList);
                }

                String[] foreignKeys = subTableVo.getForeignKeys();
                ArrayList localArrayList = new ArrayList();
                for (String str : foreignKeys) {
                    localArrayList.add(DbReadTableUtil.camelCaseUnderscore(str));
                }

                subTableVo.setForeignKeys((String[]) localArrayList.toArray(new String[0]));
                subTableVo.setOriginalForeignKeys(foreignKeys);

            }

            tableInfoMap.put("subTables", this.subTableVos);
        } catch (Exception localException) {
            throw localException;
        }

        long l = NonceUtils.randomLong() + NonceUtils.currentTime();
        tableInfoMap.put("serialVersionUID", String.valueOf(l));
        logger.info("code template data: " + tableInfoMap.toString());
        return tableInfoMap;
    }

    public void generateCodeFile() throws Exception {
        logger.info("----jeecg---Code----Generation----[一对多模型:" + this.mainTableVo.getTableName() + "]------- 生成中。。。");

        //生成代码的根目录project_path
        String projectPath = Config.project_path;
        Map tableInfo = getTableInfo();

        //模板路径根目录
        String templatepath = Config.templatepath;
        if (generatePath(templatepath, "/").equals("codegen/code-template")) {
            templatepath = "/" + generatePath(templatepath, "/") + "/onetomany";
        }

        TemplatePaths templates = new TemplatePaths(templatepath);
        createFileByTemplate(templates, projectPath, tableInfo);
        logger.info("----jeecg----Code----Generation-----[一对多模型：" + this.mainTableVo.getTableName() + "]------ 生成完成。。。");
    }

    public void generateCodeFile(String projectPath, String templatePath)
            throws Exception {
        if ((projectPath != null) && (!"".equals(projectPath))) {
            Config.getDriverName(projectPath);
        }

        if ((templatePath != null) && (!"".equals(templatePath))) {
            Config.getDbUrl(templatePath);
        }
        generateCodeFile();
    }
}