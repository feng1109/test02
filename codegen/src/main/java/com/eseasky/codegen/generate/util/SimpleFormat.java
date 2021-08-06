package com.eseasky.codegen.generate.util;

import java.text.*;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SimpleFormat
{
  public static String underlineToHump(String para)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    String[] arrayOfString1 = para.split("_");
    for (String str : arrayOfString1) {
      if (!para.contains("_")) {
        localStringBuilder.append(str);
      }
      else if (localStringBuilder.length() == 0) {
        localStringBuilder.append(str.toLowerCase());
      } else {
        localStringBuilder.append(str.substring(0, 1).toUpperCase());
        localStringBuilder.append(str.substring(1).toLowerCase());
      }
    }
    return localStringBuilder.toString();
  }

  public static String humpToUnderline(String para)
  {
    StringBuilder localStringBuilder = new StringBuilder(para);
    int i = 0;
    if (!para.contains("_")) {
      for (int j = 0; j < para.length(); j++) {
        if (Character.isUpperCase(para.charAt(j))) {
          localStringBuilder.insert(j + i, "_");
          i++;
        }
      }
    }
    if (localStringBuilder.toString().toLowerCase().startsWith("_")) {
      return localStringBuilder.toString().toLowerCase().substring(1);
    }
    System.out.println(localStringBuilder.toString().toLowerCase());
    return localStringBuilder.toString().toLowerCase();
  }

  public static String humpToShortbar(String para)
  {
    StringBuilder localStringBuilder = new StringBuilder(para);
    int i = 0;
    if (!para.contains("-")) {
      for (int j = 0; j < para.length(); j++) {
        if (Character.isUpperCase(para.charAt(j))) {
          localStringBuilder.insert(j + i, "-");
          i++;
        }
      }
    }
    if (localStringBuilder.toString().toLowerCase().startsWith("-")) {
      return localStringBuilder.toString().toLowerCase().substring(1);
    }
    System.out.println(localStringBuilder.toString().toLowerCase());
    return localStringBuilder.toString().toLowerCase();
  }

  public static void main(String[] args)
  {
    System.out.println(humpToShortbar("jeecgDemo"));
  }

  public String number(Object obj)
  {
    obj = (obj == null) || (obj.toString().length() == 0) ? Integer.valueOf(0) : obj;
    if (obj.toString().equalsIgnoreCase("NaN"))
      return "NaN";
    return new DecimalFormat("0.00").format(Double.parseDouble(obj.toString()));
  }

  public String number(Object obj, String pattern)
  {
    obj = (obj == null) || (obj.toString().length() == 0) ? Integer.valueOf(0) : obj;
    if (obj.toString().equalsIgnoreCase("NaN"))
      return "NaN";
    return new DecimalFormat(pattern).format(Double.parseDouble(obj.toString()));
  }

  public String round(Object obj)
  {
    obj = (obj == null) || (obj.toString().length() == 0) ? Integer.valueOf(0) : obj;
    if (obj.toString().equalsIgnoreCase("NaN"))
      return "NaN";
    return new DecimalFormat("0").format(Double.parseDouble(obj.toString()));
  }

  public String currency(Object obj)
  {
    obj = (obj == null) || (obj.toString().length() == 0) ? Integer.valueOf(0) : obj;
    return NumberFormat.getCurrencyInstance(Locale.CHINA).format(obj);
  }

  public String timestampToString(Object obj, String pattern)
  {
    if (obj == null)
      return "";
    SimpleDateFormat localSimpleDateFormat1 = new SimpleDateFormat("dd-MM月 -yy");
    SimpleDateFormat localSimpleDateFormat2 = new SimpleDateFormat(pattern);
    Date localDate = null;
    try {
      localDate = localSimpleDateFormat1.parse(obj.toString());
    } catch (ParseException localParseException) {
      localParseException.printStackTrace();
      return "error";
    }
    return localSimpleDateFormat2.format(localDate);
  }

  public String percent(Object obj)
  {
    obj = (obj == null) || (obj.toString().length() == 0) ? Integer.valueOf(0) : obj;
    if (obj.toString().equalsIgnoreCase("NaN"))
      return "";
    return NumberFormat.getPercentInstance(Locale.CHINA).format(obj);
  }

  public String date(Object obj, String pattern)
  {
    if (obj == null)
      return "";
    return new SimpleDateFormat(pattern).format(obj);
  }

  public String date(Object obj)
  {
    if (obj == null)
      return "";
    return DateFormat.getDateInstance(1, Locale.CHINA).format(obj);
  }

  public String time(Object obj)
  {
    if (obj == null)
      return "";
    return DateFormat.getTimeInstance(3, Locale.CHINA).format(obj);
  }

  public String datetime(Object obj)
  {
    if (obj == null)
      return "";
    return DateFormat.getDateTimeInstance(1, 3, Locale.CHINA).format(obj);
  }

  public String getInStrs(List<String> params)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (Object localObject = params.iterator(); ((Iterator)localObject).hasNext(); ) { String str = (String)((Iterator)localObject).next();
      localStringBuffer.append("'" + str + "',");
    }

    String localObject = localStringBuffer.toString();
    if ((!"".equals(localObject)) || (((String)localObject).endsWith(","))) {
      localObject = ((String)localObject).substring(0, ((String)localObject).length() - 1);
      return localObject;
    }
    return (String)null;
  }
}