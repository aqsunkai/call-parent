package com.erp.call.web.translate;

import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.text.UnicodeUtil;

import java.util.ArrayList;
import java.util.List;

public class Main {

    // 在平台申请的APP_ID 详见 http://api.fanyi.baidu.com/api/trans/product/desktop?req=developer
    private static final String APP_ID = "20210205000691277";
    private static final String SECURITY_KEY = "pWLP_IyasKjFgAgH_evT";

    public static void main(String[] args) {
//        unicodeToString();
        TransApi api = new TransApi(APP_ID, SECURITY_KEY);
//
//        String query = "Gzcz真皮女斜挎包流苏手提包手提包小斜挎包女士钱包Bolsa新";
        String query = "Gzcz Genuine Leather Female Crossbody Bag Tassel Handbags Tote Small Messenger Bags for Women Purses Bolsa New";
//        String query = "Gzcz leather women's messenger bag tassel handbag handbag small messenger bag women's purse NEW";
        System.out.println(api.getTransResult(query, "en", "zh"));
    }

    public static void unicodeToString() {

        String str = "Gzcz\\u771f\\u76ae\\u5973\\u7528\\u659c\\u630e\\u5305\\u6d41\\u82cf\\u624b\\u63d0\\u5305\\u624b\\u63d0\\u5305\\u5973\\u58eb\\u5c0f\\u4fe1\\u4f7f\\u5305\\u5305Bolsa\\u65b0\\u6b3e";
        String strNew = UnicodeUtil.toString(str);
        System.out.println(strNew);

        List<String> en = new ArrayList<>();
        StrBuilder builder = StrBuilder.create();
        String[] strs = strNew.split("");
        for (String ss : strs) {
            if (isEnglish(ss)) {
                builder.append(ss);
            } else if (builder.length() > 0) {
                en.add(builder.toString());
                builder.reset();
            }
        }
        System.out.println(en);
    }

    public static boolean isEnglish(String str) {
        return str.matches("^[a-zA-Z]*");
    }

}
