package com.secondspin.order.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondspin.api.client.ProductClient;
import com.secondspin.api.dto.ProductViewDTO;
import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.order.dto.OrderListDTO;
import com.secondspin.order.enums.OrderStatus;
import com.secondspin.order.pojo.Orders;
import com.secondspin.order.mapper.OrdersMapper;
import com.secondspin.order.service.IOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    private final ProductClient productClient;

    public OrdersServiceImpl(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Override
    public PageDTO<OrderListDTO> getOrders(Integer userId, QueryDTO queryDTO) {
        // 默认查询条件
        if (queryDTO == null) {
            queryDTO = new QueryDTO();
        }
        queryDTO.setPageNo(queryDTO.getPageNo() != null ? queryDTO.getPageNo() : 1L);
        queryDTO.setPageSize(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10L);
        queryDTO.setIsAsc(queryDTO.getIsAsc() != null ? queryDTO.getIsAsc() : false);
        queryDTO.setFilter(queryDTO.getFilter() != null ? queryDTO.getFilter() : "all");
        queryDTO.setSortBy(queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "createTime");

        Page<Orders> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        page.setOptimizeCountSql(true);

        LambdaQueryChainWrapper<Orders> queryWrapper = lambdaQuery()
                .eq(Orders::getBuyerId, userId);

        switch (queryDTO.getFilter()) {
            case "all":
                break;
            case "pending":
                queryWrapper.eq(Orders::getStatus, OrderStatus.PENDING);
                break;
            case "shipped":
                queryWrapper.eq(Orders::getStatus, OrderStatus.SHIPPED);
                break;
            case "delivered":
                queryWrapper.eq(Orders::getStatus, OrderStatus.COMPLETED);
                break;
            case "cancelled":
                queryWrapper.eq(Orders::getStatus, OrderStatus.CANCELLED);
                break;
            case "refunded":
                queryWrapper.eq(Orders::getStatus, OrderStatus.REFUNDED);
                break;
            default:
                break;
        }

        switch (queryDTO.getSortBy()) {
            case "createTime":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Orders::getCreateTime);
                break;
            case "price":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Orders::getPrice);
                break;
            case "payTime":
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Orders::getPayTime);
                break;
            default:
                queryWrapper.orderBy(true, queryDTO.getIsAsc(), Orders::getCreateTime);
                break;
        }

        Page<Orders> data = queryWrapper.page(page);

        List<ProductViewDTO> productViews = productClient.getProductView(
                data.getRecords().stream()
                        .map(Orders::getProductId)
                        .toList()
        );

        Map<Integer, ProductViewDTO> productMap = productViews.stream()
                .collect(Collectors.toMap(ProductViewDTO::getProductId, Function.identity()));

        List<OrderListDTO> orderList = data.getRecords().stream()
                .map(order -> {
                    OrderListDTO dto = new OrderListDTO();
                    dto.setOrderId(order.getOrderId())
                            .setProductId(order.getProductId())
                            .setCreateTime(order.getCreateTime())
                            .setPrice(order.getPrice())
                            .setStatus(order.getStatus())
                            .setPayId(order.getPayId())
                            .setPayTime(order.getPayTime());

                    ProductViewDTO product = productMap.get(order.getProductId());
                    dto.setTitle(product.getTitle())
                            .setPrimaryImageUrl(product.getPrimaryImageUrl());
                    return dto;
                })
                .toList();

        PageDTO<OrderListDTO> pageDTO = new PageDTO<>();
        pageDTO.setData(orderList);
        pageDTO.setTotal(data.getTotal());
        pageDTO.setTotalPage(data.getPages());

        return pageDTO;
    }
}
