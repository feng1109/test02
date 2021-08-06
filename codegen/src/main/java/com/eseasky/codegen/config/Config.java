package com.eseasky.codegen.config;

import java.util.ResourceBundle;

public class Config
{
  private static final ResourceBundle databaseConfig = ResourceBundle.getBundle("codegen/database");
  private static final ResourceBundle config = ResourceBundle.getBundle("codegen/com.eseasky.common.code.config");

  public static String db_type = "mysql";

  public static String diver_name = "com.mysql.jdbc.Driver";

  public static String db_url = "jdbc:mysql://localhost:3306/survey?useUnicode=true&characterEncoding=UTF-8";

  public static String db_username = "root";

  public static String db_password = "root";

  public static String database_name = "survey";

  public static String project_path = "db_url:/workspace/jeecg";

  public static String bussi_package = "com.eseasky";

  public static String source_root_package = "src";

  public static String webroot_package = "WebRoot";

  public static String templatepath = "/codegen/code-template/";

  public static boolean db_filed_convert = true;
  public static String db_table_id;
  public static String FieldRequiredNum = "4";

  public static String SearchFieldNum = "3";
  public static String page_filter_fields;
  public static String FieldRowNum = "1";

  private void n()
  {
  }

  public static final String getDriverName()
  {
    return databaseConfig.getString("diver_name");
  }

  public static final String getDbUrl()
  {
    return databaseConfig.getString("url");
  }

  public static final String getDbUserName()
  {
    return databaseConfig.getString("username");
  }

  public static final String getDbPassword()
  {
    return databaseConfig.getString("password");
  }

  public static final String getDatabaseName()
  {
    return databaseConfig.getString("database_name");
  }

  public static final boolean getDbFiledConvert()
  {
    String str = config.getString("db_filed_convert");

    return !str.toString().equals("false");
  }

  private static String getBussPackage()
  {
    return config.getString("bussi_package");
  }
  private static String getTemplatepath() {
    return config.getString("templatepath");
  }

  public static final String getSourceRootPackage()
  {
    return config.getString("source_root_package");
  }

  public static final String getWebrootPackage()
  {
    return config.getString("webroot_package");
  }

  public static final String getDbTableDd()
  {
    return config.getString("db_table_id");
  }

  public static final String getPageFilterFields()
  {
    return config.getString("page_filter_fields");
  }

  public static final String getPageSearchFiledNum()
  {
    return config.getString("page_search_filed_num");
  }

  public static final String getPageFieldRequiredNum()
  {
    return config.getString("page_field_required_num");
  }

  public static String getProjectPath() {
    String str = config.getString("project_path");
    if ((str != null) && (!"".equals(str))) {
      project_path = str;
    }
    return project_path;
  }

  public static void getDriverName(String paramString)
  {
    project_path = paramString;
  }

  public static void getDbUrl(String paramString)
  {
    templatepath = paramString;
  }

  static
  {
    diver_name = getDriverName();
    db_url = getDbUrl();
    db_username = getDbUserName();
    db_password = getDbPassword();
    database_name = getDatabaseName();

    source_root_package = getSourceRootPackage();
    webroot_package = getWebrootPackage();
    bussi_package = getBussPackage();
    templatepath = getTemplatepath();
    project_path = getProjectPath();

    db_table_id = getDbTableDd();
    db_filed_convert = getDbFiledConvert();

    page_filter_fields = getPageFilterFields();

    SearchFieldNum = getPageSearchFiledNum();

    if ((db_url.indexOf("mysql") >= 0) || (db_url.indexOf("MYSQL") >= 0))
      db_type = "mysql";
    else if ((db_url.indexOf("oracle") >= 0) || (db_url.indexOf("ORACLE") >= 0))
      db_type = "oracle";
    else if ((db_url.indexOf("postgresql") >= 0) || (db_url.indexOf("POSTGRESQL") >= 0)) {
      db_type = "postgresql";
    }
    else if ((db_url.indexOf("sqlserver") >= 0) || (db_url.indexOf("sqlserver") >= 0)) {
      db_type = "sqlserver";
    }

    source_root_package = source_root_package.replace(".", "/");
    webroot_package = webroot_package.replace(".", "/");
  }
}