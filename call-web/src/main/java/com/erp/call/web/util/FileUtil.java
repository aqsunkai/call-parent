package com.erp.call.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static String txt2String(String fileName, File file) {
        StringBuilder result = new StringBuilder();
        BufferedReader br = null;
        try {
            //构造一个BufferedReader类来读取文件
            br = new BufferedReader(new FileReader(file));
            String s;
            //使用readLine方法，一次读一行
            while ((s = br.readLine()) != null) {
                result.append(s).append("\n");
            }
            br.close();
        } catch (Exception e) {
            logger.error("读取" + fileName + "下的" + file.getName() + "文件失败", e);
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (Exception e) {
                    logger.error("关闭" + fileName + "下的" + file.getName() + "文件流失败", e);
                }
            }
        }
        return result.toString();
    }

    public static String readFirstLine(String fileName, File file) {
        StringBuilder result = new StringBuilder();
        BufferedReader br = null;
        try {
            //构造一个BufferedReader类来读取文件
            br = new BufferedReader(new FileReader(file));
            result.append(br.readLine());
            br.close();
        } catch (Exception e) {
            logger.error("读取" + fileName + "下的" + file.getName() + "文件失败", e);
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (Exception e) {
                    logger.error("关闭" + fileName + "下的" + file.getName() + "文件流失败", e);
                }
            }
        }
        return result.toString();
    }

    public static String readPrefixLine(String fileName, String prefix, File file) {
        BufferedReader br = null;
        try {
            //构造一个BufferedReader类来读取文件
            br = new BufferedReader(new FileReader(file));
            String s;
            //使用readLine方法，一次读一行
            while ((s = br.readLine()) != null) {
                if (s.startsWith(prefix)) {
                    return s.replace(prefix, "");
                }
            }
            br.close();
        } catch (Exception e) {
            logger.error("读取" + fileName + "下的" + file.getName() + "文件失败", e);
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (Exception e) {
                    logger.error("关闭" + fileName + "下的" + file.getName() + "文件流失败", e);
                }
            }
        }
        return "";
    }

    public static void main(String[] args) throws IOException {
        File file = new File("D:\\call\\aaaaaaaaaa\\1 (114)\\下图记录.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        System.out.println(br.readLine());
    }
}
