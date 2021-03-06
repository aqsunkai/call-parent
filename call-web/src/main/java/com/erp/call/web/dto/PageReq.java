package com.erp.call.web.dto;

import lombok.Data;

@Data
public class PageReq {

    private Integer type;

    private Integer priceType;

    private Integer attachType;

    private String filePath;

    private String cookie;

    private String property;

    private String attachProperty;

    private String productNameFile;

    private Boolean customDefCheck;

    private CustomDef customDefs;

}
