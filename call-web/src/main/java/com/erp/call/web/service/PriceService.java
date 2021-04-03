package com.erp.call.web.service;

import com.erp.call.web.dto.PageRes;
import com.erp.call.web.dto.PriceReq;
import com.erp.call.web.util.FileUtil;
import com.erp.call.web.util.HttpClientUtil;
import com.erp.call.web.util.NumberUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author sunkai
 * @description
 * @date 2021/1/17 10:54
 */
@Service
public class PriceService {

    private static final Logger logger = LoggerFactory.getLogger(PriceService.class);

    private ExecutorService executors = Executors.newCachedThreadPool();

    private volatile Map<String, Boolean> runningMap = new ConcurrentHashMap<>();
    private Map<String, List<String>> FAIL_NAME = new ConcurrentHashMap<>();

    public void calculatePrice(PriceReq priceReq) {
        File file = checkFilePath(priceReq);
        executors.execute(() -> calculate(priceReq, file));
    }

    private File checkFilePath(PriceReq priceReq) {
        if (Boolean.TRUE.equals(runningMap.get(priceReq.getFilePath().trim()))) {
            throw new RuntimeException("该文件夹正在修改价格，请耐心等待！");
        }
        File file = new File(priceReq.getFilePath().trim());
        if (!file.exists()) {
            throw new RuntimeException("文件夹地址不正确！");
        }
        runningMap.put(priceReq.getFilePath().trim(), true);
        FAIL_NAME.remove(priceReq.getFilePath().trim());
        return file;
    }

