package com.secondspin.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.secondspin.api.client.UserClient;
import com.secondspin.api.dto.UserDTO;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.utils.ImagesUtils;
import com.secondspin.common.utils.RedisConstants;
import com.secondspin.product.dto.ProductInfoDTO;
import com.secondspin.product.pojo.Categories;
import com.secondspin.product.pojo.ProductImages;
import com.secondspin.product.pojo.Products;
import com.secondspin.product.mapper.ProductsMapper;
import com.secondspin.product.service.ICategoriesService;
import com.secondspin.product.service.IFavoritesService;
import com.secondspin.product.service.IProductImagesService;
import com.secondspin.product.service.IProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ICategoriesService categoriesService;
    private final IFavoritesService favoritesService;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserClient userClient;

    public ProductsServiceImpl(IProductImagesService productImagesService, ICategoriesService categoriesService, IFavoritesService favoritesService, StringRedisTemplate stringRedisTemplate, UserClient userClient) {
        this.productImagesService = productImagesService;
        this.categoriesService = categoriesService;
        this.favoritesService = favoritesService;
        this.stringRedisTemplate = stringRedisTemplate;
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

    @Override
    public ProductInfoDTO getProductInfo(JwtUser user, Integer id) {
        String productJson = stringRedisTemplate.opsForValue().get(RedisConstants.PRODUCT_INFO_KEY + id);
        if (productJson != null && !productJson.isEmpty()) {
            ProductInfoDTO productInfo = JSON.parseObject(productJson, ProductInfoDTO.class);
            if (user != null) {
                productInfo.setIfFavorite(favoritesService.ifFavorite(user.getUserId(), id));
            }
            return productInfo;
        }

        Products product = getById(id);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        ProductInfoDTO productInfo = new ProductInfoDTO();
        productInfo.setProductId(product.getProductId())
                .setTitle(product.getTitle())
                .setPrice(product.getPrice())
                .setOriginalPrice(product.getOriginalPrice())
                .setCondition(product.getCondition())
                .setStatus(product.getStatus())
                .setPostDate(product.getPostDate())
                .setViewCount(product.getViewCount())
                .setFavoriteCount(product.getFavoriteCount());
        productInfo.setSellerId(product.getSellerId())
                .setDescription(product.getDescription())
                .setStock(product.getStock())
                .setCategoryId(product.getCategoryId());

        UserDTO seller = userClient.getUser(Long.valueOf(product.getSellerId()));
        productInfo.setSellerName(seller.getUsername())
                .setSellerAvatarUrl(seller.getAvatarUrl());

        Categories category = categoriesService.getCategoryById(product.getCategoryId());
        productInfo.setCategoryName(category.getName())
                .setCategoryIconUrl(category.getIconUrl());

        List<ProductImages> productImages = productImagesService.getProductImages(product.getProductId());
        if (productImages != null && !productImages.isEmpty()) {
            List<String> imageUrls = productImages.stream().map(ProductImages::getImageUrl).collect(Collectors.toList());
            productInfo.setImageUrls(imageUrls);
            productImages.stream()
                    .filter(ProductImages::getPrimaryImage)
                    .findFirst()
                    .ifPresent(primaryImage -> productInfo.setPrimaryImageUrl(primaryImage.getImageUrl()));
        }

        stringRedisTemplate.opsForValue().set(
                RedisConstants.PRODUCT_INFO_KEY + product.getProductId(),
                JSON.toJSONString(productInfo)
        );

        if (user != null) {
            productInfo.setIfFavorite(favoritesService.ifFavorite(user.getUserId(), product.getProductId()));
        }

        return productInfo;
    }
}
