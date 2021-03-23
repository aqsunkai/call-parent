package com.erp.call.web.util;

import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author sunkai
 * @description
 * @date 2021/3/23 19:44
 */
public class MapValueComparator implements Comparator<Map.Entry<String, String>> {

    @Override
    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
        if (isNumeric(o1.getValue()) && isNumeric(o2.getValue())) {
            return Integer.parseInt(o1.getValue()) - Integer.parseInt(o2.getValue());
        }
        return -1;
    }


    public static boolean isNumeric(String string) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(string).matches();
    }
}
