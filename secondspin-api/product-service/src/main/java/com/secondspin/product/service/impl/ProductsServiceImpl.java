package com.secondspin.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondspin.api.client.UserClient;
import com.secondspin.api.dto.UserDTO;
import com.secondspin.common.dto.JwtUser;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.common.utils.ImagesUtils;
import com.secondspin.common.utils.RedisConstants;
import com.secondspin.product.dto.ProductInfoDTO;
import com.secondspin.product.dto.ProductListDTO;
import com.secondspin.product.enums.ProductStatus;
import com.secondspin.product.pojo.Categories;
import com.secondspin.product.pojo.ProductImages;
import com.secondspin.product.pojo.Products;
import com.secondspin.product.mapper.ProductsMapper;
import com.secondspin.product.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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

    Random random = new Random();

    private final IProductImagesService productImagesService;
    private final ICategoriesService categoriesService;
    private final IFavoritesService favoritesService;
    private final IViewHistoryService viewHistoryService;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserClient userClient;

    public ProductsServiceImpl(IProductImagesService productImagesService, ICategoriesService categoriesService, IFavoritesService favoritesService, IViewHistoryService viewHistoryService, StringRedisTemplate stringRedisTemplate, UserClient userClient) {
        this.productImagesService = productImagesService;
        this.categoriesService = categoriesService;
        this.favoritesService = favoritesService;
        this.viewHistoryService = viewHistoryService;
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
                List<String> imageNames = ImagesUtils.saveProductImages(files);
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
                throw new RuntimeException("Failed to save product images: " + e.getMessage());
            }
        }
        return product.getProductId();
    }

    @Override
    public PageDTO<ProductListDTO> getHomeProducts(QueryDTO queryDTO) {
        // 默认查询条件
        if (queryDTO == null) {
            queryDTO = new QueryDTO();
        }

        queryDTO.setPageNo(queryDTO.getPageNo() != null ? queryDTO.getPageNo() : 1L);
        queryDTO.setPageSize(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 30L);
        queryDTO.setIsAsc(queryDTO.getIsAsc() != null ? queryDTO.getIsAsc() : false);
        queryDTO.setSortBy(queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "postDate");

        final int CACHE_PAGE_LIMIT = 5;

        if (queryDTO.getPageNo() <= CACHE_PAGE_LIMIT) {
            String cachedResult = stringRedisTemplate.opsForValue().get(RedisConstants.PRODUCTS_HOME_KEY + queryDTO.getPageNo());
            if (cachedResult != null) {
                if (RedisConstants.NULL_VALUE.equals(cachedResult)) {
                    throw new RuntimeException("No products found");
                }
            }
            return JSON.parseObject(cachedResult, PageDTO.class);
        }

        Page<Products> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        page.setOptimizeCountSql(true);

        LambdaQueryChainWrapper<Products> queryWrapper = lambdaQuery().eq(Products::getStatus, ProductStatus.AVAILABLE);

        switch (queryDTO.getSortBy()) {
            case "price":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getPrice);
                break;
            case "viewCount":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getViewCount);
                break;
            case "favoriteCount":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getFavoriteCount);
                break;
            case "postDate":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getPostDate);
                break;
            default:
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getPostDate);
                break;
        }

        Page<Products> data = queryWrapper.page(page);

        List<ProductListDTO> result = getProductsByIdList(data.getRecords().stream()
                .map(Products::getProductId)
                .collect(Collectors.toList()));

        PageDTO<ProductListDTO> pageDTO = new PageDTO<>();
        pageDTO.setData(result);
        pageDTO.setTotal(data.getTotal());
        pageDTO.setTotalPage(data.getPages());

        if (queryDTO.getPageNo() <= CACHE_PAGE_LIMIT) {
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.PRODUCTS_HOME_KEY + queryDTO.getPageNo(),
                    JSON.toJSONString(pageDTO),
                    RedisConstants.PRODUCTS_HOME_TTL + random.nextInt(10),
                    TimeUnit.MINUTES
            );
        }
        return pageDTO;
    }

    @Override
    public ProductInfoDTO getProductInfo(JwtUser user, Integer id) {
        String productJson = stringRedisTemplate.opsForValue().get(RedisConstants.PRODUCT_INFO_KEY + id);
        if (productJson != null) {
            if (RedisConstants.NULL_VALUE.equals(productJson)) {
                throw new RuntimeException("Product not found");
            }
            ProductInfoDTO productInfo = JSON.parseObject(productJson, ProductInfoDTO.class);
            if (user != null) {
                productInfo.setIfFavorite(favoritesService.ifFavorite(user.getUserId(), id));
                // TODO: Update view count in the database
                viewHistoryService.addViewHistory(user.getUserId(), id);
            } else {
                // TODO Ensure that the view count is same in Redis and MYSQL
                lambdaUpdate()
                        .setIncrBy(Products::getViewCount, 1)
                        .eq(Products::getProductId, id)
                        .update();
            }
            return productInfo;
        }

        boolean locked = false;
        try {
            locked = Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(
                    RedisConstants.PRODUCT_INFO_LOCK_KEY + id,
                    "1",
                    RedisConstants.LOCK_TTL,
                    TimeUnit.SECONDS
            ));
            if (!locked) {
                Thread.sleep(500);
                return getProductInfo(user, id);
            } else {
                Products product = getById(id);
                if (product == null) {
                    stringRedisTemplate.opsForValue().set(
                            RedisConstants.PRODUCT_INFO_KEY + id,
                            RedisConstants.NULL_VALUE,
                            RedisConstants.NULL_CACHE_TTL,
                            TimeUnit.MINUTES
                    );
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
                        JSON.toJSONString(productInfo),
                        RedisConstants.PRODUCT_INFO_TTL + random.nextInt(10),
                        TimeUnit.MINUTES
                );

                if (user != null) {
                    productInfo.setIfFavorite(favoritesService.ifFavorite(user.getUserId(), product.getProductId()));
                    // TODO: Update view count in the database
                } else {
                    lambdaUpdate()
                            .setIncrBy(Products::getViewCount, 1)
                            .eq(Products::getProductId, product.getProductId())
                            .update();
                }

                return productInfo;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to get product info");
        } finally {
            if (locked) {
                stringRedisTemplate.delete(RedisConstants.PRODUCT_INFO_LOCK_KEY + id);
            }
        }
    }

    @Override
    public PageDTO<ProductListDTO> getPostProducts(Integer sellerId, QueryDTO queryDTO) {
        // 默认查询条件
        if (queryDTO == null) {
            queryDTO = new QueryDTO();
        }

        queryDTO.setPageNo(queryDTO.getPageNo() != null ? queryDTO.getPageNo() : 1L);
        queryDTO.setPageSize(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 30L);
        queryDTO.setIsAsc(queryDTO.getIsAsc() != null ? queryDTO.getIsAsc() : false);
        queryDTO.setFilter(queryDTO.getFilter() != null ? queryDTO.getFilter() : "");
        queryDTO.setSortBy(queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "postDate");

        Page<Products> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        page.setOptimizeCountSql(true);

        LambdaQueryChainWrapper<Products> queryWrapper = lambdaQuery().eq(Products::getSellerId, sellerId);

        switch (queryDTO.getFilter()) {
            case "available":
                queryWrapper.eq(Products::getStatus, ProductStatus.AVAILABLE);
                break;
            case "reserved":
                queryWrapper.eq(Products::getStatus, ProductStatus.RESERVED);
                break;
            case "sold":
                queryWrapper.eq(Products::getStatus, ProductStatus.SOLD);
                break;
            default:
                queryWrapper.ne(Products::getStatus, ProductStatus.SOLD);
                break;
        }

        switch (queryDTO.getSortBy()) {
            case "price":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getPrice);
                break;
            case "viewCount":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getViewCount);
                break;
            case "favoriteCount":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getFavoriteCount);
                break;
            case "postDate":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getPostDate);
                break;
            default:
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getPostDate);
                break;
        }

        Page<Products> data = queryWrapper.page(page);

        List<ProductListDTO> result = getProductsByIdList(data.getRecords().stream()
                .map(Products::getProductId)
                .collect(Collectors.toList()));

        PageDTO<ProductListDTO> pageDTO = new PageDTO<>();
        pageDTO.setData(result);
        pageDTO.setTotal(data.getTotal());
        pageDTO.setTotalPage(data.getPages());
        return pageDTO;
    }

    @Override
    public List<ProductListDTO> getProductsByIdList(List<Integer> Ids) {
        return lambdaQuery()
                .in(Products::getProductId, Ids)
                .list()
                .stream()
                .map(product -> {
                    ProductListDTO dto = new ProductListDTO();
                    dto.setProductId(product.getProductId())
                            .setTitle(product.getTitle())
                            .setPrice(product.getPrice())
                            .setOriginalPrice(product.getOriginalPrice())
                            .setCondition(product.getCondition())
                            .setStatus(product.getStatus())
                            .setPostDate(product.getPostDate())
                            .setViewCount(product.getViewCount())
                            .setFavoriteCount(product.getFavoriteCount());

                    productImagesService.getProductImages(product.getProductId())
                            .stream()
                            .filter(ProductImages::getPrimaryImage)
                            .findFirst()
                            .ifPresent(image -> dto.setPrimaryImageUrl(image.getImageUrl()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PageDTO<ProductListDTO> getProductsByCategoryId(Integer categoryId, QueryDTO queryDTO) {
        // 默认查询条件
        if (queryDTO == null) {
            queryDTO = new QueryDTO();
        }

        queryDTO.setPageNo(queryDTO.getPageNo() != null ? queryDTO.getPageNo() : 1L);
        queryDTO.setPageSize(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 30L);
        queryDTO.setIsAsc(queryDTO.getIsAsc() != null ? queryDTO.getIsAsc() : false);
        queryDTO.setSortBy(queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "postDate");

        final int CACHE_PAGE_LIMIT = 3;

        if (queryDTO.getPageNo() <= CACHE_PAGE_LIMIT) {
            String cachedResult = stringRedisTemplate.opsForValue().get(RedisConstants.PRODUCTS_CATEGORIES_KEY + categoryId + ":" + queryDTO.getPageNo());
            if (cachedResult != null) {
                if (RedisConstants.NULL_VALUE.equals(cachedResult)) {
                    throw new RuntimeException("No products found");
                }
            }
            return JSON.parseObject(cachedResult, PageDTO.class);
        }

        Page<Products> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        page.setOptimizeCountSql(true);

        LambdaQueryChainWrapper<Products> queryWrapper = lambdaQuery()
                .eq(Products::getStatus, ProductStatus.AVAILABLE)
                .eq(Products::getCategoryId, categoryId);

        switch (queryDTO.getSortBy()) {
            case "price":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getPrice);
                break;
            case "viewCount":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getViewCount);
                break;
            case "favoriteCount":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getFavoriteCount);
                break;
            case "postDate":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getPostDate);
                break;
            default:
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Products::getPostDate);
                break;
        }

        Page<Products> data = queryWrapper.page(page);

        List<ProductListDTO> result = getProductsByIdList(data.getRecords().stream()
                .map(Products::getProductId)
                .collect(Collectors.toList()));

        PageDTO<ProductListDTO> pageDTO = new PageDTO<>();
        pageDTO.setData(result);
        pageDTO.setTotal(data.getTotal());
        pageDTO.setTotalPage(data.getPages());

        if (queryDTO.getPageNo() <= CACHE_PAGE_LIMIT) {
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.PRODUCTS_CATEGORIES_KEY + categoryId + ":" + queryDTO.getPageNo(),
                    JSON.toJSONString(pageDTO),
                    RedisConstants.PRODUCTS_CATEGORIES_TTL + random.nextInt(10),
                    TimeUnit.MINUTES
            );
        }
        return pageDTO;
    }
}
