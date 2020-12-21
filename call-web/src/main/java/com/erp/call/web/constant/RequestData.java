package com.erp.call.web.constant;

import com.alibaba.fastjson.JSON;
import com.erp.call.web.dto.*;
import com.erp.call.web.util.IDGeneratorUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;

public class RequestData {

    public static ProductReq getProductReq(CustomDef customDefs, String name, Double price, String md5, List<String> slaveMd5) {
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
        // 商品变体
        if (null != customDefs && StringUtils.isNotEmpty(customDefs.getCode())) {
            customDefs.setValueOptions(customDefs.getValueOptions().replace("，", ","));
            CustomDef customDef = new CustomDef();
            BeanUtils.copyProperties(customDefs, customDef);
            customDef.setSeq(1);
            req.setVariationDataCustomDefs(Lists.newArrayList(customDef));

            String[] valueOptions = customDef.getValueOptions().split(",");

            List<CustomChildren> customChildrens = Lists.newArrayList();
            int seq = 1;
            for (String valueOption : valueOptions) {
                CustomChildren.VariationData.Item item = new CustomChildren.VariationData.Item();
                item.setCode(customDef.getCode());
                item.setValue(valueOption);
                CustomChildren.VariationData variationData = new CustomChildren.VariationData();
                variationData.setItems(Lists.newArrayList(item));
                CustomChildren customChildren = new CustomChildren();
                customChildren.setVariationData(variationData);
                customChildren.setSeq(seq);
                customChildrens.add(customChildren);
                seq++;
            }
            req.setChildren(customChildrens);
        }
        return req;
    }

    public static ErpProductReq getErpProductReq(String product, String price, String masterId, String masterUrl, String masterSize,
                                                 Map<String, String> slaveUrl, Map<String, String> slaveSize, IDGeneratorUtil idGeneratorUtil) {
        ErpProductReq req = new ErpProductReq();
        req.setProname(product);
        req.setSkucode(idGeneratorUtil.snowflakeId());
        req.setRepeat("2");
        req.setBrand("");
        req.setPrice(price);
        req.setSaleprice(price);
        req.setCost("0");
        req.setCoin("");
        req.setCoin_sign("CNY");
        req.setIs_fanyi("1");
        req.setKeywordsId("");
        req.setKeywords(Lists.newArrayList("").toArray(new String[0]));
        req.setSketch(Lists.newArrayList("").toArray(new String[0]));
        req.setDes("");
        req.setKucun("50");
        req.setFromurl("");
        req.setUpc_ean("");
        req.setUpc_ean_type("null");
        req.setCategory_id("");
        req.setCategory("");
        Extend extend = new Extend();
        extend.setBattery("2");
        extend.setLength("0");
        extend.setWeight("0");
        extend.setWidth("0");
        extend.setHeight("0");
        extend.setColor("");
        extend.setSize("");
        extend.setOrigin("");
        extend.setBrand("");
        extend.setFacturer("");
        extend.setNumber("");
        extend.setMaterial("");
        extend.setGem("");
        extend.setMetal("");
//        extend.setPackage("");
        extend.setProduced("");
        extend.setValidity("");
        req.setExtend(extend);
        Map<String, Object> mainImgs = Maps.newHashMap();
        mainImgs.put("main", masterId);
        mainImgs.put("sample", "");
        List<String> slaveIds = Lists.newArrayListWithCapacity(slaveSize.size());
        slaveIds.addAll(slaveSize.keySet());
        mainImgs.put("affiliate", slaveIds.toArray(new String[0]));
        req.setMain_imgs(mainImgs);
        Map<String, String> allImgs = Maps.newHashMap();
        allImgs.put(masterId, masterUrl);
        allImgs.putAll(slaveUrl);
        req.setAll_imgs(allImgs);
        req.setShow_img(masterUrl);
        Map<String, String> imageInfo = Maps.newHashMap();
        imageInfo.put(masterId, masterSize);
        imageInfo.putAll(slaveSize);
        req.setImage_info(imageInfo);
        return req;
    }
}
