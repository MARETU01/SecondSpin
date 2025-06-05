package com.secondspin.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.common.utils.RedisConstants;
import com.secondspin.product.dto.CategoriesProductDTO;
import com.secondspin.product.dto.ProductListDTO;
import com.secondspin.product.pojo.Categories;
import com.secondspin.product.mapper.CategoriesMapper;
import com.secondspin.product.service.ICategoriesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondspin.product.service.IProductsService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
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
public class CategoriesServiceImpl extends ServiceImpl<CategoriesMapper, Categories> implements ICategoriesService {

    private final IProductsService productsService;
    private final StringRedisTemplate stringRedisTemplate;

    public CategoriesServiceImpl(@Lazy IProductsService productsService, StringRedisTemplate stringRedisTemplate) {
        this.productsService = productsService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<Categories> getAll() {
        String categoriesJson = stringRedisTemplate.opsForValue().get(RedisConstants.CATEGORIES_KEY);
        if (categoriesJson != null && !categoriesJson.isEmpty()) {
            return JSON.parseArray(categoriesJson, Categories.class);
        }
        List<Categories> categoriesList = list();
        if (categoriesList != null && !categoriesList.isEmpty()) {
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.CATEGORIES_KEY,
                    JSON.toJSONString(categoriesList)
            );
        }
        return categoriesList;
    }

    @Override
    public Categories getCategoryById(Integer id) {
        List<Categories> categoriesList = getAll();
        if (categoriesList != null && !categoriesList.isEmpty()) {
            return categoriesList.stream()
                    .filter(category -> id.equals(category.getCategoryId()))
                    .findFirst()
                    .orElse(null);
        }
        return getById(id);
    }

    @Override
    public CategoriesProductDTO<ProductListDTO> getProductsByCategoriesId(Integer id, QueryDTO queryDTO) {
        CategoriesProductDTO<ProductListDTO> categoriesProductDTO = new CategoriesProductDTO<>();
        Categories category = getCategoryById(id);

        PageDTO<ProductListDTO> productPage = productsService.getProductsByCategoryId(id, queryDTO);

        categoriesProductDTO
                .setCategoryId(category.getCategoryId())
                .setName(category.getName())
                .setDescription(category.getDescription())
                .setIconUrl(category.getIconUrl())
                .setPageDTO(productPage);
        return categoriesProductDTO;
    }
}
