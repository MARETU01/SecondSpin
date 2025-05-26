package com.secondspin.product.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ViewHistoryDTO {

    private Integer historyId;

    private LocalDateTime viewTime;

    private ProductListDTO product;
}
