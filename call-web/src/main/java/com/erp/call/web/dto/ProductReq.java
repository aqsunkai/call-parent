package com.erp.call.web.dto;

import java.util.ArrayList;
import java.util.List;

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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Integer getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(Integer catalogId) {
        this.catalogId = catalogId;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Double getDimensionsHeight() {
        return dimensionsHeight;
    }

    public void setDimensionsHeight(Double dimensionsHeight) {
        this.dimensionsHeight = dimensionsHeight;
    }

    public Double getDimensionsLength() {
        return dimensionsLength;
    }

    public void setDimensionsLength(Double dimensionsLength) {
        this.dimensionsLength = dimensionsLength;
    }

    public String getDimensionsUnit() {
        return dimensionsUnit;
    }

    public void setDimensionsUnit(String dimensionsUnit) {
        this.dimensionsUnit = dimensionsUnit;
    }

    public Double getDimensionsWidth() {
        return dimensionsWidth;
    }

    public void setDimensionsWidth(Double dimensionsWidth) {
        this.dimensionsWidth = dimensionsWidth;
    }

    public List<String> getExtendData() {
        return extendData;
    }

    public void setExtendData(List<String> extendData) {
        this.extendData = extendData;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }

    public ProductDeclare getProductDeclare() {
        return productDeclare;
    }

    public void setProductDeclare(ProductDeclare productDeclare) {
        this.productDeclare = productDeclare;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public Double getShippingWeight() {
        return shippingWeight;
    }

    public void setShippingWeight(Double shippingWeight) {
        this.shippingWeight = shippingWeight;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getVariationDataCustomDefs() {
        return variationDataCustomDefs;
    }

    public void setVariationDataCustomDefs(List<String> variationDataCustomDefs) {
        this.variationDataCustomDefs = variationDataCustomDefs;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }
}
