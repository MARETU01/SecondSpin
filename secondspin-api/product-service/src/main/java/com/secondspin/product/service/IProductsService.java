package com.secondspin.product.service;

import com.secondspin.product.pojo.Products;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
public interface IProductsService extends IService<Products> {

    Integer addProduct(Products product, MultipartFile[] files, Integer primaryOrder);
}
