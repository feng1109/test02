package com.eseasky.codegen.generate.impl;

import com.eseasky.codegen.config.Config;
import com.eseasky.codegen.database.DbReadTableUtil;
import com.eseasky.codegen.generate.IGenerate;
import com.eseasky.codegen.generate.pojo.ColumnVo;
import com.eseasky.codegen.generate.pojo.TableVo;
import com.eseasky.codegen.generate.util.FileUtil;
import com.eseasky.codegen.generate.util.NonceUtils;
import com.eseasky.codegen.generate.util.TemplatePaths;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerateOne extends FileUtil
        implements IGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateOne.class);
    private TableVo tableVo;
    private List<ColumnVo> columnVos;
    private List<ColumnVo> orgColumnVos;

    public CodeGenerateOne(TableVo tableVo) {
        this.tableVo = tableVo;
    }

    public CodeGenerateOne(TableVo tableVo, List<ColumnVo> columns, List<ColumnVo> originalColumns) {
        this.tableVo = tableVo;
        this.columnVos = columns;
        this.orgColumnVos = originalColumns;
    }

    public Map<String, Object> getTableInfo()
            throws Exception {
        HashMap localHashMap = new HashMap();

        localHashMap.put("bussiPackage", Config.bussi_package);

        localHashMap.put("entityPackage", this.tableVo.getEntityPackage());

        localHashMap.put("entityName", this.tableVo.getEntityName());

        localHashMap.put("tableName", this.tableVo.getTableName());

        localHashMap.put("primaryKeyField", Config.db_table_id);

        if (this.tableVo.getFieldRequiredNum() == null) {
            this.tableVo.setFieldRequiredNum(Integer.valueOf(StringUtils.isNotEmpty(Config.FieldRequiredNum) ? Integer.parseInt(Config.FieldRequiredNum) : -1));
        }

        if (this.tableVo.getSearchFieldNum() == null) {
            this.tableVo.setSearchFieldNum(Integer.valueOf(StringUtils.isNotEmpty(Config.SearchFieldNum) ? Integer.parseInt(Config.SearchFieldNum) : -1));
        }

        if (this.tableVo.getFieldRowNum() == null) {
            this.tableVo.setFieldRowNum(Integer.valueOf(Integer.parseInt(Config.FieldRowNum)));
        }

        localHashMap.put("tableVo", this.tableVo);
        try {
            if ((this.columnVos == null) || (this.columnVos.size() == 0)) {
                this.columnVos = DbReadTableUtil.getMainTableColumns(this.tableVo.getTableName());
            }
            localHashMap.put("columns", this.columnVos);

            if ((this.orgColumnVos == null) || (this.orgColumnVos.size() == 0)) {
                this.orgColumnVos = DbReadTableUtil.getOrgMainTableColumns(this.tableVo.getTableName());
            }
            localHashMap.put("originalColumns", this.orgColumnVos);

            for (ColumnVo localColumnVo : this.orgColumnVos) {
                if (localColumnVo.getFieldName().toLowerCase().equals(Config.db_table_id.toLowerCase())) {
                    localHashMap.put("primaryKeyPolicy", localColumnVo.getFieldType());
                }
            }
        } catch (Exception localException) {
            throw localException;
        }

        long l = NonceUtils.randomLong() + NonceUtils.currentTime();
        localHashMap.put("serialVersionUID", String.valueOf(l));
        logger.info("code template data: " + localHashMap.toString());
        return localHashMap;
    }

    public void generateCodeFile() throws Exception {
        logger.info("----jeecg---Code----Generation----[单表模型:" + this.tableVo.getTableName() + "]------- 生成中。。。");

        String str1 = Config.project_path;
        Map localMap = getTableInfo();

        String str2 = Config.templatepath;
        if (generatePath(str2, "/").equals("jeecg/code-template")) {
            str2 = "/" + generatePath(str2, "/") + "/one";
        }

        TemplatePaths locala = new TemplatePaths(str2);
        createFileByTemplate(locala, str1, localMap);
        logger.info("----jeecg----Code----Generation-----[单表模型：" + this.tableVo.getTableName() + "]------ 生成完成。。。");
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

    public static void main(String[] args) {
        System.out.println("----jeecg--------- Code------------- Generation -----[单表模型]------- 生成中。。。");
        TableVo localTableVo = new TableVo();
        localTableVo.setTableName("demo");
        localTableVo.setPrimaryKeyPolicy("uuid");
        localTableVo.setEntityPackage("test");
        localTableVo.setEntityName("JeecgDemo");
        localTableVo.setFtlDescription("jeecg 测试demo");
        try {
            new CodeGenerateOne(localTableVo).generateCodeFile();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        System.out.println("----jeecg--------- Code------------- Generation -----[单表模型]------- 生成完成。。。");
    }
}