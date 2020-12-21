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
import java.util.Arrays;
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
                Map<String, String> masterName = Maps.newLinkedHashMap();
                boolean upload = true;
                List<File> slaveFiles = Lists.newArrayList();
                List<String> slaveFileMd5 = Lists.newArrayList();
                for (File imageFile : imageFiles) {
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
                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getProperty());
                        for (File masterFile : masterFiles) {
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
                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getAttachProperty());
                        for (File masterFile : masterFiles) {
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
                    } else if (pageReq.getType() == 1 && imageFile.getName().startsWith(pageReq.getProductNameFile())) {
                        // txt文件夹名称
                        productName = FileUtil.readFirstLine(fileName, imageFile);
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
                    Map<String, String> slaveName = Maps.newHashMap();
                    if (slaveFiles.size() > 0) {
                        // 上传主图图片
                        logger.info("文件夹名称：{}，{}目录下图片开始上传{}产品所需图片", fileName, pageReq.getAttachProperty(), master.getValue());
                        for (File slaveFile : slaveFiles) {
                            UploadRes res = httpClientHelper.postFile(UPLOAD_URL, slaveFile, "file", getRequestHeader(pageReq.getCookie()), UploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getRet())) {
                                slaveName.put(res.getInfo().getMd5(), slaveFile.getName().substring(0, slaveFile.getName().lastIndexOf(".")));
                                logger.info("文件夹名称：{}，{}目录，上传{}图片成功，{}产品", fileName, pageReq.getAttachProperty(), slaveFile.getName(), master.getValue());
                                sleepMoment();
                            } else {
                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，导致{}产品无法创建", fileName, pageReq.getAttachProperty(), slaveFile.getName(), master.getValue());
                                uploadSlave = false;
                                break;
                            }
                        }
                    }
                    if (!uploadSlave) {
                        uploadFail(pageReq, fileName);
                        continue;
                    }
                    String product = pageReq.getType() == 1 ? productName : master.getValue();
                    ProductRes res = httpClientHelper.put(PRODUCT_URL, RequestData.getProductReq(pageReq.getCustomDefs(), product, price, master.getKey(),
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
                Map<String, String> masterName = Maps.newLinkedHashMap();
                boolean upload = true;
                List<File> slaveFiles = Lists.newArrayList();
                List<String> slaveFileIds = Lists.newArrayList();
                for (File imageFile : imageFiles) {
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
                            }
                        }
                    } else if (pageReq.getAttachProperty().equals(imageFile.getName())) {
                        List<File> masterFiles = Lists.newArrayList();
                        File[] listFiles = imageFile.listFiles();
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
                        logger.info("文件夹名称：{}，{}目录下图片开始上传", fileName, pageReq.getAttachProperty());
                        for (File masterFile : masterFiles) {
                            // 上传主图图片
                            ErpUploadRes res = httpClientHelper.postFile(ERP_UPLOAD_URL, masterFile, "imgs", getErpRequestHeader(pageReq.getCookie(), true), ErpUploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getState())) {
                                masterName.put(res.getData().getId(), masterFile.getName().substring(0, masterFile.getName().lastIndexOf(".")));
                                masterUrl.put(res.getData().getId(), res.getData().getUrl());
                                masterSize.put(res.getData().getId(), res.getData().getSize().toString());
                                slaveFileIds.add(res.getData().getId());
                                logger.info("文件夹名称：{}，{}目录，上传{}图片成功", fileName, pageReq.getAttachProperty(), masterFile.getName());
                                sleepMoment();
                            } else {
                                String errMsg = "";
                                if (null != res) {
                                    errMsg = res.getMsg();
                                }
                                logger.warn("文件夹名称：{}，{}目录，上传{}图片失败，失败原因：{}", fileName, pageReq.getAttachProperty(), masterFile.getName(), errMsg);
                                sleepMomentFail();
                            }
                        }
                    } else if (pageReq.getType() == 1 && imageFile.getName().startsWith(pageReq.getProductNameFile())) {
                        // 产品名称文件夹名称
                        productName = FileUtil.readFirstLine(fileName, imageFile);
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
                    if (slaveFiles.size() > 0) {
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
                        if (pageReq.getAttachType() == 0 && slaveFileIds.contains(master.getKey())) {
                            logger.warn("文件夹名称：{}，{}目录下{}产品上传失败，失败原因：{}", fileName, pageReq.getAttachProperty(), master.getValue(), errMsg);
                        } else {
                            logger.warn("文件夹名称：{}，{}目录下{}产品上传失败，失败原因：{}", fileName, pageReq.getProperty(), master.getValue(), errMsg);
                        }
                        failCount++;
                    } else {
                        if (pageReq.getAttachType() == 0 && slaveFileIds.contains(master.getKey())) {
                            logger.info("文件夹名称：{}，{}目录下{}产品上传成功", fileName, pageReq.getAttachProperty(), master.getValue());
                        } else {
                            logger.info("文件夹名称：{}，{}目录下{}产品上传成功", fileName, pageReq.getProperty(), master.getValue());
                        }
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
