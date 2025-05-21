package com.secondspin.product.service.impl;

import com.secondspin.product.pojo.Favorites;
import com.secondspin.product.mapper.FavoritesMapper;
import com.secondspin.product.service.IFavoritesService;
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
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements IFavoritesService {

    @Override
    public Boolean ifFavorite(Integer userId, Integer productId) {
        return lambdaQuery()
                .eq(Favorites::getUserId, userId)
                .eq(Favorites::getProductId, productId)
                .one() != null;
    }
}
