package com.erp.call.web.dto;

import lombok.Data;

@Data
public class ErpUploadRes {

    private Boolean state;

    private String msg;

    private ErpInfo data;

}
