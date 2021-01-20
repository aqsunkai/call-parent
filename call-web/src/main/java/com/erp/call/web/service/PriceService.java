package com.erp.call.web.service;

import com.erp.call.web.dto.PriceReq;
import com.erp.call.web.util.FileUtil;
import com.erp.call.web.util.HttpClientUtil;
import com.erp.call.web.util.NumberUtil;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * @author sunkai
 * @description
 * @date 2021/1/17 10:54
 */
@Service
public class PriceService {

    private static final Logger logger = LoggerFactory.getLogger(PriceService.class);

    public Set<String> calculate(PriceReq priceReq) {
        TreeMap<Double, String> map = getPriceMap(priceReq.getCalPattern());
        File file = new File(priceReq.getFilePath().trim());
        File[] files = file.listFiles();
        assert files != null;
        List<String> calPriceFolder = Arrays.asList(priceReq.getProperty().replace(" ", "").replace("，", ",").split(","));
        Set<String> errorFolder = Sets.newHashSet();
        for (File folder : files) {
            // 循环产品文件夹
            File[] productFiles = folder.listFiles();
            assert productFiles != null;
            String priceStr = "";
            // 查询价格
            try {
                for (File productFile : productFiles) {
                    if (productFile.getName().endsWith(".url")) {
                        String url = FileUtil.readPrefixLine(folder.getName(), "URL=", productFile);
                        if (StringUtils.isEmpty(url)) {
                            break;
                        }
                        String str;
                        try {
                            str = HttpClientUtil.getWithString(url, null);
                        } catch (Exception e) {
                            logger.error("文件夹：" + folder.getName() + "查询价格失败");
                            break;
                        }
                        if (str.contains("formatedActivityPrice")) {
                            str = str.substring(str.indexOf("formatedActivityPrice\":\"US $") + 28);
                            str = str.substring(0, str.indexOf("\","));
                        } else {
                            str = str.substring(str.indexOf("formatedPrice\":\"US $") + 20);
                            str = str.substring(0, str.indexOf("\","));
                        }
                        double price;
                        if (str.contains("-")) {
                            price = Double.parseDouble(str.split("-")[1]);
                        } else {
                            price = Double.parseDouble(str);
                        }
                        // 查询自己卖的价格
                        priceStr = calculatePrice(price, map);
                    }
                }
                if (StringUtils.isEmpty(priceStr)) {
                    errorFolder.add(folder.getName());
                    continue;
                }
                // 设置价格
                for (File productFile : productFiles) {
                    if (calPriceFolder.contains(productFile.getName()) && productFile.isDirectory()) {
                        File[] imageFiles = productFile.listFiles();
                        if (null != imageFiles) {
                            int i = 1;
                            for (File imageFile : imageFiles) {
                                String imageName = imageFile.getName();
                                String imagePath = imageFile.getAbsolutePath();
                                String newName = priceStr + " (" + i + ")" + imageName.substring(imageName.lastIndexOf("."));
                                File newFile = new File(imagePath.substring(0, imagePath.indexOf(imageName)) + newName);
                                imageFile.renameTo(newFile);
                                i++;
                            }
                        }
                    }
                }
                logger.info("文件夹：" + folder.getName() + "修改价格成功");
            } catch (Exception e) {
                errorFolder.add(folder.getName());
            }
        }
        return errorFolder;
    }

    private String calculatePrice(double price, TreeMap<Double, String> map) {
        String previous = "";
        double previousKey = 0D;
        for (Map.Entry<Double, String> entry : map.entrySet()) {
            if (price > entry.getKey()) {
                if (Math.abs(NumberUtil.formatDouble(previousKey - price)) > Math.abs(NumberUtil.formatDouble(price - entry.getKey()))) {
                    return entry.getValue();
                } else {
                    return previous;
                }
            }
            previousKey = entry.getKey();
            previous = entry.getValue();
        }
        return previous;
    }

    private TreeMap<Double, String> getPriceMap(String calPattern) {
        String[] patterns = calPattern.split("\n|\n|\t");
        TreeMap<Double, String> map = new TreeMap<>(Comparator.reverseOrder());
        double d1 = 0.001D;
        for (String pattern : patterns) {
            String[] priceStr = pattern.replace(" ", "").split(":");
            if (priceStr[0].startsWith(">")) {
                throw new RuntimeException("不支持使用>号");
            } else if (priceStr[0].contains("-")) {
                map.put(Double.parseDouble(priceStr[0].split("-")[0]) + d1, priceStr[1]);
                map.put(Double.parseDouble(priceStr[0].split("-")[1]) - d1, priceStr[1]);
            } else if (priceStr[0].startsWith("<=")) {
                map.put(Double.parseDouble(priceStr[0].replace("<=", "")), priceStr[1]);
            } else if (priceStr[0].startsWith("<")) {
                map.put(Double.parseDouble(priceStr[0].replace("<", "")) - d1, priceStr[1]);
            } else {
                map.put(Double.valueOf(priceStr[0]), priceStr[1]);
            }
        }
        return map;
    }

//    public static void main(String[] args) {
//        String ca = "<4:14.97\n" + //3.999以下
//                "        4-5:15.97\n" + //4.001-4.999
//                "        6-7:16.97\n" +
//                "        8:17.97\n" +
//                "        9:18.97\n" +
//                "        10:19.97\n" +
//                "        11:20.97\n" +
//                "        12:21.97\n" +
//                "        13:22.97\n" +
//                "        14:23.97\n" +
//                "        15:24.97\n" +
//                "        16:25.97\n" +
//                "        17:26.97\n" +
//                "        18:27.97\n" +
//                "        19:28.97\n" +
//                "        20:29.97";
//        TreeMap<Double, String> treeMap = getPriceMap(ca);
//        System.out.println(treeMap);
//        System.out.println(calculatePrice(3.9999D, treeMap));
//    }
}
