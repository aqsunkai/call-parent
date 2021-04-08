package com.erp.call.web.service;

import com.erp.call.web.constant.ProductNameData;
import com.erp.call.web.constant.RequestData;
import com.erp.call.web.dto.PageReq;
import com.erp.call.web.dto.PageRes;
import com.erp.call.web.dto.ProductRes;
import com.erp.call.web.dto.UploadRes;
import com.erp.call.web.util.FileUtil;
import com.erp.call.web.util.HttpClientHelper;
import com.erp.call.web.util.IDGeneratorUtil;
import com.erp.call.web.util.NumberUtil;
import com.erp.call.web.util.PlayerUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.erp.call.web.constant.HttpConstant.PRODUCT_URL;
import static com.erp.call.web.constant.HttpConstant.UPLOAD_URL;
import static com.erp.call.web.constant.HttpConstant.getRequestHeader;

@Service
public class CallService {

    private Logger logger = LoggerFactory.getLogger(CallService.class);

    private ExecutorService executors = Executors.newCachedThreadPool();

    private volatile Map<String, Boolean> runningMap = new ConcurrentHashMap<>();

    private Map<String, List<String>> FAIL_NAME = new ConcurrentHashMap<>();
    private Map<String, List<String>> UPLOAD_NAME = new ConcurrentHashMap<>();

    @Autowired
    private HttpClientHelper httpClientHelper;
    @Autowired
    private PlayerUtil playerUtil;

    @Autowired
    private IDGeneratorUtil idGeneratorUtil;

    public Integer sendProduct(PageReq pageReq) {
        File file = checkFilePath(pageReq);
        File[] files = file.listFiles();
        assert files != null;
        executors.execute(() -> createProduct(pageReq, files));
        return files.length;
    }

//    public void sendErpProduct(PageReq pageReq) {
//        File file = checkFilePath(pageReq);
//        executors.execute(() -> createErpProduct(pageReq, file));
//    }

    private File checkFilePath(PageReq pageReq) {
        if (Boolean.TRUE.equals(runningMap.get(pageReq.getFilePath()))) {
            throw new RuntimeException("该文件夹下产品正在创建，请勿重复创建！");
        }
        File file = new File(pageReq.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("文件夹地址不正确！");
        }
        runningMap.put(pageReq.getFilePath(), true);
        FAIL_NAME.remove(pageReq.getFilePath());
        UPLOAD_NAME.remove(pageReq.getFilePath());
        return file;
    }

