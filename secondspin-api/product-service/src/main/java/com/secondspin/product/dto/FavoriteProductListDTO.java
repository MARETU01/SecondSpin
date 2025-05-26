package com.secondspin.product.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class FavoriteProductListDTO {

    private Integer favoriteId;

    private LocalDateTime addDate;

    private ProductListDTO product;
}
