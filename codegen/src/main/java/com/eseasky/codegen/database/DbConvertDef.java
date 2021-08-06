package com.eseasky.codegen.database;

public abstract interface DbConvertDef
{
  public static final String a = "Y";
  public static final String b = "N";
  public static final String MYSQL = "mysql";
  public static final String ORACLE = "oracle";
  public static final String SQLSERVER = "sqlserver";
  public static final String POSTGRESQL = "postgresql";
  public static final String mysqlColumnSql = "select column_name,data_type,column_comment,numeric_precision,numeric_scale,character_maximum_length,is_nullable nullable from information_schema.columns where table_name = {0} and table_schema = {1}";
  public static final String h = " select colstable.column_name column_name, colstable.data_type data_type, commentstable.comments column_comment, colstable.Data_Precision column_precision, colstable.Data_Scale column_scale,colstable.Char_Length,colstable.nullable from user_tab_cols colstable  inner join user_col_comments commentstable  on colstable.column_name = commentstable.column_name  where colstable.table_name = commentstable.table_name  and colstable.table_name = {0}";
  public static final String i = "select distinct cast(db_type.name as varchar(50)) column_name,  cast(diver_name.name as varchar(50)) data_type,  cast(db_password.value as varchar(200)) comment,  cast(ColumnProperty(db_type.object_id,db_type.Name,'''Precision''') as int) num_precision,  cast(ColumnProperty(db_type.object_id,db_type.Name,'''Scale''') as int) num_scale,  db_type.max_length,  (case when db_type.is_nullable=1 then '''y''' else '''FieldRequiredNum''' end) nullable,column_id   from sys.columns db_type left join sys.types diver_name on db_type.user_type_id=diver_name.user_type_id left join (select top 1 * from sys.objects where type = '''U''' and name ={0}  order by name) db_url on db_type.object_id=db_url.object_id left join sys.extended_properties db_password on db_password.major_id=db_url.object_id and db_password.minor_id=db_type.column_id and db_password.class=1 where db_url.name={0} order by db_type.column_id";
  public static final String j = "SELECT db_type.attname AS  field,t.typname AS type,col_description(db_type.attrelid,db_type.attnum) as comment,null as column_precision,null as column_scale,null as Char_Length,db_type.attnotnull  FROM pg_class db_url,pg_attribute  db_type,pg_type t  WHERE db_url.relname = {0} and db_type.attnum > 0  and db_type.attrelid = db_url.oid and db_type.atttypid = t.oid  ORDER BY db_type.attnum ";
  public static final String k = "select distinct table_name from information_schema.columns where table_schema = {0}";
  public static final String l = " select distinct colstable.table_name as  table_name from user_tab_cols colstable order by colstable.table_name";
  public static final String m = "select distinct db_url.name as  table_name from sys.objects db_url where db_url.type = 'U' ";
  public static final String n = "SELECT distinct db_url.relname AS  table_name FROM pg_class db_url";
}