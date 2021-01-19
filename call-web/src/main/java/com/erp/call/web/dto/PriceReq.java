package com.erp.call.web.dto;

import lombok.Data;

@Data
public class PriceReq {

    private String filePath;

    private String property;

    private String calPattern;
}
