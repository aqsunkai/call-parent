package com.erp.call.web.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductReq {

    private String brandName;

    private Integer catalogId;

    private List<String> children = new ArrayList<>();

    private Double costPrice;

    private String currency;

    private Description description = new Description();

    private Double dimensionsHeight;

    private Double dimensionsLength;

    private String dimensionsUnit;

    private Double dimensionsWidth;

    private List<String> extendData = new ArrayList<>();

    private List<Image> images;

    private String manufacturer;

    private String name;

    private Double netWeight;

    private ProductDeclare productDeclare;

    private Double purchasePrice;

    private Double salePrice;

    private Double shippingWeight;

    private String sourceUrl;

    private String status;

    private String title;

    private List<String> variationDataCustomDefs = new ArrayList<>();

    private String weightUnit;

}
