package com.secondspin.product.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.secondspin.product.enums.Condition;
import com.secondspin.product.enums.ProductStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("products")
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "product_id", type = IdType.AUTO)
    private Integer productId;

    private Integer sellerId;

    private String title;

    private String description;

    private Integer categoryId;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private Condition condition;

    private ProductStatus status;

    private LocalDateTime postDate;

    private Integer viewCount;

    private Integer favoriteCount;


}
