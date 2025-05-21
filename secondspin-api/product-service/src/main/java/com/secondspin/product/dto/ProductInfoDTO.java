package com.secondspin.product.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ProductInfoDTO extends ProductListDTO {

    private Integer sellerId;

    // Seller information
    private String sellerName;

    private String sellerAvatarUrl;

    private String description;

    // Product images
    private List<String> imageUrls;

    private Integer categoryId;

    // Category information
    private String categoryName;

    private String categoryIconUrl;

    private Integer stock;

    // User favorite status
    private Boolean ifFavorite;
}
