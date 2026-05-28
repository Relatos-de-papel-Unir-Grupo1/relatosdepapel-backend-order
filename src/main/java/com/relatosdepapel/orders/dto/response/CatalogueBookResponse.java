package com.relatosdepapel.orders.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
        private BigDecimal rating;
        private Boolean visible;
        private Integer stock;
    }
}
