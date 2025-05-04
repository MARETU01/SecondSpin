package com.secondspin.user.service;

import com.secondspin.user.pojo.Address;
import com.baomidou.mybatisplus.extension.service.IService;
import com.secondspin.user.pojo.Users;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
public interface IAddressService extends IService<Address> {

    List<Address> getAddressByUserId(Integer userId);

    Boolean saveAddress(Users user, Address address);

    Boolean updateAddress(Users user, Address address);

    Boolean deleteAddress(Users user, Long id);
}
