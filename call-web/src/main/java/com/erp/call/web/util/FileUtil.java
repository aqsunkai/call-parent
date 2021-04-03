package com.erp.call.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static List<String> txt2ListString(String fileName, File file) {
        List<String> result = new ArrayList<>();
        BufferedReader br = null;
        try {
            //构造一个BufferedReader类来读取文件
            br = new BufferedReader(new FileReader(file));
            String s;
            //使用readLine方法，一次读一行
            while ((s = br.readLine()) != null) {
                result.add(s);
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
        return result;
    }

    /**
     * 指定读取第几行
     *
     * @param fileName
     * @param file
     * @param line     第几行
     * @return
     */
    public static String readExplicitLine(String fileName, File file, int line) {
        StringBuilder result = new StringBuilder();
        BufferedReader br = null;
        try {
            //构造一个BufferedReader类来读取文件
            br = new BufferedReader(new FileReader(file));
            for (int i = 1; i < line; i++) {
                br.readLine();
            }
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

    //    public static void main(String[] args) throws IOException {
//        File file = new File("D:\\call\\aaaaaaaaaa\\1 (114)\\下图记录.txt");
//        BufferedReader br = new BufferedReader(new FileReader(file));
//        System.out.println(br.readLine());
//    }
    public static void writerFile(String content, String url) {
        boolean flag = true;
        try {
            //创建File对象，参数为String类型，表示目录名
            File myFile = new File(url);
            //判断文件是否存在，如果不存在则调用createNewFile()方法创建新目录，否则跳至异常处理代码
            if (!myFile.exists()) {
                myFile.createNewFile();
            } else {  //如果不存在则扔出异常
                throw new Exception("The new file already exists!");
            }
            //下面把数据写入创建的文件，首先新建文件名为参数创建FileWriter对象
            FileWriter resultFile = new FileWriter(myFile);
            //把该对象包装进PrinterWriter对象
            PrintWriter myNewFile = new PrintWriter(resultFile);
            //再通过PrinterWriter对象的println()方法把字符串数据写入新建文件
            myNewFile.println(content);
            resultFile.close();   //关闭文件写入流
        } catch (Exception ex) {
            System.out.println("无法创建新文件！");
            flag = false;
        }
    }

}
