package com.secondspin.product.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.product.dto.FavoriteProductListDTO;
import com.secondspin.product.dto.ProductListDTO;
import com.secondspin.product.dto.ViewHistoryDTO;
import com.secondspin.product.pojo.Favorites;
import com.secondspin.product.mapper.FavoritesMapper;
import com.secondspin.product.service.IFavoritesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondspin.product.service.IProductsService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements IFavoritesService {

    private final IProductsService productsService;

    public FavoritesServiceImpl(@Lazy IProductsService productsService) {
        this.productsService = productsService;
    }

    @Override
    public Boolean ifFavorite(Integer userId, Integer productId) {
        return lambdaQuery()
                .eq(Favorites::getUserId, userId)
                .eq(Favorites::getProductId, productId)
                .one() != null;
    }

    @Override
    public PageDTO<FavoriteProductListDTO> getFavorites(Integer userId, QueryDTO queryDTO) {
        // 默认查询条件
        if (queryDTO == null) {
            queryDTO = new QueryDTO();
        }

        queryDTO.setPageNo(queryDTO.getPageNo() != null ? queryDTO.getPageNo() : 1L);
        queryDTO.setPageSize(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10L);
        queryDTO.setIsAsc(queryDTO.getIsAsc() != null ? queryDTO.getIsAsc() : false);
        queryDTO.setSortBy(queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "addDate");

        Page<Favorites> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        page.setOptimizeCountSql(true);

        LambdaQueryChainWrapper<Favorites> queryWrapper = lambdaQuery()
                .eq(Favorites::getUserId, userId);

        switch (queryDTO.getSortBy()) {
            case "addDate":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Favorites::getAddDate);
                break;
            default:
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Favorites::getAddDate);
                break;
        }

        Page<Favorites> data = queryWrapper.page(page);

        List<Integer> productIds = data.getRecords().stream()
                .map(Favorites::getProductId)
                .toList();

        List<ProductListDTO> productList = productsService.getProductsByIdList(productIds);

        Map<Integer, ProductListDTO> productMap = productList.stream()
                .collect(Collectors.toMap(ProductListDTO::getProductId, Function.identity()));

        List<FavoriteProductListDTO> result = data.getRecords().stream()
                .map(favorite -> new FavoriteProductListDTO()
                        .setFavoriteId(favorite.getFavoriteId())
                        .setAddDate(favorite.getAddDate())
                        .setProduct(productMap.get(favorite.getProductId())))
                .toList();

        PageDTO<FavoriteProductListDTO> pageDTO = new PageDTO<>();
        pageDTO.setData(result);
        pageDTO.setTotal(data.getTotal());
        pageDTO.setTotalPage(data.getPages());

        return pageDTO;
    }
}
