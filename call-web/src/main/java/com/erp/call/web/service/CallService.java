package com.erp.call.web.service;

import com.erp.call.web.dto.*;
import com.erp.call.web.util.HttpClientHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.erp.call.web.constant.HttpConstant.PRODUCT_URL;
import static com.erp.call.web.constant.HttpConstant.UPLOAD_URL;

@Service
public class CallService {

    private Logger logger = LoggerFactory.getLogger(CallService.class);

    private volatile Map<String, Boolean> runningMap = new ConcurrentHashMap<>();

    private Map<String, List<String>> FAIL_NAME = new ConcurrentHashMap<>();
    private Map<String, List<String>> UPLOAD_NAME = new ConcurrentHashMap<>();

    @Autowired
    private HttpClientHelper httpClientHelper;

    public UploadRes sendImage(PageReq pageReq) {
        File file = new File(pageReq.getFilePath());
        if (!file.exists()) {
            return null;
        }
        return httpClientHelper.postFile(UPLOAD_URL, file, getRequestHeader(pageReq.getCookie()), UploadRes.class);
    }

    public void sendProduct(PageReq pageReq) {
        File file = new File(pageReq.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("文件夹地址不正确！");
        }
        if (Boolean.TRUE.equals(runningMap.get(pageReq.getFilePath()))) {
            throw new RuntimeException("老哥别重复创建哈，先等等！");
        }
        runningMap.put(pageReq.getFilePath(), true);
        FAIL_NAME.remove(pageReq.getFilePath());
        UPLOAD_NAME.remove(pageReq.getFilePath());
        ExecutorService executors = Executors.newSingleThreadExecutor();
        executors.execute(() -> createProduct(pageReq, file));
        executors.shutdown();
    }

