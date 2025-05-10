package com.secondspin.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.secondspin.common.utils.RedisConstants;
import com.secondspin.product.pojo.Categories;
import com.secondspin.product.mapper.CategoriesMapper;
import com.secondspin.product.service.ICategoriesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    private final StringRedisTemplate stringRedisTemplate;

    public CategoriesServiceImpl(StringRedisTemplate stringRedisTemplate) {
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
}
