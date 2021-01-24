package com.erp.call.web.controller;

import com.erp.call.web.dto.PageRes;
import com.erp.call.web.dto.PriceReq;
import com.erp.call.web.service.PriceService;
import com.erp.call.web.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

}
