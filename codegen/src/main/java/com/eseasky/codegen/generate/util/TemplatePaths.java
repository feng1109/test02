package com.eseasky.codegen.generate.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplatePaths
{
  private static final Logger logger = LoggerFactory.getLogger(TemplatePaths.class);
  private String templatePath;
  private List<File> classpath = new ArrayList();
  private String stylePath;

  public TemplatePaths(String paramString)
  {
    logger.info("----templatePath-----------------" + paramString);
    this.templatePath = paramString;
  }

  private void a(File paramFile) {
    a(new File[] { paramFile });
  }

  private void a(File[] paramArrayOfFile) {
    this.classpath = Arrays.asList(paramArrayOfFile);
  }

  public String a() {
    return this.stylePath;
  }

  public void a(String paramString) {
    this.stylePath = paramString;
  }

  public List<File> getClassPath() {
    String packagePath = getClass().getResource(this.templatePath).getFile();
    packagePath = packagePath.replaceAll("%20", " ");
    try {
      packagePath = java.net.URLDecoder.decode(packagePath,"utf-8");
    } catch (UnsupportedEncodingException e) {
      System.out.println("获取路径异常"+e);
    }
    logger.debug("-------classpath-------" + packagePath);
    a(new File(packagePath));
    return this.classpath;
  }

  public void a(List<File> paramList) {
    this.classpath = paramList;
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("{\"templateRootDirs\":\"");
    localStringBuilder.append(this.classpath);
    localStringBuilder.append("\",\"stylePath\":\"");
    localStringBuilder.append(this.stylePath);
    localStringBuilder.append("\"} ");
    return localStringBuilder.toString();
  }
}