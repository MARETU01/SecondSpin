package com.secondspin.product.service.impl;

import com.secondspin.product.pojo.ProductImages;
import com.secondspin.product.mapper.ProductImagesMapper;
import com.secondspin.product.service.IProductImagesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
@Service
public class ProductImagesServiceImpl extends ServiceImpl<ProductImagesMapper, ProductImages> implements IProductImagesService {

    @Override
    public List<ProductImages> getProductImages(Integer productId) {
        return lambdaQuery().eq(ProductImages::getProductId, productId).list();
    }
}
