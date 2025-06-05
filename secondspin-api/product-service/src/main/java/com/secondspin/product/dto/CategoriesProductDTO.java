package com.secondspin.product.dto;

import com.secondspin.common.dto.PageDTO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CategoriesProductDTO<T> {

    private Integer categoryId;

    private String name;

    private String description;

    private String iconUrl;

    private PageDTO<T> pageDTO;
}
