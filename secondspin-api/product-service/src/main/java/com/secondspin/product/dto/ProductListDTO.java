package com.secondspin.product.dto;

import com.secondspin.product.enums.Condition;
import com.secondspin.product.enums.ProductStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ProductListDTO {

    private Integer productId;

    private String title;

    // Primary image URL
    private String PrimaryImageUrl;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Condition condition;

    private ProductStatus status;

    private LocalDateTime postDate;

    private Integer viewCount;

    private Integer favoriteCount;
}
