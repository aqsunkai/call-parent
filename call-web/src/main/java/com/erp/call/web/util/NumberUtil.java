package com.erp.call.web.util;

import java.math.BigDecimal;

/**
 * @author sunkai
 * @description
 * @date 2021/1/20 14:34
 */
public class NumberUtil {

    public static double formatDoubleScale3(double d) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double formatDoubleScale2(double d) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String changeInt(int num) {
        if (num < 10) {
            return "0" + num;
        }
        return num + "";
    }
}
