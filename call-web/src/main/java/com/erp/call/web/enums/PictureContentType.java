package com.erp.call.web.enums;

import org.apache.http.entity.ContentType;

/**
 * 图片类型
 */
public enum PictureContentType {

    //
    JPG(ContentType.IMAGE_JPEG),
    PNG(ContentType.IMAGE_PNG),
    BMP(ContentType.IMAGE_BMP),
    GIF(ContentType.IMAGE_GIF),
    JPEG(ContentType.IMAGE_JPEG);

    private ContentType type;

    PictureContentType(ContentType type) {
        this.type = type;
    }

    public static ContentType getContentType(String fileType) {
        for (PictureContentType enums : PictureContentType.values()) {
            if (enums.name().equalsIgnoreCase(fileType)) {
                return enums.getType();
            }
        }
        return ContentType.IMAGE_JPEG;
    }

    public ContentType getType() {
        return type;
    }
}
