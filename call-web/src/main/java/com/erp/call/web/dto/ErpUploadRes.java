package com.erp.call.web.dto;

public class ErpUploadRes {

    private Boolean state;

    private String msg;

    private ErpInfo data;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ErpInfo getData() {
        return data;
    }

    public void setData(ErpInfo data) {
        this.data = data;
    }
}