    private void createProduct(PageReq pageReq, File[] files) {
        try {
            Map<String, String> variationTransferMap = getVariationTransferMap(pageReq.getVariationTransfer());
            int count = 0;
            int length = files.length;
            for (File productFile : files) {
                count++;
                if (productFile.isHidden()) {
                    continue;
                }
                File[] imageFiles = productFile.listFiles();
                assert imageFiles != null;
                String fileName = productFile.getName();
                String productName = null;
                Double priceTxt = null;
                Map<String, String> masterName = Maps.newLinkedHashMap();
                boolean upload = true;
                List<File> slaveFiles = Lists.newArrayList();
                List<String> slaveFileMd5 = Lists.newArrayList();
                for (File imageFile : imageFiles) {
                    if (imageFile.isHidden()) {
                        continue;
                    }
                    if (pageReq.getProperty().equals(imageFile.getName())) {
                        File[] masterFiles = imageFile.listFiles();
                        if (null == masterFiles || masterFiles.length == 0) {
                            if (pageReq.getAttachType() == 1) {
                                logger.warn("文件夹名称：{}，{}目录下没有图片，该文件夹下所有产品都不会创建", fileName, pageReq.getProperty());
                                upload = false;
                                break;
                            } else {
                                // 等于0、2、3时忽略列表文件夹
                                logger.warn("文件夹名称：{}，{}目录下没有图片", fileName, pageReq.getProperty());
                                continue;
                            }
                        }
                        Arrays.sort(masterFiles);
                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getProperty());
                        for (File masterFile : masterFiles) {
                            if (masterFile.isHidden()) {
                                continue;
                            }
                            // 上传列表文件夹图片
                            UploadRes res = httpClientHelper.postFile(UPLOAD_URL, masterFile, "file", getRequestHeader(pageReq.getCookie()), UploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getRet())) {
                                masterName.put(res.getInfo().getMd5(), masterFile.getName().substring(0, masterFile.getName().lastIndexOf(".")));
                                logger.info("文件夹名称：{}，{}目录，上传{}图片成功", fileName, pageReq.getProperty(), masterFile.getName());
                                sleepMoment();
                            } else {
                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败", fileName, pageReq.getProperty(), masterFile.getName());
                                sleepMomentFail();
                            }
                        }
                    } else if (pageReq.getAttachProperty().equals(imageFile.getName())) {
                        List<File> masterFiles = Lists.newArrayList();
                        File[] listFiles = imageFile.listFiles();
                        assert listFiles != null;
                        Arrays.sort(listFiles);
                        if (pageReq.getAttachType() == 0) {
                            // 列表文件夹和详情文件夹平级，即两个文件夹都作为主图
                            masterFiles.addAll(Arrays.asList(listFiles));
                        } else if (pageReq.getAttachType() == 1) {
                            // 列表文件夹作为主图，详情文件夹作为附图
                            slaveFiles.addAll(Arrays.asList(listFiles));
                            continue;
                        } else if (pageReq.getAttachType() == 2) {
                            // 列表文件夹和详情文件夹第一张作为主图，详情文件夹剩下的图片作为附图
                            boolean flag = true;
                            for (File listFile : listFiles) {
                                if (listFile.isHidden()) {
                                    continue;
                                }
                                if (flag) {
                                    masterFiles.add(listFile);
                                } else {
                                    slaveFiles.add(listFile);
                                }
                                flag = false;
                            }
                        } else if (pageReq.getAttachType() == 3) {
                            // 列表文件夹和详情文件夹第一张作为主图，详情文件夹剩下的图片丢弃
                            masterFiles.add(listFiles[0]);
                        }
                        if (masterFiles.size() == 0) {
                            logger.warn("文件夹名称：{}，{}目录下没有图片", fileName, pageReq.getAttachProperty());
                            continue;
                        }
                        // 详情文件夹图片排序
                        masterFiles.sort(Comparator.comparing(File::getName));
                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getAttachProperty());
                        for (File masterFile : masterFiles) {
                            if (masterFile.isHidden()) {
                                continue;
                            }
                            // 上传主图图片
                            UploadRes res = httpClientHelper.postFile(UPLOAD_URL, masterFile, "file", getRequestHeader(pageReq.getCookie()), UploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getRet())) {
                                masterName.put(res.getInfo().getMd5(), masterFile.getName().substring(0, masterFile.getName().lastIndexOf(".")));
                                slaveFileMd5.add(res.getInfo().getMd5());
                                logger.info("文件夹名称：{}，{}目录，上传{}图片成功", fileName, pageReq.getAttachProperty(), masterFile.getName());
                                sleepMoment();
                            } else {
                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败", fileName, pageReq.getAttachProperty(), masterFile.getName());
                                sleepMomentFail();
                            }
                        }
                    } else if (imageFile.getName().startsWith(pageReq.getProductNameFile())) {
                        if (pageReq.getType() == 1) {
                            // txt文件夹名称
                            productName = FileUtil.readExplicitLine(fileName, imageFile, 1);
                            if (StringUtils.isBlank(productName)) {
                                logger.warn("文件夹名称：{}，读取{}文件，第一行产品名称不存在，该文件夹下所有产品都不会创建", fileName, pageReq.getProductNameFile());
                                upload = false;
                                break;
                            }
                            productName = ProductNameData.changeProductName(productName);
                        }
                        if (pageReq.getPriceType() == 3) {
                            String priceTxt1 = FileUtil.readExplicitLine(fileName, imageFile, 2);
                            priceTxt = changePrice(priceTxt1);
                            if (null == priceTxt) {
                                logger.warn("文件夹名称：{}，读取{}文件，第二行不是数字，该文件夹下所有产品都不会创建", fileName, pageReq.getProductNameFile());
                                upload = false;
                                break;
                            }
                        }
                    }
                }
                if (!upload || masterName.size() == 0) {
                    uploadFail(pageReq, fileName);
                    continue;
                }
                boolean uploadSlave = true;
                Map<String, String> slaveName = Maps.newLinkedHashMap();
                if (slaveFiles.size() > 0) {
                    // 详情文件夹图片排序
                    slaveFiles.sort(Comparator.comparing(File::getName));
                    // 上传详情文件夹图片
                    logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getAttachProperty());
                    for (File slaveFile : slaveFiles) {
                        if (slaveFile.isHidden()) {
                            continue;
                        }
                        UploadRes res = httpClientHelper.postFile(UPLOAD_URL, slaveFile, "file", getRequestHeader(pageReq.getCookie()), UploadRes.class);
                        if (null != res && Boolean.TRUE.equals(res.getRet())) {
                            slaveName.put(res.getInfo().getMd5(), slaveFile.getName().substring(0, slaveFile.getName().lastIndexOf(".")));
                            logger.info("文件夹名称：{}，{}目录，上传{}图片成功", fileName, pageReq.getAttachProperty(), slaveFile.getName());
                            sleepMoment();
                        } else {
                            logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，该文件夹下所有产品都不会创建", fileName, pageReq.getAttachProperty(), slaveFile.getName());
                            uploadSlave = false;
                            break;
                        }
                    }
                }
                if (!uploadSlave) {
                    uploadFail(pageReq, fileName);
                    continue;
                }
                int failCount = 0;

                if (Boolean.TRUE.equals(pageReq.getNewProduct())) {
                    // 新变体产品，文件夹下产品合并上传到一个产品
                    Map<String, String> variationMap = new LinkedHashMap<>();
                    Map<String, String> variationPriceMap = new HashMap<>();
                    int i = 1;
                    String incrPrefix = StringUtils.isNotEmpty(pageReq.getIncrPrefix()) ? pageReq.getIncrPrefix() : "";
                    List<String> variationList = Lists.newArrayList();
                    for (Map.Entry<String, String> master : masterName.entrySet()) {
                        String variationName;
                        if (pageReq.getVariation() == 0) {
                            variationName = ProductNameData.changeProductName(master.getValue());
                            if (variationTransferMap.containsKey(variationName)) {
                                String variationNameTransfer = variationTransferMap.get(variationName);
                                if (!variationList.contains(variationNameTransfer)) {
                                    variationName = variationNameTransfer;
                                    variationList.add(variationName);
                                }
                            }
                        } else {
                            variationName = incrPrefix + NumberUtil.changeInt(i);
                        }
                        variationMap.put(master.getKey(), variationName);
                        Double price = pageReq.getPriceType() == 0 ? getPriceFromPictureName(fileName, pageReq, master, slaveFileMd5) : (pageReq.getPriceType() == 3 ? priceTxt : null);
                        variationPriceMap.put(master.getKey(), null == price ? "0" : String.valueOf(price));
                        i++;
                    }
                    Map<String, String> variationMap1;
                    if (pageReq.getVariation() == 0) {
                        variationMap1 = new LinkedHashMap<>();
                        // 白色、黑色展示在前面
                        variationMap.forEach((k, v) -> {
                            if ("White".equals(v)) {
                                variationMap1.put(k, v);
                            }
                            if ("Black".equals(v)) {
                                variationMap1.put(k, v);
                            }
                        });
                        variationMap.forEach((k, v) -> {
                            if (!"White".equals(v) && !"Black".equals(v)) {
                                variationMap1.put(k, v);
                            }
                        });
                    } else {
                        variationMap1 = variationMap;
                    }
                    ProductRes res = httpClientHelper.put(PRODUCT_URL, RequestData.getNewProductReq(pageReq, productName, variationMap1, variationPriceMap,
                        new ArrayList<>(slaveName.keySet())), getRequestHeader(pageReq.getCookie()), ProductRes.class);
                    if (null == res || Boolean.TRUE.equals(res.getCheckFail())) {
                        logger.warn("文件夹名称：{}，变体产品上传失败", fileName);
                        failCount++;
                    } else {
                        logger.info("文件夹名称：{}，变体产品上传成功", fileName);
                    }
                    sleepMomentForProduct();
                } else {
                    for (Map.Entry<String, String> master : masterName.entrySet()) {
                        String product = pageReq.getType() == 1 ? productName : ProductNameData.changeProductName(master.getValue());
                        Double price = pageReq.getPriceType() == 0 ? getPriceFromPictureName(fileName, pageReq, master, slaveFileMd5) : (pageReq.getPriceType() == 3 ? priceTxt : null);
                        String brandName = pageReq.getPriceType() == 1 ? master.getValue() : null;
                        ProductRes res = httpClientHelper.put(PRODUCT_URL, RequestData.getProductReq(pageReq, product, brandName, price, master.getKey(),
                            new ArrayList<>(slaveName.keySet())), getRequestHeader(pageReq.getCookie()), ProductRes.class);
                        if (null == res || Boolean.TRUE.equals(res.getCheckFail())) {
                            if (slaveFileMd5.contains(master.getKey())) {
                                logger.warn("文件夹名称：{}，{}目录下{}产品上传失败", fileName, pageReq.getAttachProperty(), master.getValue());
                            } else {
                                logger.warn("文件夹名称：{}，{}目录下{}产品上传失败", fileName, pageReq.getProperty(), master.getValue());
                            }
                            failCount++;
                        } else {
                            if (slaveFileMd5.contains(master.getKey())) {
                                logger.info("文件夹名称：{}，{}目录下{}产品上传成功", fileName, pageReq.getAttachProperty(), master.getValue());
                            } else {
                                logger.info("文件夹名称：{}，{}目录下{}产品上传成功", fileName, pageReq.getProperty(), master.getValue());
                            }
                        }
                        sleepMomentForProduct();
                    }
                }
                if (failCount == 0) {
                    logger.info("第{}个文件夹名称：{}，所有产品都上传成功", count, fileName);
                    uploadSuccess(pageReq, fileName);
                } else {
                    logger.warn("第{}个文件夹名称：{}，有{}个产品上传失败", count, fileName, failCount);
                    uploadFail(pageReq, fileName);
                }
                if (count < length) {
                    sleepMoment();
                }
            }
        } catch (Exception e) {
            logger.error("异步创建产品失败", e);
        } finally {
            runningMap.put(pageReq.getFilePath(), false);
            if (pageReq.getPlayMusic() == 1) {
                playerUtil.play();
            }
        }
    }

    private Map<String, String> getVariationTransferMap(String variationTransfer) {
        Map<String, String> variationTransferMap = Maps.newHashMap();
        if (StringUtils.isEmpty(variationTransfer)) {
            return variationTransferMap;
        }
        String[] splits = variationTransfer.split(",");
        for (String split : splits) {
            String[] va = split.split(":");
            variationTransferMap.put(va[0], va[1]);
        }
        Map<String, String> maps = Maps.newHashMap();
        for (Map.Entry<String, String> entry : variationTransferMap.entrySet()) {
            if (!entry.getKey().endsWith("色")) {
                maps.put(entry.getKey() + "色", entry.getValue());
            } else {
                maps.put(entry.getKey().substring(0, entry.getKey().length() - 1), entry.getValue());
            }
        }
        variationTransferMap.putAll(maps);
        Map<String, String> maps1 = Maps.newHashMap();
        for (Map.Entry<String, String> entry : variationTransferMap.entrySet()) {
            maps1.put("浅" + entry.getKey(), "Light " + entry.getValue());
            maps1.put("深" + entry.getKey(), "Dark " + entry.getValue());
        }
        variationTransferMap.putAll(maps1);
        return variationTransferMap;
    }

    private Double getPriceFromPictureName(String fileName, PageReq pageReq, Map.Entry<String, String> master, List<String> slaveFileMd5) {
        try {
            String price = master.getValue().replace("（", "(");
            if (price.contains("(")) {
                price = price.split("\\(")[0];
            }
            return Double.valueOf(price);
        } catch (Exception e) {
            if (slaveFileMd5.contains(master.getKey())) {
                logger.warn("文件夹名称：{}，{}目录下{}产品名称不是数字，该产品不生成价格", fileName, pageReq.getAttachProperty(), master.getValue());
            } else {
                logger.warn("文件夹名称：{}，{}目录下{}产品名称不是数字，该产品不生成价格", fileName, pageReq.getProperty(), master.getValue());
            }
        }
        return null;
    }

    private Double changePrice(String price) {
        try {
            return Double.valueOf(price);
        } catch (Exception ignored) {
        }
        return null;
    }

