package com.erp.call.web.service;

import com.erp.call.web.constant.RequestData;
import com.erp.call.web.dto.*;
import com.erp.call.web.util.FileUtil;
import com.erp.call.web.util.HttpClientHelper;
import com.erp.call.web.util.IDGeneratorUtil;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.erp.call.web.constant.HttpConstant.*;

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
    private IDGeneratorUtil idGeneratorUtil;

    public void sendProduct(PageReq pageReq) {
        File file = checkFilePath(pageReq);
        executors.execute(() -> createProduct(pageReq, file));
    }

    public void sendErpProduct(PageReq pageReq) {
        File file = checkFilePath(pageReq);
        executors.execute(() -> createErpProduct(pageReq, file));
    }

    private File checkFilePath(PageReq pageReq) {
        File file = new File(pageReq.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("文件夹地址不正确！");
        }
        if (Boolean.TRUE.equals(runningMap.get(pageReq.getFilePath()))) {
            throw new RuntimeException("该文件夹下产品正在创建，请勿重复创建！");
        }
        runningMap.put(pageReq.getFilePath(), true);
        FAIL_NAME.remove(pageReq.getFilePath());
        UPLOAD_NAME.remove(pageReq.getFilePath());
        return file;
    }

    private void createProduct(PageReq pageReq, File file) {
        try {
            File[] files = file.listFiles();
            assert files != null;
            for (File productFile : files) {
                File[] imageFiles = productFile.listFiles();
                assert imageFiles != null;
                String fileName = productFile.getName();
                String productName = null;
                Double price = null;
                Map<String, String> masterMd5 = Maps.newHashMap();
                Map<String, String> slaveMd5 = Maps.newHashMap();
                String firstSlaveMd5 = null;
                String firstSlaveFileName = null;
                boolean upload = true;
                pro:
                for (File imageFile : imageFiles) {
                    if (pageReq.getProperty().equals(imageFile.getName())) {
                        File[] masterFiles = imageFile.listFiles();
                        if (null == masterFiles || masterFiles.length == 0) {
                            logger.warn("文件夹名称：{}，{}目录下没有图片，正常上传", fileName, pageReq.getProperty());
                            continue;
                        }
                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getProperty());
                        for (File masterFile : masterFiles) {
                            // 上传属性图图片
                            UploadRes res = httpClientHelper.postFile(UPLOAD_URL, masterFile, "file", getRequestHeader(pageReq.getCookie()), UploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getRet())) {
                                masterMd5.put(res.getInfo().getMd5(), masterFile.getName().substring(0, masterFile.getName().lastIndexOf(".")));
                            } else {
                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，该文件夹下所有产品都不会创建", fileName, pageReq.getProperty(), masterFile.getName());
                                upload = false;
                                break pro;
                            }
                            sleepMoment();
                        }
                    } else if (pageReq.getAttachProperty().equals(imageFile.getName())) {
                        File[] slaveFiles = imageFile.listFiles();
                        if (null == slaveFiles || slaveFiles.length == 0) {
                            logger.warn("文件夹名称：{}，{}目录下没有图片，正常上传", fileName, pageReq.getAttachProperty());
                            continue;
                        }
                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getAttachProperty());
                        for (File slaveFile : slaveFiles) {
                            // 上传主图图片
                            UploadRes res = httpClientHelper.postFile(UPLOAD_URL, slaveFile, "file", getRequestHeader(pageReq.getCookie()), UploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getRet())) {
                                slaveMd5.put(res.getInfo().getMd5(), slaveFile.getName().substring(0, slaveFile.getName().lastIndexOf(".")));
                                if (StringUtils.isEmpty(firstSlaveMd5)) {
                                    firstSlaveMd5 = res.getInfo().getMd5();
                                    firstSlaveFileName = slaveFile.getName();
                                }
                            } else {
                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，该文件夹下所有产品都不会创建", fileName, pageReq.getAttachProperty(), slaveFile.getName());
                                upload = false;
                                break pro;
                            }
                            sleepMoment();
                        }
                    } else if (pageReq.getType() == 1 && imageFile.getName().startsWith(pageReq.getProductNameFile())) {
                        // 产品名称文件夹名称
                        String txt = FileUtil.txt2String(fileName, imageFile);
                        if (StringUtils.isNotEmpty(txt)) {
                            if (txt.contains("@")) {
                                price = Double.valueOf(txt.split("@")[0]);
                                productName = txt.split("@")[1];
                                productName = productName.split("- AliExpress")[0];
                                productName = productName.substring(0, productName.length() - 5);
                            } else {
                                productName = txt.split("- AliExpress")[0];
                                productName = productName.substring(0, productName.length() - 5);
                            }
                        }
                        if (StringUtils.isEmpty(productName)) {
                            logger.warn("文件夹名称：{}，读取{}文件，产品名称不存在，该文件夹下所有产品都不会创建", fileName, pageReq.getProductNameFile());
                            upload = false;
                            break;
                        }
                    }
                }
                if (!upload) {
                    uploadFail(pageReq, fileName);
                    continue;
                }
                boolean slaveFlag = false;
                if (masterMd5.isEmpty() && !slaveMd5.isEmpty()) {
                    logger.warn("文件夹名称：{}，{}目录不存在图片，使用{}目录下{}图片作为属性图", fileName, pageReq.getProperty(), pageReq.getAttachProperty(), firstSlaveFileName);
                    masterMd5.put(firstSlaveMd5, slaveMd5.get(firstSlaveMd5));
                    slaveMd5.remove(firstSlaveMd5);
                    slaveFlag = true;
                }
                if (masterMd5.isEmpty()) {
                    logger.warn("文件夹名称：{}，{}目录和{}目录都不存在图片，已自动忽略该文件夹", fileName, pageReq.getProperty(), pageReq.getAttachProperty());
                    continue;
                }
                int failCount = 0;
                for (Map.Entry<String, String> md5 : masterMd5.entrySet()) {
                    String product = pageReq.getType() == 1 ? productName : md5.getValue();
                    ProductRes res = httpClientHelper.put(PRODUCT_URL, RequestData.getProductReq(product, price, md5.getKey(),
                            new ArrayList<>(slaveMd5.keySet())), getRequestHeader(pageReq.getCookie()), ProductRes.class);
                    if (null == res || Boolean.TRUE.equals(res.getCheckFail())) {
                        logger.warn("文件夹名称：{}，{}目录下{}产品上传失败，自动上传下一个", fileName, slaveFlag ? pageReq.getAttachProperty() : pageReq.getProperty(), md5.getValue());
                        failCount++;
                    }
                    sleepMoment();
                }
                if (failCount == 0) {
                    logger.info("文件夹名称：{}，所有产品都上传成功", fileName);
                    uploadSuccess(pageReq, fileName);
                } else {
                    logger.warn("文件夹名称：{}，有{}个产品上传失败", fileName, failCount);
                    uploadFail(pageReq, fileName);
                }
            }
        } catch (Exception e) {
            logger.error("异步上传产品失败", e);
        } finally {
            runningMap.put(pageReq.getFilePath(), false);
        }
    }

    private void createErpProduct(PageReq pageReq, File file) {
        try {
            File[] files = file.listFiles();
            assert files != null;
            for (File productFile : files) {
                File[] imageFiles = productFile.listFiles();
                assert imageFiles != null;
                String fileName = productFile.getName();
                String productName = null;
                String price = "0";
                Map<String, String> masterSize = Maps.newHashMap();
                Map<String, String> masterUrl = Maps.newHashMap();
                Map<String, String> masterName = Maps.newHashMap();
                boolean upload = true;
                File[] slaveFiles = null;
                for (File imageFile : imageFiles) {
                    if (pageReq.getProperty().equals(imageFile.getName())) {
                        File[] masterFiles = imageFile.listFiles();
                        if (null == masterFiles || masterFiles.length == 0) {
                            logger.warn("文件夹名称：{}，{}目录下没有图片，该文件夹下所有产品都不会创建", fileName, pageReq.getProperty());
                            upload = false;
                            break;
                        }
                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getProperty());
                        for (File masterFile : masterFiles) {
                            // 上传属性图图片
                            ErpUploadRes res = httpClientHelper.postFile(ERP_UPLOAD_URL, masterFile, "imgs", getErpRequestHeader(pageReq.getCookie(), true), ErpUploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getState())) {
                                masterName.put(res.getData().getId(), masterFile.getName().substring(0, masterFile.getName().lastIndexOf(".")));
                                masterUrl.put(res.getData().getId(), res.getData().getUrl());
                                masterSize.put(res.getData().getId(), res.getData().getSize().toString());
                                logger.info("文件夹名称：{}，{}目录，上传{}图片成功", fileName, pageReq.getProperty(), masterFile.getName());
                                sleepMoment();
                            } else {
                                String errMsg = "";
                                if (null != res) {
                                    errMsg = res.getMsg();
                                }
                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，失败原因：{}", fileName, pageReq.getProperty(), masterFile.getName(), errMsg);
                                sleepMomentFail();
                                //upload = false;
//                                break pro;
                            }
                        }
                    } else if (pageReq.getAttachProperty().equals(imageFile.getName())) {
                        slaveFiles = imageFile.listFiles();
                    } else if (pageReq.getType() == 1 && imageFile.getName().startsWith(pageReq.getProductNameFile())) {
                        // 产品名称文件夹名称
                        String txt = FileUtil.txt2String(fileName, imageFile);
                        if (StringUtils.isNotEmpty(txt)) {
                            if (txt.contains("@")) {
                                price = txt.split("@")[0];
                                productName = txt.split("@")[1];
                                productName = productName.split("- AliExpress")[0];
                                productName = productName.substring(0, productName.length() - 5);
                            } else {
                                productName = txt.split("- AliExpress")[0];
                                productName = productName.substring(0, productName.length() - 5);
                            }
                        }
                        if (StringUtils.isEmpty(productName)) {
                            logger.warn("文件夹名称：{}，读取{}文件，产品名称不存在，该文件夹下所有产品都不会创建", fileName, pageReq.getProductNameFile());
                            upload = false;
                            break;
                        }
                    }
                }
                if (!upload || masterName.size() == 0) {
                    uploadFail(pageReq, fileName);
                    continue;
                }
                int failCount = 0;
                for (Map.Entry<String, String> master : masterName.entrySet()) {
                    boolean uploadSlave = true;
                    Map<String, String> slaveSize = Maps.newHashMap();
                    Map<String, String> slaveUrl = Maps.newHashMap();
                    if (null != slaveFiles && slaveFiles.length > 0) {
                        // 上传主图图片
                        logger.info("文件夹名称：{}，{}目录下图片开始上传{}产品所需图片", fileName, pageReq.getAttachProperty(), master.getValue());
                        for (File slaveFile : slaveFiles) {
                            // 上传主图图片
                            ErpUploadRes res = httpClientHelper.postFile(ERP_UPLOAD_URL, slaveFile, "imgs", getErpRequestHeader(pageReq.getCookie(), true), ErpUploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getState())) {
                                slaveUrl.put(res.getData().getId(), res.getData().getUrl());
                                slaveSize.put(res.getData().getId(), res.getData().getSize().toString());
                                logger.info("文件夹名称：{}，{}目录，上传{}图片成功，{}产品", fileName, pageReq.getAttachProperty(), slaveFile.getName(), master.getValue());
                            } else {
                                String errMsg = "";
                                if (null != res) {
                                    errMsg = res.getMsg();
                                }
                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，失败原因：{}，导致{}产品无法创建", fileName, pageReq.getAttachProperty(), slaveFile.getName(), errMsg, master.getValue());
                                uploadSlave = false;
                                break;
                            }
                            sleepMoment();
                        }
                    }
                    if (!uploadSlave) {
                        uploadFail(pageReq, fileName);
                        continue;
                    }
                    String product = pageReq.getType() == 1 ? productName : master.getValue();
                    ErpProductRes res = httpClientHelper.postFormData(ERP_PRODUCT_URL, RequestData.getErpProductReq(product, price, master.getKey(),
                            masterUrl.get(master.getKey()), masterSize.get(master.getKey()),
                            slaveUrl, slaveSize, idGeneratorUtil), getErpRequestHeader(pageReq.getCookie(), false), ErpProductRes.class);
                    if (null == res || res.getCode() != 1) {
                        String errMsg = "";
                        if (null != res) {
                            errMsg = res.getMsg();
                        }
                        logger.warn("文件夹名称：{}，{}目录下{}产品上传失败，失败原因：{}", fileName, pageReq.getProperty(), master.getValue(), errMsg);
                        failCount++;
                    } else {
                        logger.info("文件夹名称：{}，{}目录下{}产品上传成功", fileName, pageReq.getProperty(), master.getValue());
                    }
                    sleepMomentForProduct();
                }
                if (failCount == 0) {
                    logger.info("文件夹名称：{}，所有产品都上传成功", fileName);
                    uploadSuccess(pageReq, fileName);
                } else {
                    logger.warn("文件夹名称：{}，有{}个产品上传失败", fileName, failCount);
                    uploadFail(pageReq, fileName);
                }
                sleepMomentFail();
            }
        } catch (Exception e) {
            logger.error("异步创建产品失败", e);
        } finally {
            runningMap.put(pageReq.getFilePath(), false);
        }
    }

    private void sleepMoment() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
    }

    private void sleepMomentForProduct() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
    }

    private void sleepMomentFail() throws InterruptedException {
        TimeUnit.SECONDS.sleep(30);
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
