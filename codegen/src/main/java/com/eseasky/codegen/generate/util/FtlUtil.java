package com.eseasky.codegen.generate.util;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class FtlUtil
{
  public static Configuration getConfig(List<File> paramList, String paramString1, String paramString2)
    throws IOException
  {
    Configuration config = new Configuration();

    FileTemplateLoader[] arrayOfFileTemplateLoader = new FileTemplateLoader[paramList.size()];
    for (int i = 0; i < paramList.size(); i++) {
      arrayOfFileTemplateLoader[i] = new FileTemplateLoader((File)paramList.get(i));
    }
    MultiTemplateLoader localMultiTemplateLoader = new MultiTemplateLoader(arrayOfFileTemplateLoader);

    config.setTemplateLoader(localMultiTemplateLoader);
    config.setNumberFormat("###############");
    config.setBooleanFormat("true,false");
    config.setDefaultEncoding(paramString1);

    return config;
  }

  public static List<String> a(String paramString1, String paramString2) {
    String[] arrayOfString = b(paramString1, "\\/");
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(paramString2);
    localArrayList.add(File.separator + paramString2);
    String str = "";
    for (int i = 0; i < arrayOfString.length; i++) {
      str = str + File.separator + arrayOfString[i];
      localArrayList.add(str + File.separator + paramString2);
    }
    return localArrayList;
  }

  public static String[] b(String paramString1, String paramString2)
  {
    if (paramString1 == null) return new String[0];
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString1, paramString2);
    ArrayList localArrayList = new ArrayList();

    while (localStringTokenizer.hasMoreElements()) {
      Object localObject = localStringTokenizer.nextElement();
      localArrayList.add(localObject.toString());
    }
    return (String[])localArrayList.toArray(new String[localArrayList.size()]);
  }

  public static String repalceParam(String paramString, Map<String, Object> paramMap, Configuration paramConfiguration) {
    StringWriter localStringWriter = new StringWriter();
    try {
      Template localTemplate = new Template("templateString...", new StringReader(paramString), paramConfiguration);
      localTemplate.process(paramMap, localStringWriter);
      return localStringWriter.toString(); } catch (Exception localException) {
      throw new IllegalStateException("cannot process templateString:" + paramString + " cause:" + localException, localException);
    }
  }

  public static void flushFile(Template paramTemplate, Map<String, Object> paramMap, File paramFile, String paramString) throws IOException, TemplateException
  {
    BufferedWriter localBufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(paramFile), paramString));

    paramMap.put("Format", new SimpleFormat());
    paramTemplate.process(paramMap, localBufferedWriter);
    localBufferedWriter.close();
  }
}