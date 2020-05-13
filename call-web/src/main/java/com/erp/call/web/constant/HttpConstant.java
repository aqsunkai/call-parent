package com.erp.call.web.constant;

import com.google.common.collect.Maps;

import java.util.Map;

public class HttpConstant {

    public static final String UPLOAD_URL = "http://erp.fjlonfenner.com/image/api/image/upload";
    public static final String PRODUCT_URL = "http://erp.fjlonfenner.com/gm-product/api/salesman/products";
    public static final String ERP_UPLOAD_URL = "http://erp2.miwaimao.com/admin/upload/uploadlocalurl.html";
    public static final String ERP_PRODUCT_URL = "http://erp2.miwaimao.com/admin/product/add.html";


    /**
     * 表头数据
     */
    public static Map<String, String> getRequestHeader(String cookie) {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Authorization", cookie);
        headers.put("Host", "erp.fjlonfenner.com");
        headers.put("Origin", "http://erp.fjlonfenner.com");
        headers.put("Referer", "http://erp.fjlonfenner.com/gmp/");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.92 Safari/537.36");
        return headers;
    }

    /**
     * 小米erp表头数据
     *
     * @param flag false为上传产品，true为上传图片
     */
    public static Map<String, String> getErpRequestHeader(String cookie, boolean flag) {
        Map<String, String> headers = Maps.newHashMap();
        if (!flag) {
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        }
        headers.put("Cookie", cookie);
        headers.put("Host", "erp2.miwaimao.com");
        headers.put("Origin", "http://erp2.miwaimao.com");
        headers.put("Referer", "http://erp2.miwaimao.com/admin/product/add.html");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
        return headers;
    }

}
