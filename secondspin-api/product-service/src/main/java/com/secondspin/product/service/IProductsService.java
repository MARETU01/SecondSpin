package com.secondspin.product.service;

import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.product.dto.ProductInfoDTO;
import com.secondspin.product.dto.ProductListDTO;
import com.secondspin.product.pojo.Products;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    PageDTO<ProductListDTO> getHomeProducts(QueryDTO queryDTO);

    ProductInfoDTO getProductInfo(JwtUser user, Integer id);

    PageDTO<ProductListDTO> getPostProducts(Integer sellerId, QueryDTO queryDTO);

    List<ProductListDTO> getProductsByIdList(List<Integer> Ids);

    PageDTO<ProductListDTO> getProductsByCategoryId(Integer categoryId, QueryDTO queryDTO);
}
