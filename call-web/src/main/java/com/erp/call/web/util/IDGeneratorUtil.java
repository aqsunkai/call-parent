/**
 * <p>Title: IDGeneratorUtil.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: www.zto.com</p>
 */
package com.erp.call.web.util;

import com.erp.call.web.util.id.EasyGenerator;

import java.net.InetAddress;

public class IDGeneratorUtil {
    private static EasyGenerator easyGenerator = null;

    static {
        initWorkerId();
    }

    static void initWorkerId() {
        InetAddress address;
        try {
            address = NetUtils.getLocalAddress();
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot get LocalHost InetAddress, please check your network!");
        }
        byte[] ipAddressByteArray = address.getAddress();
        easyGenerator = new EasyGenerator((((ipAddressByteArray[ipAddressByteArray.length - 2] & 0B11) << Byte.SIZE) + (ipAddressByteArray[ipAddressByteArray.length - 1] & 0xFF)),
                600);
    }

    /***
     * 请注意 此方法通过IP转成二进制
     * @return
     */
    public static Number generateId() {
        return easyGenerator.newId();
    }

    public static long generateLongId() {
        return generateId().longValue();
    }

    public static int generateIntId() {
        return generateId().intValue();
    }

    public static String generateStringId() {
        return generateId().toString();
    }

}
