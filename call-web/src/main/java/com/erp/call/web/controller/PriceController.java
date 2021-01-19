package com.erp.call.web.controller;

import com.erp.call.web.dto.PriceReq;
import com.erp.call.web.service.PriceService;
import com.erp.call.web.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

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
    public Result<Set<String>> calculate(@RequestBody PriceReq priceReq) {
        return Result.success(priceService.calculate(priceReq));
    }

}
