package com.erp.call.web.controller;

import com.erp.call.web.dto.PageRes;
import com.erp.call.web.dto.PriceReq;
import com.erp.call.web.service.PriceService;
import com.erp.call.web.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sunkai
 * @description 计算价格
 * @date 2021/1/19 18:31
 */
@RestController
@RequestMapping("/api/price")
public class PriceController {

    @Autowired
    private PriceService priceService;

    @PostMapping(value = "/calculate")
    public Result<String> calculate(@RequestBody PriceReq priceReq) {
        priceService.calculatePrice(priceReq);
        return Result.success(null);
    }

    @GetMapping(value = "/calculate/result")
    public Result<PageRes> getCalculateResult(@RequestParam("filePath") String filePath) {
        return Result.success(priceService.getCalculateResult(filePath.trim()));
    }

    @PostMapping(value = "/change")
    public Result<String> change(@RequestBody PriceReq priceReq) {
        return Result.success(priceService.changePrice(priceReq));
    }

}
