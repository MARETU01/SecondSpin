package com.secondspin.product.service;

import com.secondspin.product.pojo.Categories;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
public interface ICategoriesService extends IService<Categories> {

    List<Categories> getAll();

    Categories getCategoryById(Integer id);
}
