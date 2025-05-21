package com.secondspin.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteProductListDTO {

    private Integer favoriteId;

    private LocalDateTime addDate;

    private ProductListDTO product;
}
