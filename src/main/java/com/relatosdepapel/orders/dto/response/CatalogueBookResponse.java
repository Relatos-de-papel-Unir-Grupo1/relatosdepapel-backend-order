package com.relatosdepapel.orders.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CatalogueBookResponse {
    private boolean success;
    private String message;
    private BookData data;

    @Getter
    @Setter
    public static class BookData {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private String category;
        private Double rating;
        private Boolean visible;
        private Integer stock;
        private BigDecimal unitPrice;
    }
}
