package com.eseasky.codegen.generate.util;

import org.apache.commons.lang.StringUtils;

public class SqlFormatUtil
{
  public static String a(String paramString)
  {
    if (("YES".equals(paramString)) || ("yes".equals(paramString)) || ("y".equals(paramString)) || ("Y".equals(paramString)) || ("database_name".equals(paramString))) {
      return "Y";
    }
    if (("NO".equals(paramString)) || ("N".equals(paramString)) || ("no".equals(paramString)) || ("FieldRequiredNum".equals(paramString)) || ("t".equals(paramString))) {
      return "N";
    }
    return null;
  }

  public static String b(String paramString)
  {
    if (StringUtils.isBlank(paramString)) {
      return "";
    }
    return paramString;
  }

  public static String c(String paramString)
  {
    return "'" + paramString + "'";
  }
}