    private void createProduct(PageReq pageReq, File file) {
        try {
            File[] files = file.listFiles();
            for (File productFile : files) {
                String fileName = productFile.getName();
                String productName = null;
                Double price = null;
                File[] imageFiles = productFile.listFiles();
                Map<String, String> masterMd5 = new HashMap<>();
                Map<String, String> slaveMd5 = new HashMap<>();
                String firstSlaveMd5 = null;
                boolean upload = true;
                pro:
                for (File imageFile : imageFiles) {
                    if (pageReq.getProperty().equals(imageFile.getName())) {
                        File[] masterFiles = imageFile.listFiles();
                        if (null == masterFiles || masterFiles.length == 0) {
                            continue;
                        }
                        for (File masterFile : masterFiles) {
                            // 上传属性图图片
                            UploadRes res = httpClientHelper.postFile(UPLOAD_URL, masterFile, getRequestHeader(pageReq.getCookie()), UploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getRet())) {
                                masterMd5.put(res.getInfo().getMd5(), masterFile.getName().substring(0, masterFile.getName().lastIndexOf(".")));
                            } else {
                                upload = false;
                                break pro;
                            }
                            TimeUnit.MILLISECONDS.sleep(200);
                        }
                    } else if (pageReq.getAttachProperty().equals(imageFile.getName())) {
                        File[] slaveFiles = imageFile.listFiles();
                        if (null == slaveFiles || slaveFiles.length == 0) {
                            continue;
                        }
                        for (File slaveFile : slaveFiles) {
                            // 上传主图图片
                            UploadRes res = httpClientHelper.postFile(UPLOAD_URL, slaveFile, getRequestHeader(pageReq.getCookie()), UploadRes.class);
                            if (null != res && Boolean.TRUE.equals(res.getRet())) {
                                slaveMd5.put(res.getInfo().getMd5(), slaveFile.getName().substring(0, slaveFile.getName().lastIndexOf(".")));
                                if (StringUtils.isEmpty(firstSlaveMd5)) {
                                    firstSlaveMd5 = res.getInfo().getMd5();
                                }
                            } else {
                                upload = false;
                                break pro;
                            }
                            TimeUnit.MILLISECONDS.sleep(200);
                        }
                    } else if (pageReq.getType() == 1 && pageReq.getProductNameFile().equals(imageFile.getName())) {
                        // 产品名称文件夹名称
                        String txt = txt2String(fileName, imageFile);
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
                    }
                }
                if (upload) {
                    if (pageReq.getType() == 1 && StringUtils.isEmpty(productName)) {
                        logger.error("文件夹名称：{}，产品名称不存在，请自行检查", fileName);
                        List<String> uploads = FAIL_NAME.get(pageReq.getFilePath());
                        if (CollectionUtils.isEmpty(uploads)) {
                            uploads = Lists.newArrayList();
                        }
                        uploads.add(fileName);
                        FAIL_NAME.put(pageReq.getFilePath(), uploads);
                        continue;
                    }
                    int failCount = 0;
                    if (masterMd5.isEmpty() && !slaveMd5.isEmpty()) {
                        masterMd5.put(firstSlaveMd5, slaveMd5.get(firstSlaveMd5));
                        slaveMd5.remove(firstSlaveMd5);
                    }
                    for (Map.Entry<String, String> md5 : masterMd5.entrySet()) {
                        String product = pageReq.getType() == 1 ? productName : md5.getValue();
                        ProductRes res = httpClientHelper.put(PRODUCT_URL, getProductReq(product, price, md5.getKey(),
                                new ArrayList<>(slaveMd5.keySet())), getRequestHeader(pageReq.getCookie()), ProductRes.class);
                        if (null == res || Boolean.TRUE.equals(res.getCheckFail())) {
                            failCount++;
                        }
                        TimeUnit.MILLISECONDS.sleep(200);
                    }
                    if (failCount == 0) {
                        logger.info("文件夹名称：{}，创建产品成功", fileName);
//                        productFile.delete();
                        List<String> uploads = UPLOAD_NAME.get(pageReq.getFilePath());
                        if (CollectionUtils.isEmpty(uploads)) {
                            uploads = Lists.newArrayList();
                        }
                        uploads.add(fileName);
                        UPLOAD_NAME.put(pageReq.getFilePath(), uploads);
                    } else {
                        logger.info("文件夹名称：{}，创建产品失败，请自行检查", fileName);
                        List<String> uploads = FAIL_NAME.get(pageReq.getFilePath());
                        if (CollectionUtils.isEmpty(uploads)) {
                            uploads = Lists.newArrayList();
                        }
                        uploads.add(fileName);
                        FAIL_NAME.put(pageReq.getFilePath(), uploads);
                    }
                } else {
                    logger.info("文件夹名称：{}，上传图片失败，请自行检查", fileName);
                    List<String> uploads = FAIL_NAME.get(pageReq.getFilePath());
                    if (CollectionUtils.isEmpty(uploads)) {
                        uploads = Lists.newArrayList();
                    }
                    uploads.add(fileName);
                    FAIL_NAME.put(pageReq.getFilePath(), uploads);
                }
            }
            runningMap.put(pageReq.getFilePath(), false);
        } catch (Exception e) {
            logger.error("异步创建产品失败", e);
            runningMap.put(pageReq.getFilePath(), false);
        }
    }

    private String txt2String(String fileName, File file) {
        StringBuilder result = new StringBuilder();
        BufferedReader br = null;
        try {
            //构造一个BufferedReader类来读取文件
            br = new BufferedReader(new FileReader(file));
            String s;
            //使用readLine方法，一次读一行
            while ((s = br.readLine()) != null) {
                result.append(s);
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

    private ProductReq getProductReq(String name, Double price, String md5, List<String> slaveMd5) {
        ProductReq req = new ProductReq();
        req.setCurrency("CNY");
        req.setDimensionsUnit("CM");
        req.setName(name);
        req.setWeightUnit("G");
        req.setSalePrice(price);

        ProductDeclare declare = new ProductDeclare();
        declare.setCurrency("USD");
        req.setProductDeclare(declare);

        List<Image> images = Lists.newArrayList();
        Image image1 = new Image();
        image1.setSeq(1);
        image1.setSourceUrl("//cdn-images.x-oss.com/" + md5 + "/jpg");
        image1.setStorageKey(md5);
        images.add(image1);
        for (int i = 2; i < slaveMd5.size() + 2; i++) {
            Image image = new Image();
            image.setSeq(i);
            image.setSourceUrl("//cdn-images.x-oss.com/" + slaveMd5.get(i - 2) + "/jpg");
            image.setStorageKey(slaveMd5.get(i - 2));
            images.add(image);
        }
        req.setImages(images);
        return req;
    }

    private Map<String, String> getRequestHeader(String cookie) {
        Map<String, String> headers = Maps.newHashMap();
//        headers.put("Cookie", "_ati=4439240839304; access_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX21vYmlsZV9waG9uZSI6IjE3NTIxMDYyOTQyIiwidXNlcl9pZCI6MjQyNzU4LCJ1c2VyX25hbWUiOiJuaXVrYW5namllIiwic2NvcGUiOlsib3BlbmlkIl0sInVzZXJfbW9iaWxlX3Bob25lX3ZlcmlmaWVkIjpmYWxzZSwidXNlcl9uaWNrX25hbWUiOiLlsI_nu4Qt54mb5bq35p2wIiwiZXhwIjoxNTg2OTkzODAyLCJpYXQiOjE1ODY5NTA2MDIsImF1dGhvcml0aWVzIjpbIlJPTEVfR01QX0VNUExPWUVFIiwiUk9MRV9HTVBfU0FMRVNNQU5fSU5ORVIiXSwianRpIjoiMWQzMDZlNDQtNTBhNS00ZmJhLWI0ZTAtNWE1YWY4ZGQyZDhiIiwiY2xpZW50X2lkIjoid2ViX2FwcCJ9.InY1FjN4YfFPW5UmJNp29AH6EFwB6bTwVuj0hwW6V-QW8CHoDZRb9JceF4dEqB52i2YWpSOOjlsHWUsJ_xWixP6PP8dVSrUwHCUpkN5rDxpstdII5zSM8TcqQPCaAQLEwOBHlrkOL8Nxt_Gw42jV55Th_y9IsjMwm3ZcKxBlLZvFzm2vCIfEkvY8_bCg47EzaHUoBfkcriFgGd1ma-Gc3_BpSl9u-www3eFi47jJx3yQFAOklw4UNGog05WZsJ9HUDOZgUtjfNLweAoNoSRijYxrlWiSibaMyzADAtfR1EEiAdbp3x8h2-3dAUuWCanHydiKqGaBkQUQx_fGCsQ6dw; refresh_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX21vYmlsZV9waG9uZSI6IjE3NTIxMDYyOTQyIiwidXNlcl9pZCI6MjQyNzU4LCJ1c2VyX25hbWUiOiJuaXVrYW5namllIiwic2NvcGUiOlsib3BlbmlkIl0sInVzZXJfbW9iaWxlX3Bob25lX3ZlcmlmaWVkIjpmYWxzZSwiYXRpIjoiMWQzMDZlNDQtNTBhNS00ZmJhLWI0ZTAtNWE1YWY4ZGQyZDhiIiwidXNlcl9uaWNrX25hbWUiOiLlsI_nu4Qt54mb5bq35p2wIiwiZXhwIjoxNTg5NTQyNjAyLCJpYXQiOjE1ODY5NTA2MDIsImF1dGhvcml0aWVzIjpbIlJPTEVfR01QX0VNUExPWUVFIiwiUk9MRV9HTVBfU0FMRVNNQU5fSU5ORVIiXSwianRpIjoiYzJlOGE5YzItY2VjMy00ODIzLTgxYTctYjhkZmI3ZGQyZTliIiwiY2xpZW50X2lkIjoid2ViX2FwcCJ9.Xo0BtWHe6UizUDeDkcye-vRzcYZa5LUZ6fhgyVFYMDDvy285rOR8zpOmU-FcbmHhPlBBr3owEL7daMfuXROGnIZTBe90-NeyEJoFHXw2rd-3_GEopoyLgt0pFg_ikelwpvugKgW4I1JVxOVi29uuq5vr98bjYlqKO3i_lTLk7U_PRhSeE7v0FPFLwGnxiaGBSbp47uUSILyqteUPiWv-61bSy9re_auz8Iwy3t068Y4wBlrOclguhq9vytgHXuMiDqCUwDJ5wWxm5gHEfViubfHZSyJND6XCn46c5Hel8dwMrG34ykpzEZbW6vaDj_eF7dOUtBo2Wc_WgA-wzI4dBA");
//        headers.put("Authorization","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX21vYmlsZV9waG9uZSI6IjE3NTIxMDYyOTQyIiwidXNlcl9pZCI6MjQyNzU4LCJ1c2VyX25hbWUiOiJuaXVrYW5namllIiwic2NvcGUiOlsib3BlbmlkIl0sInVzZXJfbW9iaWxlX3Bob25lX3ZlcmlmaWVkIjpmYWxzZSwidXNlcl9uaWNrX25hbWUiOiLlsI_nu4Qt54mb5bq35p2wIiwiZXhwIjoxNTg2OTkzODAyLCJpYXQiOjE1ODY5NTA2MDIsImF1dGhvcml0aWVzIjpbIlJPTEVfR01QX0VNUExPWUVFIiwiUk9MRV9HTVBfU0FMRVNNQU5fSU5ORVIiXSwianRpIjoiMWQzMDZlNDQtNTBhNS00ZmJhLWI0ZTAtNWE1YWY4ZGQyZDhiIiwiY2xpZW50X2lkIjoid2ViX2FwcCJ9.InY1FjN4YfFPW5UmJNp29AH6EFwB6bTwVuj0hwW6V-QW8CHoDZRb9JceF4dEqB52i2YWpSOOjlsHWUsJ_xWixP6PP8dVSrUwHCUpkN5rDxpstdII5zSM8TcqQPCaAQLEwOBHlrkOL8Nxt_Gw42jV55Th_y9IsjMwm3ZcKxBlLZvFzm2vCIfEkvY8_bCg47EzaHUoBfkcriFgGd1ma-Gc3_BpSl9u-www3eFi47jJx3yQFAOklw4UNGog05WZsJ9HUDOZgUtjfNLweAoNoSRijYxrlWiSibaMyzADAtfR1EEiAdbp3x8h2-3dAUuWCanHydiKqGaBkQUQx_fGCsQ6dw");
        headers.put("Cookie", cookie);
        headers.put("Host", "erp.fjlonfenner.com");
        headers.put("Origin", "http://erp.fjlonfenner.com");
        headers.put("Referer", "http://erp.fjlonfenner.com/gmp/");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.92 Safari/537.36");
        return headers;
    }

    public PageRes getProductResult(String filePath) {
        PageRes res = new PageRes();
        res.setRunning(runningMap.get(filePath));
        res.setFailName(FAIL_NAME.get(filePath));
        res.setUploadName(UPLOAD_NAME.get(filePath));
        return res;
    }
}
