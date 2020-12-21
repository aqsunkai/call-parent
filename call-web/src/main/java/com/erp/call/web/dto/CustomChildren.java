package com.erp.call.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class CustomChildren {

    private VariationData variationData;

    private Integer seq;


    @Data
    public static class VariationData {

        private List<Item> items;


        @Data
        public static class Item {
            private String code;
            private String value;
        }
    }

}
