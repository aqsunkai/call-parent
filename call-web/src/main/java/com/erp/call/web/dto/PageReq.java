package com.erp.call.web.dto;

public class PageReq {

    private Integer type;

    private String filePath;

    private String cookie;

    private String property;

    private String attachProperty;

    private String productNameFile;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getAttachProperty() {
        return attachProperty;
    }

    public void setAttachProperty(String attachProperty) {
        this.attachProperty = attachProperty;
    }

    public String getProductNameFile() {
        return productNameFile;
    }

    public void setProductNameFile(String productNameFile) {
        this.productNameFile = productNameFile;
    }
}
