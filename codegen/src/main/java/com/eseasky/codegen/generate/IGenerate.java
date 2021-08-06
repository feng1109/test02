package com.eseasky.codegen.generate;

import java.util.Map;

public abstract interface IGenerate
{
  public abstract Map<String, Object> getTableInfo()
    throws Exception;

  public abstract void generateCodeFile()
    throws Exception;

  public abstract void generateCodeFile(String paramString1, String paramString2)
    throws Exception;
}