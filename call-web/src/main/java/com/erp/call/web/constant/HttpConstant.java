package com.erp.call.web.constant;

import com.google.common.collect.Maps;

import java.util.Map;

public class HttpConstant {

    public static final String UPLOAD_URL = "http://erp.fjlonfenner.com/image/api/image/upload";
    public static final String PRODUCT_URL = "http://erp.fjlonfenner.com/gm-product/api/v4/products";
    public static final String ERP_UPLOAD_URL = "http://erp2.miwaimao.com/admin/upload/uploadlocalurl.html";
    public static final String ERP_PRODUCT_URL = "http://erp2.miwaimao.com/admin/product/add.html";


    /**
     * 表头数据
     */
    public static Map<String, String> getRequestHeader(String cookie) {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Authorization", cookie);
//        headers.put("Cookie", "crx_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX21vYmlsZV9waG9uZSI6IjE1NjAxNzc1NTM1IiwidXNlcl9pZCI6MzY2ODQ1LCJ1c2VyX25hbWUiOiJ3bGN1aWJpbmdiaW5nIiwic2NvcGUiOlsib3BlbmlkIl0sInVzZXJfbW9iaWxlX3Bob25lX3ZlcmlmaWVkIjpmYWxzZSwidXNlcl9uaWNrX25hbWUiOiLltJTlhrDlhrAiLCJleHAiOjE2MDg0OTcyOTksImlhdCI6MTYwODQ1NDA5OSwiYXV0aG9yaXRpZXMiOlsiUk9MRV9PRkZJQ0VfT1dORVJfMTY0MzE0IiwiUk9MRV9PRkZJQ0VfT1dORVJfQ09NUEFOWSIsIlJPTEVfSVNLX0FETUlOIiwiUk9MRV9JU0tfQURNSU5fQkFTRSJdLCJqdGkiOiI1YTcxOGE1Yy01MDAwLTQwOGYtOGYyYy1jZmE1ZjcyNWEyYjAiLCJjbGllbnRfaWQiOiJ3ZWJfYXBwIn0.IO1ZwiCCOxfphTnaliXuU4V-gKLMRcCXnrpn0wkVGwXbgqRMpjKPm1ja_kdvY5T-H-St9w3qOZMdM4FoVH-hcQ58W-q5y15F5HDnDs_f2VugA9P0I0kZUcYhY_i14_q76zD0-deswNfJQ5zve6W5-dCMQidZSuCFyGL0wlicDxzLN77HnxcA5YqXoWxXLfQVIdKeJNjHcW-eJs_X1gnxlCQTwFFpLnOFISmah_qfma06jaKk64DGWasbv30Nw56bCcEwUQ6i2hTuYQ8AEJVdHFIsYn52hwkV_kDg_V0yaMd4BP9_63XaACNJzr_AS-_Zy9uVlK-jAqqCKR9wgd7RfQ");
        headers.put("Host", "erp.fjlonfenner.com");
        headers.put("Origin", "http://erp.fjlonfenner.com");
        headers.put("Referer", "http://erp.fjlonfenner.com/isk/");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.135 Safari/537.36");
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
