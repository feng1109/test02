package com.eseasky.codegen.database;

import com.eseasky.codegen.config.Config;
import com.eseasky.codegen.generate.pojo.ColumnVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DbReadTableUtil {
    private static final Logger logger = LoggerFactory.getLogger(DbReadTableUtil.class);
    private static Connection connection;
    private static Statement statement;

    public static List<String> getTableName()
            throws SQLException {
        String str1 = null;
        ArrayList localArrayList = new ArrayList(0);
        try {
            Class.forName(Config.diver_name);
            connection = DriverManager.getConnection(Config.db_url, Config.db_username, Config.db_password);
            statement = connection.createStatement(1005, 1007);

            if (Config.db_type.equals("mysql")) {
                str1 = MessageFormat.format("select distinct table_name from information_schema.columns where table_schema = {0}", new Object[]{Config.database_name});
            }

            if (Config.db_type.equals("oracle")) {
                str1 = " select distinct colstable.table_name as  table_name from user_tab_cols colstable order by colstable.table_name";
            }

            if (Config.db_type.equals("postgresql")) {
                str1 = "SELECT distinct c.relname AS  table_name FROM pg_class c";
            }

            if (Config.db_type.equals("sqlserver")) {
                str1 = "select distinct c.name as  table_name from sys.objects c where c.type = 'U' ";
            }

            ResultSet localResultSet = statement.executeQuery(str1);
            while (localResultSet.next()) {
                String str2 = localResultSet.getString(1);
                localArrayList.add(str2);
            }
        } catch (Exception localSQLException2) {
            localSQLException2.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                    statement = null;
                    System.gc();
                }
                if (connection != null) {
                    connection.close();
                    connection = null;
                    System.gc();
                }
            } catch (SQLException localSQLException3) {
                throw localSQLException3;
            }
        }
        return localArrayList;
    }

    /**
     * 获取主表列属性
     */
    public static List<ColumnVo> getMainTableColumns(String paramString) throws Exception {
        String str = null;
        ArrayList columnList = new ArrayList();
        try {
            Class.forName(Config.diver_name);
            connection = DriverManager.getConnection(Config.db_url, Config.db_username, Config.db_password);
            statement = connection.createStatement(1005, 1007);

            //mysql增加主键列
            if (Config.db_type.equals("mysql")) {
                str = MessageFormat.format("select column_name,data_type,column_comment,numeric_precision,numeric_scale,character_maximum_length,is_nullable nullable,column_key from information_schema.columns where table_name = ''{0}'' and table_schema = (select database())", new Object[]{paramString.toLowerCase()});
            }

            if (Config.db_type.equals("oracle")) {
                str = MessageFormat.format(" select colstable.column_name column_name, colstable.data_type data_type, commentstable.comments column_comment, colstable.Data_Precision column_precision, colstable.Data_Scale column_scale,colstable.Char_Length,colstable.nullable from user_tab_cols colstable  inner join user_col_comments commentstable  on colstable.column_name = commentstable.column_name  where colstable.table_name = commentstable.table_name  and colstable.table_name = {0}", new Object[]{paramString.toLowerCase()});
            }

            if (Config.db_type.equals("postgresql")) {
                str = MessageFormat.format("SELECT db_type.attname AS  field,t.typname AS type,col_description(db_type.attrelid,db_type.attnum) as comment,null as column_precision,null as column_scale,null as Char_Length,db_type.attnotnull  FROM pg_class db_url,pg_attribute  db_type,pg_type t  WHERE db_url.relname = {0} and db_type.attnum > 0  and db_type.attrelid = db_url.oid and db_type.atttypid = t.oid  ORDER BY db_type.attnum ", new Object[]{paramString.toLowerCase()});
            }

            if (Config.db_type.equals("sqlserver")) {
                str = MessageFormat.format("select distinct cast(db_type.name as varchar(50)) column_name,  cast(diver_name.name as varchar(50)) data_type,  cast(db_password.value as varchar(200)) comment,  cast(ColumnProperty(db_type.object_id,db_type.Name,'''Precision''') as int) num_precision,  cast(ColumnProperty(db_type.object_id,db_type.Name,'''Scale''') as int) num_scale,  db_type.max_length,  (case when db_type.is_nullable=1 then '''y''' else '''FieldRequiredNum''' end) nullable,column_id   from sys.columns db_type left join sys.types diver_name on db_type.user_type_id=diver_name.user_type_id left join (select top 1 * from sys.objects where type = '''U''' and name ={0}  order by name) db_url on db_type.object_id=db_url.object_id left join sys.extended_properties db_password on db_password.major_id=db_url.object_id and db_password.minor_id=db_type.column_id and db_password.class=1 where db_url.name={0} order by db_type.column_id", new Object[]{paramString.toLowerCase()});
            }

            ResultSet resultSet = statement.executeQuery(str);
            resultSet.last();
            int i = resultSet.getRow();
            int index = i;

            ColumnVo columnVo;
            if (index > 0) {
                columnVo = new ColumnVo();

                if (Config.db_filed_convert)
                    columnVo.setFieldName(camelCase(resultSet.getString(1).toLowerCase()));
                else {
                    columnVo.setFieldName(resultSet.getString(1).toLowerCase());
                }

                columnVo.setFieldDbName(resultSet.getString(1).toUpperCase());
                columnVo.setFieldType(camelCase(resultSet.getString(2).toLowerCase()));
                columnVo.setFieldDbType(camelCase(resultSet.getString(2).toLowerCase()));

                columnVo.setPrecision(resultSet.getString(4));
                columnVo.setScale(resultSet.getString(5));
                columnVo.setCharmaxLength(resultSet.getString(6));
                columnVo.setNullable(resultSet.getString(7));
                columnVo.setColumnKey(resultSet.getString(8).toUpperCase());//表字段类型（主键判断）

                getTableName(columnVo);
                columnVo.setFiledComment(StringUtils.isBlank(resultSet.getString(3)) ? columnVo.getFieldName() : resultSet.getString(3));

                logger.debug("columnt.getFieldName() -------------" + columnVo.getFieldName());

                String[] arrayOfString = new String[0];
                //自动生成create_by~update_time字段
                if (Config.page_filter_fields != null) {
                    arrayOfString = Config.page_filter_fields.toLowerCase().split(",");
                }

                //主键判断
                if ((!Config.db_table_id.equals(columnVo.getFieldName())) &&
                        (!DbUtil.parseStr(columnVo
                                .getFieldDbName().toLowerCase(), arrayOfString))) {
                    columnList.add(columnVo);
                }
                while (resultSet.previous()) {
                    ColumnVo columnVo1 = new ColumnVo();

                    if (Config.db_filed_convert)
                        columnVo1.setFieldName(camelCase(resultSet.getString(1).toLowerCase()));
                    else {
                        columnVo1.setFieldName(resultSet.getString(1).toLowerCase());
                    }

                    columnVo1.setFieldDbName(resultSet.getString(1).toUpperCase());
                    logger.debug("columnt.getFieldName() -------------" + columnVo1.getFieldName());
                    if ((Config.db_table_id.equals(columnVo1.getFieldName())) ||
                            (DbUtil.parseStr(columnVo1
                                    .getFieldDbName().toLowerCase(), arrayOfString))) {
                        continue;
                    }
                    columnVo1.setFieldType(camelCase(resultSet.getString(2).toLowerCase()));

                    columnVo1.setFieldDbType(camelCase(resultSet.getString(2).toLowerCase()));
                    logger.debug("-----po.setFieldType------------" + columnVo1.getFieldType());

                    columnVo1.setPrecision(resultSet.getString(4));
                    columnVo1.setScale(resultSet.getString(5));
                    columnVo1.setCharmaxLength(resultSet.getString(6));
                    columnVo1.setNullable(resultSet.getString(7));
                    columnVo.setColumnKey(resultSet.getString(8).toUpperCase());//表字段类型（主键判断）

                    getTableName(columnVo1);
                    columnVo1.setFiledComment(StringUtils.isBlank(resultSet.getString(3)) ? columnVo1.getFieldName() : resultSet.getString(3));
                    columnList.add(columnVo1);
                }
            } else {
                throw new Exception("该表不存在或者表中没有字段");
            }

            logger.debug("读取表成功");
        } catch (ClassNotFoundException localClassNotFoundException) {
            throw localClassNotFoundException;
        } catch (SQLException localSQLException2) {
            throw localSQLException2;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                    statement = null;
                    System.gc();
                }
                if (connection != null) {
                    connection.close();
                    connection = null;
                    System.gc();
                }
            } catch (SQLException localSQLException3) {
                throw localSQLException3;
            }

        }

        ArrayList columnList2 = new ArrayList();
        for (int j = columnList.size() - 1; j >= 0; j--) {
            ColumnVo columnVo = (ColumnVo) columnList.get(j);
            columnList2.add(columnVo);
        }
        return columnList2;
    }

    public static List<ColumnVo> getOrgMainTableColumns(String paramString) throws Exception {
        ResultSet resultSet = null;
        String str = null;
        ArrayList columnList = new ArrayList();
        ColumnVo columnVo;
        try {
            Class.forName(Config.diver_name);
            connection = DriverManager.getConnection(Config.db_url, Config.db_username, Config.db_password);
            statement = connection.createStatement(1005, 1007);

            //mysql增加主键列
            if (Config.db_type.equals("mysql")) {
                str = MessageFormat.format("select column_name,data_type,column_comment,numeric_precision,numeric_scale,character_maximum_length,is_nullable nullable,column_key from information_schema.columns where table_name = ''{0}'' and table_schema = (select database())", new Object[]{paramString.toLowerCase()});
            }

            if (Config.db_type.equals("oracle")) {
                str = MessageFormat.format(" select colstable.column_name column_name, colstable.data_type data_type, commentstable.comments column_comment, colstable.Data_Precision column_precision, colstable.Data_Scale column_scale,colstable.Char_Length,colstable.nullable from user_tab_cols colstable  inner join user_col_comments commentstable  on colstable.column_name = commentstable.column_name  where colstable.table_name = commentstable.table_name  and colstable.table_name = {0}", new Object[]{paramString.toLowerCase()});
            }

            if (Config.db_type.equals("postgresql")) {
                str = MessageFormat.format("SELECT db_type.attname AS  field,t.typname AS type,col_description(db_type.attrelid,db_type.attnum) as comment,null as column_precision,null as column_scale,null as Char_Length,db_type.attnotnull  FROM pg_class db_url,pg_attribute  db_type,pg_type t  WHERE db_url.relname = {0} and db_type.attnum > 0  and db_type.attrelid = db_url.oid and db_type.atttypid = t.oid  ORDER BY db_type.attnum ", new Object[]{paramString.toLowerCase()});
            }

            if (Config.db_type.equals("sqlserver")) {
                str = MessageFormat.format("select distinct cast(db_type.name as varchar(50)) column_name,  cast(diver_name.name as varchar(50)) data_type,  cast(db_password.value as varchar(200)) comment,  cast(ColumnProperty(db_type.object_id,db_type.Name,'''Precision''') as int) num_precision,  cast(ColumnProperty(db_type.object_id,db_type.Name,'''Scale''') as int) num_scale,  db_type.max_length,  (case when db_type.is_nullable=1 then '''y''' else '''FieldRequiredNum''' end) nullable,column_id   from sys.columns db_type left join sys.types diver_name on db_type.user_type_id=diver_name.user_type_id left join (select top 1 * from sys.objects where type = '''U''' and name ={0}  order by name) db_url on db_type.object_id=db_url.object_id left join sys.extended_properties db_password on db_password.major_id=db_url.object_id and db_password.minor_id=db_type.column_id and db_password.class=1 where db_url.name={0} order by db_type.column_id", new Object[]{paramString.toLowerCase()});
            }

            resultSet = statement.executeQuery(str);
            resultSet.last();
            int i = resultSet.getRow();
            int index = i;

            if (index > 0) {
                columnVo = new ColumnVo();

                if (Config.db_filed_convert)
                    columnVo.setFieldName(camelCase(resultSet.getString(1).toLowerCase()));
                else {
                    columnVo.setFieldName(resultSet.getString(1).toLowerCase());
                }

                columnVo.setFieldDbName(resultSet.getString(1).toUpperCase());

                columnVo.setPrecision(resultSet.getString(4));
                columnVo.setScale(resultSet.getString(5));
                columnVo.setCharmaxLength(resultSet.getString(6));
                columnVo.setNullable(resultSet.getString(7));

                columnVo.setFieldType(getTableName(resultSet.getString(2).toLowerCase(), columnVo.getPrecision(), columnVo.getScale()));

                columnVo.setFieldDbType(camelCase(resultSet.getString(2).toLowerCase()));

                columnVo.setColumnKey(resultSet.getString(8).toUpperCase());//表字段类型（主键判断）

                getTableName(columnVo);
                columnVo.setFiledComment(StringUtils.isBlank(resultSet.getString(3)) ? columnVo.getFieldName() : resultSet.getString(3));

                logger.debug("columnt.getFieldName() -------------" + columnVo.getFieldName());
                columnList.add(columnVo);
                while (resultSet.previous()) {
                    ColumnVo columnVo1 = new ColumnVo();

                    if (Config.db_filed_convert)
                        columnVo1.setFieldName(camelCase(resultSet.getString(1).toLowerCase()));
                    else {
                        columnVo1.setFieldName(resultSet.getString(1).toLowerCase());
                    }

                    columnVo1.setFieldDbName(resultSet.getString(1).toUpperCase());

                    columnVo1.setPrecision(resultSet.getString(4));
                    columnVo1.setScale(resultSet.getString(5));
                    columnVo1.setCharmaxLength(resultSet.getString(6));
                    columnVo1.setNullable(resultSet.getString(7));

                    columnVo.setColumnKey(resultSet.getString(8).toUpperCase());//表字段类型（主键判断）

                    columnVo1.setFieldType(getTableName(resultSet.getString(2).toLowerCase(), columnVo1.getPrecision(), columnVo1.getScale()));

                    columnVo1.setFieldDbType(camelCase(resultSet.getString(2).toLowerCase()));

                    getTableName(columnVo1);
                    columnVo1.setFiledComment(StringUtils.isBlank(resultSet.getString(3)) ? columnVo1.getFieldName() : resultSet.getString(3));
                    columnList.add(columnVo1);
                }
            } else {
                throw new Exception("该表不存在或者表中没有字段");
            }

            logger.debug("读取表成功");
        } catch (ClassNotFoundException localClassNotFoundException) {
            throw localClassNotFoundException;
        } catch (SQLException localSQLException2) {
            throw localSQLException2;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                    statement = null;
                    System.gc();
                }
                if (connection != null) {
                    connection.close();
                    connection = null;
                    System.gc();
                }
            } catch (SQLException localSQLException3) {
                throw localSQLException3;
            }

        }

        ArrayList columnList2 = new ArrayList();
        for (int j = columnList.size() - 1; j >= 0; j--) {
            ColumnVo columnVo2 = (ColumnVo) columnList.get(j);
            columnList2.add(columnVo2);
        }
        return columnList2;
    }

    public static boolean c(String paramString) {
        String str = null;
        try {
            logger.debug("数据库驱动: " + Config.diver_name);
            Class.forName(Config.diver_name);
            connection = DriverManager.getConnection(Config.db_url, Config.db_username, Config.db_password);
            statement = connection.createStatement(1005, 1007);

            if (Config.db_type.equals("mysql")) {
                str = "select column_name,data_type,column_comment,0,0 from information_schema.columns where table_name = '" + paramString
                        .toUpperCase() + "' and table_schema = '" + Config.database_name + "'";
            }

            if (Config.db_type.equals("oracle")) {
                str = "select colstable.column_name column_name, colstable.data_type data_type, commentstable.comments column_comment from user_tab_cols colstable  inner join user_col_comments commentstable  on colstable.column_name = commentstable.column_name  where colstable.table_name = commentstable.table_name  and colstable.table_name = '" + paramString
                        .toUpperCase() + "'";
            }

            if (Config.db_type.equals("postgresql")) {
                str = MessageFormat.format("SELECT db_type.attname AS  field,t.typname AS type,col_description(db_type.attrelid,db_type.attnum) as comment,null as column_precision,null as column_scale,null as Char_Length,db_type.attnotnull  FROM pg_class db_url,pg_attribute  db_type,pg_type t  WHERE db_url.relname = {0} and db_type.attnum > 0  and db_type.attrelid = db_url.oid and db_type.atttypid = t.oid  ORDER BY db_type.attnum ", new Object[]{paramString.toLowerCase()});
            }
            if (Config.db_type.equals("sqlserver")) {
                str = MessageFormat.format("select distinct cast(db_type.name as varchar(50)) column_name,  cast(diver_name.name as varchar(50)) data_type,  cast(db_password.value as varchar(200)) comment,  cast(ColumnProperty(db_type.object_id,db_type.Name,'''Precision''') as int) num_precision,  cast(ColumnProperty(db_type.object_id,db_type.Name,'''Scale''') as int) num_scale,  db_type.max_length,  (case when db_type.is_nullable=1 then '''y''' else '''FieldRequiredNum''' end) nullable,column_id   from sys.columns db_type left join sys.types diver_name on db_type.user_type_id=diver_name.user_type_id left join (select top 1 * from sys.objects where type = '''U''' and name ={0}  order by name) db_url on db_type.object_id=db_url.object_id left join sys.extended_properties db_password on db_password.major_id=db_url.object_id and db_password.minor_id=db_type.column_id and db_password.class=1 where db_url.name={0} order by db_type.column_id", new Object[]{paramString.toLowerCase()});
            }

            ResultSet localResultSet = statement.executeQuery(str);
            localResultSet.last();
            int i = localResultSet.getRow();
            if (i > 0)
                return true;
        } catch (Exception localException) {
            localException.printStackTrace();
            return false;
        }
        return false;
    }

    private static String camelCase(String paramString) {
        String[] arrayOfString = paramString.split("_");
        paramString = "";
        int i = 0;
        for (int j = arrayOfString.length; i < j; i++) {
            if (i > 0) {
                String str = arrayOfString[i].toLowerCase();

                str = str.substring(0, 1).toUpperCase() + str
                        .substring(1, str
                                .length());
                paramString = paramString + str;
            } else {
                paramString = paramString + arrayOfString[i].toLowerCase();
            }
        }
        return paramString;
    }

    public static String camelCaseUnderscore(String paramString) {
        String[] arrayOfString = paramString.split("_");
        paramString = "";
        int i = 0;
        for (int j = arrayOfString.length; i < j; i++) {
            if (i > 0) {
                String str = arrayOfString[i].toLowerCase();

                str = str.substring(0, 1).toUpperCase() + str
                        .substring(1, str
                                .length());
                paramString = paramString + str;
            } else {
                paramString = paramString + arrayOfString[i].toLowerCase();
            }
        }
        paramString = paramString.substring(0, 1).toUpperCase() + paramString.substring(1);
        return paramString;
    }

    private static void getTableName(ColumnVo paramColumnVo) {
        String str1 = paramColumnVo.getFieldType();
        String str2 = paramColumnVo.getScale();

        paramColumnVo.setClassType("inputxt");

        if ("N".equals(paramColumnVo.getNullable())) {
            paramColumnVo.setOptionType("*");
        }
        if (("datetime".equals(str1)) || (str1.contains("time")))
            paramColumnVo.setClassType("easyui-datetimebox");
        else if ("date".equals(str1))
            paramColumnVo.setClassType("easyui-datebox");
        else if (str1.contains("int"))
            paramColumnVo.setOptionType("FieldRequiredNum");
        else if ("number".equals(str1)) {
            if ((StringUtils.isNotBlank(str2)) && (Integer.parseInt(str2) > 0))
                paramColumnVo.setOptionType("db_username");
        } else if (("float".equals(str1)) || ("double".equals(str1)) || ("decimal".equals(str1)))
            paramColumnVo.setOptionType("db_username");
        else if ("numeric".equals(str1))
            paramColumnVo.setOptionType("db_username");
    }

    private static String getTableName(String paramString1, String paramString2, String paramString3) {
        if (paramString1.contains("char"))
            paramString1 = "java.lang.String";
        else if (paramString1.contains("int"))
            paramString1 = "java.lang.Integer";
        else if (paramString1.contains("float"))
            paramString1 = "java.lang.Float";
        else if (paramString1.contains("double"))
            paramString1 = "java.lang.Double";
        else if (paramString1.contains("number")) {
            if ((StringUtils.isNotBlank(paramString3)) && (Integer.parseInt(paramString3) > 0))
                paramString1 = "java.math.BigDecimal";
            else if ((StringUtils.isNotBlank(paramString2)) && (Integer.parseInt(paramString2) > 10))
                paramString1 = "java.lang.Long";
            else
                paramString1 = "java.lang.Integer";
        } else if (paramString1.contains("decimal"))
            paramString1 = "java.math.BigDecimal";
        else if (paramString1.contains("date"))
            paramString1 = "java.util.Date";
        else if (paramString1.contains("time")) {
            paramString1 = "java.util.Date";
        } else if (paramString1.contains("blob"))
            paramString1 = "byte[]";
        else if (paramString1.contains("clob"))
            paramString1 = "java.sql.Clob";
        else if (paramString1.contains("numeric"))
            paramString1 = "java.math.BigDecimal";
        else {
            paramString1 = "java.lang.Object";
        }
        return paramString1;
    }
}