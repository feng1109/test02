package com.eseasky.codegen.database;

import org.apache.commons.lang.StringUtils;

import java.util.List;

public class DbUtil {
    public static String parseStr(String[] paramArrayOfString) {
        StringBuffer localStringBuffer = new StringBuffer();
        for (String str : paramArrayOfString) {
            if (StringUtils.isNotBlank(str)) {
                localStringBuffer.append(",");
                localStringBuffer.append("'");
                localStringBuffer.append(str.trim());
                localStringBuffer.append("'");
            }
        }
        return localStringBuffer.toString().substring(1);
    }

    public static String parseStr(String paramString) {
        if (StringUtils.isNotBlank(paramString)) {
            paramString = paramString.substring(0, 1).toLowerCase() + paramString.substring(1);
        }
        return paramString;
    }

    public static Integer parseStr(Integer paramInteger) {
        if (paramInteger == null) {
            return Integer.valueOf(0);
        }
        return paramInteger;
    }

    public static boolean parseStr(String paramString, String[] paramArrayOfString) {
        if ((paramArrayOfString == null) || (paramArrayOfString.length == 0)) {
            return false;
        }
        for (int i = 0; i < paramArrayOfString.length; i++) {
            String str = paramArrayOfString[i];
            if (str.equals(paramString)) {
                return true;
            }
        }
        return false;
    }

    public static boolean parseStr(String paramString, List<String> paramList) {
        String[] arrayOfString = new String[0];
        if (paramList != null) {
            arrayOfString = (String[]) (String[]) paramList.toArray();
        }

        if ((arrayOfString == null) || (arrayOfString.length == 0)) {
            return false;
        }
        for (int i = 0; i < arrayOfString.length; i++) {
            String str = arrayOfString[i];
            if (str.equals(paramString)) {
                return true;
            }
        }
        return false;
    }
}