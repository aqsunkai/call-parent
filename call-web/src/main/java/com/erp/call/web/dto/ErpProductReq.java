package com.erp.call.web.dto;

import com.google.common.collect.Maps;

import java.util.Map;

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

    public String getKeywordsId() {
        return keywordsId;
    }

    public void setKeywordsId(String keywordsId) {
        this.keywordsId = keywordsId;
    }

    public String getProname() {
        return proname;
    }

    public void setProname(String proname) {
        this.proname = proname;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSaleprice() {
        return saleprice;
    }

    public void setSaleprice(String saleprice) {
        this.saleprice = saleprice;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getCoin_sign() {
        return coin_sign;
    }

    public void setCoin_sign(String coin_sign) {
        this.coin_sign = coin_sign;
    }

    public String getSkucode() {
        return skucode;
    }

    public void setSkucode(String skucode) {
        this.skucode = skucode;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getIs_fanyi() {
        return is_fanyi;
    }

    public void setIs_fanyi(String is_fanyi) {
        this.is_fanyi = is_fanyi;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String[] getSketch() {
        return sketch;
    }

    public void setSketch(String[] sketch) {
        this.sketch = sketch;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getKucun() {
        return kucun;
    }

    public void setKucun(String kucun) {
        this.kucun = kucun;
    }

    public String getFromurl() {
        return fromurl;
    }

    public void setFromurl(String fromurl) {
        this.fromurl = fromurl;
    }

    public String getUpc_ean() {
        return upc_ean;
    }

    public void setUpc_ean(String upc_ean) {
        this.upc_ean = upc_ean;
    }

    public String getUpc_ean_type() {
        return upc_ean_type;
    }

    public void setUpc_ean_type(String upc_ean_type) {
        this.upc_ean_type = upc_ean_type;
    }

    public Extend getExtend() {
        return extend;
    }

    public void setExtend(Extend extend) {
        this.extend = extend;
    }

    public Map<String, String> getAttribute() {
        return attribute;
    }

    public void setAttribute(Map<String, String> attribute) {
        this.attribute = attribute;
    }

    public Map<String, Object> getMain_imgs() {
        return main_imgs;
    }

    public void setMain_imgs(Map<String, Object> main_imgs) {
        this.main_imgs = main_imgs;
    }

    public Map<String, String> getAll_imgs() {
        return all_imgs;
    }

    public void setAll_imgs(Map<String, String> all_imgs) {
        this.all_imgs = all_imgs;
    }

    public Map<String, String> getImage_info() {
        return image_info;
    }

    public void setImage_info(Map<String, String> image_info) {
        this.image_info = image_info;
    }

    public String getShow_img() {
        return show_img;
    }

    public void setShow_img(String show_img) {
        this.show_img = show_img;
    }

    public String[] getVariants() {
        return variants;
    }

    public void setVariants(String[] variants) {
        this.variants = variants;
    }
}
