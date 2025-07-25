package com.secondspin.product.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.product.dto.ProductListDTO;
import com.secondspin.product.dto.ViewHistoryDTO;
import com.secondspin.product.pojo.ViewHistory;
import com.secondspin.product.mapper.ViewHistoryMapper;
import com.secondspin.product.service.IProductsService;
import com.secondspin.product.service.IViewHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class ViewHistoryServiceImpl extends ServiceImpl<ViewHistoryMapper, ViewHistory> implements IViewHistoryService {

    private final IProductsService productsService;

    public ViewHistoryServiceImpl(@Lazy IProductsService productsService) {
        this.productsService = productsService;
    }

    @Override
    public Boolean addViewHistory(Integer userId, Integer productId) {
        if (userId == null || productId == null) {
            return false;
        }
        boolean updated = lambdaUpdate()
                .eq(ViewHistory::getUserId, userId)
                .eq(ViewHistory::getProductId, productId)
                .setSql("view_time = DEFAULT")
                .update();

        if (!updated) {
            ViewHistory viewHistory = new ViewHistory()
                    .setUserId(userId)
                    .setProductId(productId);
            return save(viewHistory);
        }
        return true;
    }

    @Override
    public PageDTO<ViewHistoryDTO> getViewHistory(Integer userId, QueryDTO queryDTO) {
        // 默认查询条件
        if (queryDTO == null) {
            queryDTO = new QueryDTO();
        }
        queryDTO.setPageNo(queryDTO.getPageNo() != null ? queryDTO.getPageNo() : 1L);
        queryDTO.setPageSize(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10L);
        queryDTO.setIsAsc(queryDTO.getIsAsc() != null ? queryDTO.getIsAsc() : false);
        queryDTO.setSortBy(queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "viewTime");

        Page<ViewHistory> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        page.setOptimizeCountSql(true);

        LambdaQueryChainWrapper<ViewHistory> queryWrapper = lambdaQuery()
                .eq(ViewHistory::getUserId, userId);

        switch (queryDTO.getSortBy()) {
            case "viewTime":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), ViewHistory::getViewTime);
                break;
            default:
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), ViewHistory::getViewTime);
                break;
        }

        Page<ViewHistory> data = queryWrapper.page(page);

        List<Integer> productIds = data.getRecords().stream()
                .map(ViewHistory::getProductId)
                .toList();

        List<ProductListDTO> productList = productsService.getProductsByIdList(productIds);

        Map<Integer, ProductListDTO> productMap = productList.stream()
                .collect(Collectors.toMap(ProductListDTO::getProductId, Function.identity()));

        List<ViewHistoryDTO> result = data.getRecords().stream()
                .map(history -> new ViewHistoryDTO()
                        .setHistoryId(history.getHistoryId())
                        .setViewTime(history.getViewTime())
                        .setProduct(productMap.get(history.getProductId())))
                .toList();

        PageDTO<ViewHistoryDTO> pageDTO = new PageDTO<>();
        pageDTO.setData(result);
        pageDTO.setTotal(data.getTotal());
        pageDTO.setTotalPage(data.getPages());

        return pageDTO;
    }
}
