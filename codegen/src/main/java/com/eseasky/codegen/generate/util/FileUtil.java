package com.eseasky.codegen.generate.util;

import com.eseasky.codegen.config.Config;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    protected static String encode = "UTF-8";

    protected void createFileByTemplate(TemplatePaths templates, String projectPath, Map<String, Object> tableInfoMap)
            throws Exception {
        logger.debug("--------projectPath--------" + projectPath);
        for (int i = 0; i < templates.getClassPath().size(); i++) {
            File templateDir = (File) templates.getClassPath().get(i);
            createAllFile(projectPath, templateDir, tableInfoMap, templates);
        }
    }

    protected void createAllFile(String projectPath, File templateDir, Map<String, Object> tableInfoMap, TemplatePaths templates) throws Exception {
        if (templateDir == null)
            throw new IllegalStateException("'templateRootDir' must be not null");
        logger.debug("-------------------load template from templateRootDir = '" + templateDir.getAbsolutePath() + "' outJavaRootDir:" +
                new File(Config.source_root_package
                        .replace(".", File.separator))
                        .getAbsolutePath() + "' outWebappRootDir:" +
                new File(Config.webroot_package
                        .replace(".", File.separator))
                        .getAbsolutePath());

        List templatesList = DirFileUtil.maniDir(templateDir);
        logger.debug("----srcFiles----size-----------" + templatesList.size());
        logger.debug("----srcFiles----list------------" + templatesList.toString());
        for (int i = 0; i < templatesList.size(); i++) {
            File file = (File) templatesList.get(i);
            createFileByTemplate(projectPath, templateDir, tableInfoMap, file, templates);
        }
    }

    protected void createFileByTemplate(String projectPath, File filedir, Map<String, Object> tableInfoMap, File file, TemplatePaths templates) throws Exception {
        logger.debug("-------templateRootDir--" + filedir.getPath());
        logger.debug("-------srcFile--" + file.getPath());

        String srcfile = DirFileUtil.a(filedir, file);
        try {
            logger.debug("-------templateFile--" + srcfile);
            String sourcefile = replaceLabel(tableInfoMap, srcfile, templates);
            logger.debug("-------outputFilepath--" + sourcefile);
            String sourceSrcDir;
            String sourceDir;
            if (sourcefile.startsWith("java")) {
                sourceSrcDir = projectPath + File.separator + Config.source_root_package.replace(".", File.separator);

                sourceDir = sourceSrcDir;
                sourcefile = sourcefile.substring("java".length());
                sourcefile = sourceDir + sourcefile;
                logger.debug("-------java----outputFilepath--" + sourcefile);
                createFile(srcfile, sourcefile, tableInfoMap, templates);
            } else if (sourcefile.startsWith("webapp")) {
                sourceSrcDir = projectPath + File.separator + Config.webroot_package.replace(".", File.separator);

                sourceDir = sourceSrcDir;
                sourcefile = sourcefile.substring("webapp".length());
                sourcefile = sourceDir + sourcefile;
                logger.debug("-------webapp---outputFilepath---" + sourcefile);
                createFile(srcfile, sourcefile, tableInfoMap, templates);
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            logger.error(localException.toString());
        }
    }

    protected void createFile(String srcfile, String sourcefile, Map<String, Object> tableInfoMap, TemplatePaths templates)
            throws Exception {
        if (sourcefile.endsWith("source_root_package")) {
            sourcefile = sourcefile.substring(0, sourcefile.length() - 1);
        }

        Template localTemplate = getFileTemplate(srcfile, templates);
        localTemplate.setOutputEncoding(encode);
        File templateFile = DirFileUtil.mkdirs(sourcefile);
        logger.debug("[generate]\t template:" + srcfile + " ==> " + sourcefile);
        FtlUtil.flushFile(localTemplate, tableInfoMap, templateFile, encode);
        if (hasFieldRequiredNum(templateFile))
            createSubFile(templateFile, "#segment#");
    }

    protected Template getFileTemplate(String paramString, TemplatePaths templates) throws IOException {
        return FtlUtil.getConfig(templates.getClassPath(), encode, paramString).getTemplate(paramString);
    }

    protected boolean hasFieldRequiredNum(File paramFile) {
        return paramFile.getName().startsWith("[1-n]");
    }

    protected static void createSubFile(File paramFile, String paramString) {
        InputStreamReader localInputStreamReader = null;
        BufferedReader localBufferedReader = null;
        ArrayList localArrayList = new ArrayList();
        try {
            localInputStreamReader = new InputStreamReader(new FileInputStream(paramFile), "UTF-8");
            localBufferedReader = new BufferedReader(localInputStreamReader);

            int m = 0;
            OutputStreamWriter localOutputStreamWriter = null;
            String str1;
            while ((str1 = localBufferedReader.readLine()) != null) {
                if ((str1.trim().length() > 0) && (str1.startsWith(paramString))) {
                    String str2 = str1.substring(paramString.length());
                    String str3 = paramFile.getParentFile().getAbsolutePath();
                    str2 = str3 + File.separator + str2;
                    logger.debug("[generate]\t split file:" + paramFile.getAbsolutePath() + " ==> " + str2);

                    localOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(str2), "UTF-8");
                    localArrayList.add(localOutputStreamWriter);
                    m = 1;
                    continue;
                }
                if (m != 0) {
                    logger.debug("row : " + str1);
                    localOutputStreamWriter.append(str1 + "\r\n");
                }
            }
            for (int n = 0; n < localArrayList.size(); n++) {
                ((Writer) localArrayList.get(n)).close();
            }
            localBufferedReader.close();
            localInputStreamReader.close();

            logger.debug("[generate]\t delete file:" + paramFile.getAbsolutePath());
            fileDelete(paramFile);
        } catch (FileNotFoundException localIOException2) {
            int i;
            localIOException2.printStackTrace();
        } catch (IOException localIOException4) {
            int j;
            localIOException4.printStackTrace();
        } finally {
            try {
                int k;
                if (localBufferedReader != null) {
                    localBufferedReader.close();
                }
                if (localInputStreamReader != null) {
                    localInputStreamReader.close();
                }
                if (localArrayList.size() > 0) {
                    for (int i1 = 0; i1 < localArrayList.size(); i1++)
                        if (localArrayList.get(i1) != null)
                            ((Writer) localArrayList.get(i1)).close();
                }
            } catch (IOException localIOException5) {
                localIOException5.printStackTrace();
            }
        }
    }

    protected static String replaceLabel(Map<String, Object> tableInfoMap, String srcFile, TemplatePaths paths) throws Exception {
        String filepath = srcFile;

        int i = -1;
        if ((i = srcFile.indexOf('@')) != -1) {
            filepath = srcFile.substring(0, i);
            String localObject1 = srcFile.substring(i + 1);
            Object localObject2 = tableInfoMap.get(localObject1);
            if (localObject2 == null) {
                System.err.println("[not-generate] WARN: test expression is null by key:[" + (String) localObject1 + "] on template:[" + srcFile + "]");
                return null;
            }
            if (!"true".equals(String.valueOf(localObject2))) {
                logger.error("[not-generate]\t test expression '@" + (String) localObject1 + "' is false,template:" + srcFile);
                return null;
            }
        }
        Configuration config = FtlUtil.getConfig(paths.getClassPath(), encode, "/");
        filepath = FtlUtil.repalceParam(filepath, tableInfoMap, config);
        //TODO 区别jeecg的javai模板改用.vm模板处理
        filepath = filepath.substring(0,filepath.lastIndexOf("."));//去掉.vm模板

        String subfilepath = filepath.substring(filepath.lastIndexOf("."));
        String newfilepath = filepath.substring(0, filepath.lastIndexOf(".")).replace(".", File.separator);
        filepath = newfilepath + subfilepath;
        return filepath;
    }

    protected static boolean fileDelete(File paramFile) {
        boolean bool = false;
        int i = 0;
        while ((!bool) && (i++ < 10)) {
            System.gc();
            bool = paramFile.delete();
        }
        return bool;
    }

    protected static String generatePath(String paramString1, String paramString2) {
        int i = 1;
        int j = 1;
        do {
            int k = paramString1.indexOf(paramString2) == 0 ? 1 : 0;
            int m = paramString1.lastIndexOf(paramString2) + 1 == paramString1.length() ? paramString1.lastIndexOf(paramString2) : paramString1.length();
            paramString1 = paramString1.substring(k, m);
            i = paramString1.indexOf(paramString2) == 0 ? 1 : 0;
            j = paramString1.lastIndexOf(paramString2) + 1 == paramString1.length() ? 1 : 0;
        } while ((i != 0) || (j != 0));
        return paramString1;
    }
}