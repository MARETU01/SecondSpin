package com.secondspin.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProductViewDTO {

    private Integer productId;

    private String title;

    private String primaryImageUrl;
}