//    private void createErpProduct(PageReq pageReq, File file) {
//        try {
//            File[] files = file.listFiles();
//            assert files != null;
//            for (File productFile : files) {
//                File[] imageFiles = productFile.listFiles();
//                assert imageFiles != null;
//                String fileName = productFile.getName();
//                String productName = null;
//                String price = "0";
//                Map<String, String> masterSize = Maps.newHashMap();
//                Map<String, String> masterUrl = Maps.newHashMap();
//                Map<String, String> masterName = Maps.newLinkedHashMap();
//                boolean upload = true;
//                List<File> slaveFiles = Lists.newArrayList();
//                List<String> slaveFileIds = Lists.newArrayList();
//                for (File imageFile : imageFiles) {
//                    if (pageReq.getProperty().equals(imageFile.getName())) {
//                        File[] masterFiles = imageFile.listFiles();
//                        if (null == masterFiles || masterFiles.length == 0) {
//                            if (pageReq.getAttachType() == 1) {
//                                logger.warn("文件夹名称：{}，{}目录下没有图片，该文件夹下所有产品都不会创建", fileName, pageReq.getProperty());
//                                upload = false;
//                                break;
//                            } else {
//                                // 等于0、2、3时忽略列表文件夹
//                                logger.warn("文件夹名称：{}，{}目录下没有图片", fileName, pageReq.getProperty());
//                                continue;
//                            }
//                        }
//                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getProperty());
//                        for (File masterFile : masterFiles) {
//                            // 上传属性图图片
//                            ErpUploadRes res = httpClientHelper.postFile(ERP_UPLOAD_URL, masterFile, "imgs", getErpRequestHeader(pageReq.getCookie(), true), ErpUploadRes.class);
//                            if (null != res && Boolean.TRUE.equals(res.getState())) {
//                                masterName.put(res.getData().getId(), masterFile.getName().substring(0, masterFile.getName().lastIndexOf(".")));
//                                masterUrl.put(res.getData().getId(), res.getData().getUrl());
//                                masterSize.put(res.getData().getId(), res.getData().getSize().toString());
//                                logger.info("文件夹名称：{}，{}目录，上传{}图片成功", fileName, pageReq.getProperty(), masterFile.getName());
//                                sleepMoment();
//                            } else {
//                                String errMsg = "";
//                                if (null != res) {
//                                    errMsg = res.getMsg();
//                                }
//                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，失败原因：{}", fileName, pageReq.getProperty(), masterFile.getName(), errMsg);
//                                sleepMomentFail();
//                            }
//                        }
//                    } else if (pageReq.getAttachProperty().equals(imageFile.getName())) {
//                        List<File> masterFiles = Lists.newArrayList();
//                        File[] listFiles = imageFile.listFiles();
//                        if (pageReq.getAttachType() == 0) {
//                            // 列表文件夹和详情文件夹平级，即两个文件夹都作为主图
//                            masterFiles.addAll(Arrays.asList(listFiles));
//                        } else if (pageReq.getAttachType() == 1) {
//                            // 列表文件夹作为主图，详情文件夹作为附图
//                            slaveFiles.addAll(Arrays.asList(listFiles));
//                            continue;
//                        } else if (pageReq.getAttachType() == 2) {
//                            // 列表文件夹和详情文件夹第一张作为主图，详情文件夹剩下的图片作为附图
//                            boolean flag = true;
//                            for (File listFile : listFiles) {
//                                if (flag) {
//                                    masterFiles.add(listFile);
//                                } else {
//                                    slaveFiles.add(listFile);
//                                }
//                                flag = false;
//                            }
//                        } else if (pageReq.getAttachType() == 3) {
//                            // 列表文件夹和详情文件夹第一张作为主图，详情文件夹剩下的图片丢弃
//                            masterFiles.add(listFiles[0]);
//                        }
//                        if (masterFiles.size() == 0) {
//                            logger.warn("文件夹名称：{}，{}目录下没有图片", fileName, pageReq.getAttachProperty());
//                            continue;
//                        }
//                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getAttachProperty());
//                        for (File masterFile : masterFiles) {
//                            // 上传主图图片
//                            ErpUploadRes res = httpClientHelper.postFile(ERP_UPLOAD_URL, masterFile, "imgs", getErpRequestHeader(pageReq.getCookie(), true), ErpUploadRes.class);
//                            if (null != res && Boolean.TRUE.equals(res.getState())) {
//                                masterName.put(res.getData().getId(), masterFile.getName().substring(0, masterFile.getName().lastIndexOf(".")));
//                                masterUrl.put(res.getData().getId(), res.getData().getUrl());
//                                masterSize.put(res.getData().getId(), res.getData().getSize().toString());
//                                slaveFileIds.add(res.getData().getId());
//                                logger.info("文件夹名称：{}，{}目录，上传{}图片成功", fileName, pageReq.getAttachProperty(), masterFile.getName());
//                                sleepMoment();
//                            } else {
//                                String errMsg = "";
//                                if (null != res) {
//                                    errMsg = res.getMsg();
//                                }
//                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，失败原因：{}", fileName, pageReq.getAttachProperty(), masterFile.getName(), errMsg);
//                                sleepMomentFail();
//                            }
//                        }
//                    } else if (pageReq.getType() == 1 && imageFile.getName().startsWith(pageReq.getProductNameFile())) {
//                        // 产品名称文件夹名称
//                        productName = FileUtil.readFirstLine(fileName, imageFile);
//                        if (StringUtils.isEmpty(productName)) {
//                            logger.warn("文件夹名称：{}，读取{}文件，产品名称不存在，该文件夹下所有产品都不会创建", fileName, pageReq.getProductNameFile());
//                            upload = false;
//                            break;
//                        }
//                    }
//                }
//                if (!upload || masterName.size() == 0) {
//                    uploadFail(pageReq, fileName);
//                    continue;
//                }
//                int failCount = 0;
//                for (Map.Entry<String, String> master : masterName.entrySet()) {
//                    boolean uploadSlave = true;
//                    Map<String, String> slaveSize = Maps.newHashMap();
//                    Map<String, String> slaveUrl = Maps.newHashMap();
//                    if (slaveFiles.size() > 0) {
//                        // 上传主图图片
//                        logger.info("文件夹名称：{}，{}目录下图片开始上传{}产品所需图片", fileName, pageReq.getAttachProperty(), master.getValue());
//                        for (File slaveFile : slaveFiles) {
//                            // 上传主图图片
//                            ErpUploadRes res = httpClientHelper.postFile(ERP_UPLOAD_URL, slaveFile, "imgs", getErpRequestHeader(pageReq.getCookie(), true), ErpUploadRes.class);
//                            if (null != res && Boolean.TRUE.equals(res.getState())) {
//                                slaveUrl.put(res.getData().getId(), res.getData().getUrl());
//                                slaveSize.put(res.getData().getId(), res.getData().getSize().toString());
//                                logger.info("文件夹名称：{}，{}目录，上传{}图片成功，{}产品", fileName, pageReq.getAttachProperty(), slaveFile.getName(), master.getValue());
//                            } else {
//                                String errMsg = "";
//                                if (null != res) {
//                                    errMsg = res.getMsg();
//                                }
//                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，失败原因：{}，导致{}产品无法创建", fileName, pageReq.getAttachProperty(), slaveFile.getName(), errMsg, master.getValue());
//                                uploadSlave = false;
//                                break;
//                            }
//                            sleepMoment();
//                        }
//                    }
//                    if (!uploadSlave) {
//                        uploadFail(pageReq, fileName);
//                        continue;
//                    }
//                    String product = pageReq.getType() == 1 ? productName : master.getValue();
//                    ErpProductRes res = httpClientHelper.postFormData(ERP_PRODUCT_URL, RequestData.getErpProductReq(product, price, master.getKey(),
//                            masterUrl.get(master.getKey()), masterSize.get(master.getKey()),
//                            slaveUrl, slaveSize, idGeneratorUtil), getErpRequestHeader(pageReq.getCookie(), false), ErpProductRes.class);
//                    if (null == res || res.getCode() != 1) {
//                        String errMsg = "";
//                        if (null != res) {
//                            errMsg = res.getMsg();
//                        }
//                        if (pageReq.getAttachType() == 0 && slaveFileIds.contains(master.getKey())) {
//                            logger.warn("文件夹名称：{}，{}目录下{}产品上传失败，失败原因：{}", fileName, pageReq.getAttachProperty(), master.getValue(), errMsg);
//                        } else {
//                            logger.warn("文件夹名称：{}，{}目录下{}产品上传失败，失败原因：{}", fileName, pageReq.getProperty(), master.getValue(), errMsg);
//                        }
//                        failCount++;
//                    } else {
//                        if (pageReq.getAttachType() == 0 && slaveFileIds.contains(master.getKey())) {
//                            logger.info("文件夹名称：{}，{}目录下{}产品上传成功", fileName, pageReq.getAttachProperty(), master.getValue());
//                        } else {
//                            logger.info("文件夹名称：{}，{}目录下{}产品上传成功", fileName, pageReq.getProperty(), master.getValue());
//                        }
//                    }
//                    sleepMomentForProduct();
//                }
//                if (failCount == 0) {
//                    logger.info("文件夹名称：{}，所有产品都上传成功", fileName);
//                    uploadSuccess(pageReq, fileName);
//                } else {
//                    logger.warn("文件夹名称：{}，有{}个产品上传失败", fileName, failCount);
//                    uploadFail(pageReq, fileName);
//                }
//                sleepMomentFail();
//            }
//        } catch (Exception e) {
//            logger.error("异步创建产品失败", e);
//        } finally {
//            runningMap.put(pageReq.getFilePath(), false);
//        }
//    }

    private void sleepMoment() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
    }

    private void sleepMomentForProduct() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
    }

    private void sleepMomentFail() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
    }

    /**
     * 产品上传成功
     */
    private void uploadSuccess(PageReq pageReq, String fileName) {
        List<String> uploads = UPLOAD_NAME.get(pageReq.getFilePath());
        if (CollectionUtils.isEmpty(uploads)) {
            uploads = Lists.newArrayList();
        }
        uploads.add(fileName);
        UPLOAD_NAME.put(pageReq.getFilePath(), uploads);
    }

    /**
     * 产品上传失败
     */
    private void uploadFail(PageReq pageReq, String fileName) {
        List<String> uploads = FAIL_NAME.get(pageReq.getFilePath());
        if (CollectionUtils.isEmpty(uploads)) {
            uploads = Lists.newArrayList();
        }
        uploads.add(fileName);
        FAIL_NAME.put(pageReq.getFilePath(), uploads);
    }

    public PageRes getProductResult(String filePath) {
        PageRes res = new PageRes();
        res.setRunning(runningMap.get(filePath));
        res.setFailName(FAIL_NAME.get(filePath));
        res.setUploadName(UPLOAD_NAME.get(filePath));
        return res;
    }
}