    public void calculate(PriceReq priceReq, File file) {
        Set<String> errorFolder = Sets.newHashSet();
        try {
            TreeMap<Double, String> map = null;
            boolean ifOriginalPrice = "原价".equals(priceReq.getCalPattern());
            if (!ifOriginalPrice) {
                map = getPriceMap(priceReq.getCalPattern());
            }
            File[] files = file.listFiles();
            assert files != null;
            List<String> calPriceFolder = Arrays.asList(priceReq.getProperty().replace(" ", "").replace("，", ",").split(","));
            for (File folder : files) {
                // 循环产品文件夹
                File[] productFiles = folder.listFiles();
                assert productFiles != null;
                String priceStr = "";
                // 查询价格
                try {
                    for (File productFile : productFiles) {
                        if (productFile.getName().endsWith(".url")) {
                            TimeUnit.SECONDS.sleep(1);
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
                            double price;
                            if (priceReq.getSourcePlatform() == 0) {
                                price = getPriceFromAliExpress(priceReq, str);
                            } else {
                                //1688
                                str = str.substring(str.indexOf("refPrice:'") + 10);
                                str = str.substring(0, str.indexOf("',"));
                                price = Double.parseDouble(str);
                            }
                            // 查询自己卖的价格
                            if (ifOriginalPrice) {
                                priceStr = String.valueOf(NumberUtil.formatStringScale2(price));
                            } else {
                                priceStr = calculatePrice(price, map);
                            }
                            break;
                        }
                    }
                    if (StringUtils.isEmpty(priceStr)) {
                        logger.info("文件夹：" + folder.getName() + "修改价格失败：未获取到价格");
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
                    logger.info("文件夹：" + folder.getName() + "修改价格异常");
                    errorFolder.add(folder.getName());
                }
            }
        } finally {
            FAIL_NAME.put(priceReq.getFilePath().trim(), Lists.newArrayList(errorFolder));
            runningMap.put(priceReq.getFilePath().trim(), false);
        }
    }

    private double getPriceFromAliExpress(PriceReq priceReq, String str) {
        double price;// 速卖通
        if (priceReq.getValueType() == 0) {
            // 优先使用促销价格
            if (str.contains("formatedActivityPrice")) {
                str = str.substring(str.indexOf("formatedActivityPrice\":\"US $") + 28);
            } else {
                str = str.substring(str.indexOf("formatedPrice\":\"US $") + 20);
            }
        } else {
            // 强制使用完整价格
            str = str.substring(str.indexOf("formatedPrice\":\"US $") + 20);
        }
        str = str.substring(0, str.indexOf("\","));
        if (str.contains("-")) {
            if (priceReq.getIntervalType() == 0) {
                // 取最小值
                price = Double.parseDouble(str.split("-")[0]);
            } else if (priceReq.getIntervalType() == 2) {
                // 取最大值
                price = Double.parseDouble(str.split("-")[1]);
            } else {
                price = NumberUtil.formatDoubleScale2((Double.parseDouble(str.split("-")[0]) + Double.parseDouble(str.split("-")[1])) / 2);
            }
        } else {
            price = Double.parseDouble(str);
        }
        return price;
    }

    private String calculatePrice(double price, TreeMap<Double, String> map) {
        String previous = "";
        double previousKey = 0D;
        String originPrice = String.valueOf(NumberUtil.formatStringScale2(price));
        for (Map.Entry<Double, String> entry : map.entrySet()) {
            if (price > entry.getKey()) {
                if (Math.abs(NumberUtil.formatDoubleScale3(previousKey - price)) > Math.abs(NumberUtil.formatDoubleScale3(price - entry.getKey()))) {
                    return "原价".equals(entry.getValue()) ? originPrice : entry.getValue();
                } else {
                    return "原价".equals(previous) ? originPrice : previous;
                }
            }
            previousKey = entry.getKey();
            previous = entry.getValue();
        }
        return "原价".equals(previous) ? originPrice : previous;
    }

    private TreeMap<Double, String> getPriceMap(String calPattern) {
        TreeMap<Double, String> map = new TreeMap<>(Comparator.reverseOrder());
        String[] patterns = calPattern.split("\n|\n|\t");
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

    public String changePrice(PriceReq priceReq) {
        TreeMap<Double, String> treeMap = getPriceMap(priceReq.getCalPattern());
        Double maxPrice = treeMap.keySet().iterator().next();
        String[] prices = priceReq.getFrontPrice().split("\n|\n|\t");
        StringBuilder sb = new StringBuilder();
        for (String s : prices) {
            try {
                double price = Double.parseDouble(s);
                if (price <= maxPrice) {
                    sb.append(calculatePrice(price, treeMap));
                } else {
                    sb.append("超过").append(maxPrice);
                }
            } catch (Exception ignored) {
                sb.append("不是数字");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public PageRes getCalculateResult(String filePath) {
        PageRes res = new PageRes();
        res.setRunning(runningMap.get(filePath));
        res.setFailName(FAIL_NAME.get(filePath));
        return res;
    }

    public static void main(String[] args) {
        String ca = "<=16:29.97\n" +
            "17:30.97\n" +
            "18:31.97\n" +
            "19:32.97\n" +
            "20:33.97\n" +
            "21:34.97\n" +
            "22:35.97\n" +
            "23:36.97\n" +
            "24:37.97\n" +
            "25:38.97\n" +
            "26:39.97\n" +
            "27:40.97\n" +
            "28:41.97\n" +
            "29:42.97\n" +
            "30:43.97";
//        TreeMap<Double, String> treeMap = getPriceMap(ca);
//        changeTxtPrice(treeMap);
    }

//    private static void changeTxtPrice(TreeMap<Double, String> treeMap) {
//        File file = new File("C:\\Users\\sunkai9203\\Desktop\\33.txt");
//        List<String> s1 = FileUtil.txt2ListString(file.getName(), file);
//        StringBuilder sb = new StringBuilder();
//        for (String s : s1) {
//            try {
//                double price = Double.parseDouble(s);
//                if (price <= 30) {
//                    sb.append(calculatePrice(price, treeMap));
//                } else {
//                    sb.append("超过30");
//                }
//            } catch (Exception ignored) {
//                sb.append("不是数字");
//            }
//            sb.append("\n");
//        }
//        FileUtil.writerFile(sb.toString(), "C:\\Users\\sunkai9203\\Desktop\\44.txt");
//    }

}
