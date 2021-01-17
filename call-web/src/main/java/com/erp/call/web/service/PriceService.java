package com.erp.call.web.service;

import java.io.File;

/**
 * @author sunkai
 * @description
 * @date 2021/1/17 10:54
 */
public class PriceService {

    public static void main(String[] args) {
        File file = new File("D:\\call\\images\\20210113_223418共下图52个地址");
        File[] files = file.listFiles();
        assert files != null;
        for (File productFile1 : files) {
            File[] productFiles = productFile1.listFiles();
            assert productFiles != null;
            for (File productFile : productFiles) {
                if ("属性图".equals(productFile.getName())) {
                    File[] imageFiles = productFile.listFiles();
                    if (null != imageFiles) {
                        for (File imageFile : imageFiles) {
                            String imageName = imageFile.getName();
                            String imagePath = imageFile.getAbsolutePath();
                            String newName = changeName(imageName, imagePath);
                            File newFile = new File(imagePath.substring(0, imagePath.indexOf(imageName)) + newName);
                            imageFile.renameTo(newFile);
                        }
                    }
                }
            }
        }
    }

    private static String changeName(String fileName, String imagePath) {
        try {
            String[] fileNames = fileName.replace("（", "(").split("\\(");
            if (fileNames.length > 1) {
                return changePrice(Double.valueOf(fileNames[0]), imagePath) + " (" + fileNames[1];
            } else {
                String price = fileNames[0].substring(0, fileNames[0].lastIndexOf("."));
                return changePrice(Double.valueOf(price), imagePath) + fileNames[0].substring(fileNames[0].lastIndexOf("."));
            }
        } catch (Exception e) {
            System.out.println(imagePath);
        }
        return fileName;
    }

    private static String changePrice(Double price, String imagePath) {
        if (price <= 16) {
            return "14.97";
        } else if (price <= 17) {
            return "15.97";
        } else if (price <= 18) {
            return "16.97";
        } else if (price <= 19) {
            return "17.97";
        } else if (price <= 20) {
            return "19.97";
        } else if (price <= 21) {
            return "21.97";
        } else if (price <= 22) {
            return "23.97";
        } else if (price <= 23) {
            return "25.97";
        } else if (price <= 24) {
            return "27.97";
        } else if (price <= 25) {
            return "29.97";
        } else {
            System.out.println(imagePath);
            return price.toString();
        }
    }
}
