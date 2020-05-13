package com.erp.call.web.dto;

import lombok.Data;

@Data
public class PageReq {

    private Integer type;

    private String filePath;

    private String cookie;

    private String property;

    private String attachProperty;

    private String productNameFile;

}
