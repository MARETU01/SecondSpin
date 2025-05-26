package com.secondspin.product.service;

import com.secondspin.common.dto.PageDTO;
import com.secondspin.common.dto.QueryDTO;
import com.secondspin.product.dto.ProductListDTO;
import com.secondspin.product.dto.ViewHistoryDTO;
import com.secondspin.product.pojo.ViewHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
public interface IViewHistoryService extends IService<ViewHistory> {

    Boolean addViewHistory(Integer userId, Integer productId);

    PageDTO<ViewHistoryDTO> getViewHistory(Integer userId, QueryDTO queryDTO);
}
