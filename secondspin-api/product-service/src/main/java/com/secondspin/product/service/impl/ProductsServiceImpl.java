package com.secondspin.product.service.impl;

import com.secondspin.api.client.UserClient;
import com.secondspin.api.dto.UserDTO;
import com.secondspin.common.utils.ImagesUtils;
import com.secondspin.product.pojo.ProductImages;
import com.secondspin.product.pojo.Products;
import com.secondspin.product.mapper.ProductsMapper;
import com.secondspin.product.service.IProductImagesService;
import com.secondspin.product.service.IProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements IProductsService {

    private final IProductImagesService productImagesService;
    private final UserClient userClient;

    public ProductsServiceImpl(IProductImagesService productImagesService, UserClient userClient) {
        this.productImagesService = productImagesService;
        this.userClient = userClient;
    }

    @Transactional
    @Override
    public Integer addProduct(Products product, MultipartFile[] files, Integer primaryOrder) {
        UserDTO user = userClient.getUser(Long.valueOf(product.getSellerId()));
        if (user == null) {
            throw new RuntimeException("User not found");
        } else if (!user.getAccountStatus().equals("active")) {
            throw new RuntimeException("User's status is not active");
        }
        save(product);
        if (files != null && files.length > 0) {
            try {
                List<String> imageNames = ImagesUtils.saveImages(files);
                primaryOrder = primaryOrder == null ? 1 : primaryOrder;
                List<ProductImages> productImages = new ArrayList<>();
                for (int i = 0; i < imageNames.size(); i++) {
                    ProductImages productImage = new ProductImages();
                    productImage.setProductId(product.getProductId());
                    productImage.setImageUrl(imageNames.get(i));
                    productImage.setOrder(i + 1);
                    productImage.setPrimaryImage(i + 1 == primaryOrder);
                    productImages.add(productImage);
                }
                productImagesService.saveBatch(productImages);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return product.getProductId();
    }
}
