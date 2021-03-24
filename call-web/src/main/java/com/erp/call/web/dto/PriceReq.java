package com.erp.call.web.dto;

import lombok.Data;

@Data
public class PriceReq {

    private String filePath;

    private String property;

    private Integer sourcePlatform;

    private Integer valueType;

    private Integer intervalType;

    private String calPattern;

    private String frontPrice;
}
