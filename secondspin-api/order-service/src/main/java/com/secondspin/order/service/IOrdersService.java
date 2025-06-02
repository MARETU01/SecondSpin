package com.secondspin.order.service;

import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.order.dto.OrderListDTO;
import com.secondspin.order.pojo.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
public interface IOrdersService extends IService<Orders> {

    PageDTO<OrderListDTO> getOrders(Integer userId, QueryDTO queryDTO);

    Orders createOrder(Integer userId, Orders order);

    Orders updateOrder(Integer userId, Orders order);
}
