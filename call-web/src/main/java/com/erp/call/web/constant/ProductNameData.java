package com.erp.call.web.constant;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author sunkai
 * @description 亚马逊标题有些单词不能用
 * @date 2021/1/24 18:18
 */
public class ProductNameData {

    private static List<String> forbiddenWords = Lists.newArrayList();
    private static List<String> upperWords = Lists.newArrayList();
    private static List<String> firstLetterNotCapitalized = Lists.newArrayList();

    static {
        forbiddenWords.add("2018");
        forbiddenWords.add("2019");
        forbiddenWords.add("2020");
        forbiddenWords.add("2021");
        forbiddenWords.add("new trendy");
        forbiddenWords.add("new fashion");
        forbiddenWords.add("new style");
        forbiddenWords.add("new designer");
        forbiddenWords.add("new design");
        forbiddenWords.add("new arrival");
        forbiddenWords.add("new arrive");
        forbiddenWords.add("new original");
        forbiddenWords.add("new pattern");
        forbiddenWords.add("elegant fashion");
        forbiddenWords.add("cool fashion");
        forbiddenWords.add("high-end");
        forbiddenWords.add("high-quality");
        forbiddenWords.add("top-quality");
        forbiddenWords.add("high quality");
        forbiddenWords.add("top quality");
        forbiddenWords.add("good quality");
        forbiddenWords.add("high capacity");
        forbiddenWords.add("large-capacity");
        forbiddenWords.add("low price");
        forbiddenWords.add("top rated");
        forbiddenWords.add("brand designer");
        forbiddenWords.add("famous designer");
        forbiddenWords.add("brand new");
        forbiddenWords.add("free gift");
        forbiddenWords.add("free shipping");
        forbiddenWords.add("free-shipping");
        forbiddenWords.add("free ship");
        forbiddenWords.add("hot sales");
        forbiddenWords.add("hot sale");
        forbiddenWords.add("hot selling");
        forbiddenWords.add("clearance sales");
        forbiddenWords.add("sale");
        forbiddenWords.add("hot item");
        forbiddenWords.add("best seller");
        forbiddenWords.add("branded");
        forbiddenWords.add("giveaway");
        forbiddenWords.add("new");
        forbiddenWords.add("discount");
        forbiddenWords.add("promotion");
        forbiddenWords.add("baguette");

        firstLetterNotCapitalized.add("and");
        firstLetterNotCapitalized.add("or");
        firstLetterNotCapitalized.add("for");
        firstLetterNotCapitalized.add("the");
        firstLetterNotCapitalized.add("a");
        firstLetterNotCapitalized.add("an");
        firstLetterNotCapitalized.add("in");
        firstLetterNotCapitalized.add("on");
        firstLetterNotCapitalized.add("over");
        firstLetterNotCapitalized.add("with");

        upperWords.add("pu");
    }

    public static String changeProductName(String productName) {
        String newName = productName.toLowerCase();
        for (String word : forbiddenWords) {
            newName = newName.replace(word, "");
        }
        newName = newName.trim().replaceAll(" {2,}", " ");
        if (StringUtils.isBlank(newName)) {
            return "";
        }
        String[] newWords = newName.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String newWord : newWords) {
            if (firstLetterNotCapitalized.contains(newWord)) {
                sb.append(newWord).append(" ");
            } else if (upperWords.contains(newWord)) {
                sb.append(newWord.toUpperCase()).append(" ");
            } else {
                sb.append(newWord.substring(0, 1).toUpperCase()).append(newWord.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

//    public static void main(String[] args) {
//        String name = "v  f  dsa  f";
//        System.out.println(changeProductName(name));
//    }

}
