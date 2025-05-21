package com.secondspin.product.service;

import com.secondspin.product.pojo.Favorites;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
public interface IFavoritesService extends IService<Favorites> {

    Boolean ifFavorite(Integer userId, Integer productId);
}
