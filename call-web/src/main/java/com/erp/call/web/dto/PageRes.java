package com.erp.call.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageRes {

    private Boolean running;
    private List<String> failName;
    private List<String> uploadName;
    private String runningResult;

}
