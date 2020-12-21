package com.erp.call.web.controller;

import com.erp.call.web.dto.PageReq;
import com.erp.call.web.dto.PageRes;
import com.erp.call.web.service.CallService;
import com.erp.call.web.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class CallController {

    @Autowired
    private CallService callService;

    @PostMapping(value = "/product/send")
    public Result<String> sendProduct(@RequestBody PageReq pageReq) {
        if (StringUtils.isEmpty(pageReq.getCookie())) {
            return Result.error("填写cookie");
        }
        if (StringUtils.isEmpty(pageReq.getFilePath())) {
            return Result.error("填写filePath");
        }
        if (StringUtils.isEmpty(pageReq.getProperty())) {
            return Result.error("填写列表文件夹名称");
        }
        if (StringUtils.isEmpty(pageReq.getAttachProperty())) {
            return Result.error("填写详情文件夹名称");
        }
        if (pageReq.getType() == 1 && StringUtils.isEmpty(pageReq.getProductNameFile())) {
            return Result.error("填写txt文件名");
        }
        pageReq.setCookie(pageReq.getCookie().trim());
        callService.sendProduct(pageReq);
        return Result.success(null);
    }

    @PostMapping(value = "/erpProduct/send")
    public Result<String> sendErpProduct(@RequestBody PageReq pageReq) {
        if (StringUtils.isEmpty(pageReq.getCookie())) {
            return Result.error("填写cookie");
        }
        if (StringUtils.isEmpty(pageReq.getFilePath())) {
            return Result.error("填写filePath");
        }
        if (StringUtils.isEmpty(pageReq.getProperty())) {
            return Result.error("填写列表文件夹名称");
        }
        if (StringUtils.isEmpty(pageReq.getAttachProperty())) {
            return Result.error("填写详情文件夹名称");
        }
        if (pageReq.getType() == 1 && StringUtils.isEmpty(pageReq.getProductNameFile())) {
            return Result.error("填写txt文件名");
        }
        pageReq.setCookie(pageReq.getCookie().trim());
        callService.sendErpProduct(pageReq);
        return Result.success(null);
    }

    @PostMapping(value = "/product/result")
    public Result<PageRes> getProductResult(@RequestBody PageReq pageReq) {
        return Result.success(callService.getProductResult(pageReq.getFilePath()));
    }
}
