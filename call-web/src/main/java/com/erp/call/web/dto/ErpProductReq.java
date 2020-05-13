package com.erp.call.web.dto;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

@Data
public class ErpProductReq {

    private String keywordsId;
    private String proname;
    private String repeat;
    private String brand;
    private String price;
    private String saleprice;
    private String cost;
    private String coin;
    private String coin_sign;
    private String skucode;
    private String category_id;
    private String is_fanyi;
    private String category;
    private String[] keywords;
    private String[] sketch;
    private String des;
    private String kucun;
    private String fromurl;
    private String upc_ean;
    private String upc_ean_type;
    private Extend extend;
    private Map<String, String> attribute = Maps.newHashMap();
    private Map<String, Object> main_imgs;
    private Map<String, String> all_imgs;
    private Map<String, String> image_info;
    private String show_img;
    private String[] variants = new String[0];

}
