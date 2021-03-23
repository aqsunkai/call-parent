package com.erp.call.web.dto;

import lombok.Data;

@Data
public class Image {

    private String purpose;

    private Integer seq;

    private String sourceUrl;

    private String storageKey;

    private Boolean check;

}
