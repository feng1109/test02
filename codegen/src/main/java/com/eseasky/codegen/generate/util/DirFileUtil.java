package com.eseasky.codegen.generate.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DirFileUtil
{
  private static final Logger logger = LoggerFactory.getLogger(DirFileUtil.class);

  public static List<String> a = new ArrayList();
  public static List<String> b = new ArrayList();

  public static List<File> maniDir(File paramFile)
    throws IOException
  {
    ArrayList localArrayList = new ArrayList();
    a(paramFile, localArrayList);
    /*Collections.sort(localArrayList, new Comparator() {
      public int db_type(File paramFile1, File paramFile2) {
        return paramFile1.getAbsolutePath().compareTo(paramFile2.getAbsolutePath());
      }
    });*/
    return localArrayList;
  }

  public static void a(File paramFile, List<File> paramList) throws IOException {
    logger.debug("---------dir------------path: " + paramFile.getPath() + " -- isHidden --: " + paramFile.isHidden() + " -- isDirectory --: " + paramFile.isDirectory());
    if ((!paramFile.isHidden()) && (paramFile.isDirectory()) && (!d(paramFile))) {
      File[] arrayOfFile = paramFile.listFiles();
      for (int i = 0; i < arrayOfFile.length; i++) {
        a(arrayOfFile[i], paramList);
      }
    }
    else if ((!e(paramFile)) && (!d(paramFile))) {
      paramList.add(paramFile);
    }
  }

  public static String a(File filedir, File file)
  {
    if (filedir.equals(file))
      return "";
    if (filedir.getParentFile() == null)
      return file.getAbsolutePath().substring(filedir.getAbsolutePath().length());
    return file.getAbsolutePath().substring(filedir.getAbsolutePath().length() + 1);
  }

  public static boolean b(File paramFile)
  {
    if (paramFile.isDirectory()) return false;
    return a(paramFile.getName());
  }

  public static boolean a(String paramString) {
    return !StringUtils.isBlank(b(paramString));
  }

  public static String b(String paramString)
  {
    if (paramString == null) {
      return null;
    }
    int i = paramString.indexOf(".");
    if (i == -1) {
      return "";
    }
    return paramString.substring(i + 1);
  }

  public static File mkdirs(String paramString)
  {
    if (paramString == null) throw new IllegalArgumentException("file must be not null");
    File localFile = new File(paramString);
    mkdirs(localFile);
    return localFile;
  }

  public static void mkdirs(File paramFile) {
    if (paramFile.getParentFile() != null)
      paramFile.getParentFile().mkdirs();
  }

  private static boolean d(File paramFile)
  {
    for (int i = 0; i < a.size(); i++) {
      if (paramFile.getName().equals(a.get(i))) {
        return true;
      }
    }
    return false;
  }

  private static boolean e(File paramFile) {
    for (int i = 0; i < b.size(); i++) {
      if (paramFile.getName().endsWith((String)b.get(i))) {
        return true;
      }
    }
    return false;
  }

  static
  {
    a.add(".svn");
    a.add("CVS");
    a.add(".cvsignore");
    a.add(".copyarea.db");
    a.add("SCCS");
    a.add("vssver.scc");
    a.add(".DS_Store");
    a.add(".git");
    a.add(".gitignore");
    b.add(".ftl");
  }
}