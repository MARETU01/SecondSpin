package com.secondspin.product.service.impl;

import com.secondspin.product.pojo.Products;
import com.secondspin.product.mapper.ProductsMapper;
import com.secondspin.product.service.IProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements IProductsService {

}
