package com.secondspin.order.service.impl;

import com.secondspin.order.pojo.Orders;
import com.secondspin.order.mapper.OrdersMapper;
import com.secondspin.order.service.IOrdersService;
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
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

}
