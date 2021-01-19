package com.erp.call.web.service;

import com.erp.call.web.dto.PriceReq;
import com.erp.call.web.util.FileUtil;
import com.erp.call.web.util.HttpClientUtil;
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
        TreeMap<Integer, String> map = getPriceMap(priceReq.getCalPattern());
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

    private String calculatePrice(double price, TreeMap<Integer, String> map) {
        String previous = "";
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (price > entry.getKey()) {
                return previous;
            }
            previous = entry.getValue();
        }
        return previous;
    }

    private TreeMap<Integer, String> getPriceMap(String calPattern) {
        String[] patterns = calPattern.split("\n|\n|\t");
        TreeMap<Integer, String> map = new TreeMap<>(Comparator.reverseOrder());
        for (String pattern : patterns) {
            String[] priceStr = pattern.replace(" ", "").split(":");
            if (priceStr[0].startsWith(">")) {
                throw new RuntimeException("不支持使用>号");
            } else if (priceStr[0].contains("-")) {
                map.put(Integer.valueOf(priceStr[0].split("-")[1]), priceStr[1]);
            } else if (priceStr[0].startsWith("<=")) {
                map.put(Integer.parseInt(priceStr[0].replace("<=", "")) + 1, priceStr[1]);
            } else if (priceStr[0].startsWith("<")) {
                map.put(Integer.parseInt(priceStr[0].replace("<", "")), priceStr[1]);
            } else {
                map.put(Integer.valueOf(priceStr[0]), priceStr[1]);
            }
        }
        return map;
    }

}
