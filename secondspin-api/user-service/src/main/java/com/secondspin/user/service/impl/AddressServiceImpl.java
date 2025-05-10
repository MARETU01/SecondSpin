package com.secondspin.user.service.impl;

import com.secondspin.user.pojo.Address;
import com.secondspin.user.mapper.AddressMapper;
import com.secondspin.user.pojo.Users;
import com.secondspin.user.service.IAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

    @Override
    public List<Address> getAddressByUserId(Integer userId) {
        return lambdaQuery().eq(Address::getUserId, userId).list();
    }

    @Transactional
    @Override
    public Boolean saveAddress(Users user, Address address) {
        address.setUserId(user.getUserId());
        Boolean isDefault = address.getDefaultAddress() != null ? address.getDefaultAddress() : false;
        if (isDefault) {
            lambdaUpdate()
                .eq(Address::getUserId, user.getUserId())
                .eq(Address::getDefaultAddress, true)
                .set(Address::getDefaultAddress, false)
                .update();
        } else {
            Long count = lambdaQuery()
                    .eq(Address::getUserId, user.getUserId())
                    .count();
            address.setDefaultAddress(count == 0);
        }
        return save(address);
    }

    @Transactional
    @Override
    public Boolean updateAddress(Users user, Address address) {
        address.setUserId(user.getUserId());
        Boolean isDefault = address.getDefaultAddress() != null ? address.getDefaultAddress() : false;
        if (isDefault) {
            lambdaUpdate()
                .eq(Address::getUserId, user.getUserId())
                .eq(Address::getDefaultAddress, true)
                .ne(Address::getAddressId, address.getAddressId())
                .set(Address::getDefaultAddress, false)
                .update();
        } else {
            Long count = lambdaQuery()
                    .eq(Address::getUserId, user.getUserId())
                    .ne(Address::getAddressId, address.getAddressId())
                    .count();
            if (count == 0) {
                address.setDefaultAddress(true);
            } else {
                lambdaUpdate()
                        .eq(Address::getUserId, user.getUserId())
                        .ne(Address::getAddressId, address.getAddressId())
                        .set(Address::getDefaultAddress, true)
                        .last("limit 1")
                        .update();
            }
        }
        return updateById(address);
    }

    @Transactional
    @Override
    public Boolean deleteAddresses(Users user, List<Long> ids) {
        List<Address> addresses = listByIds(ids);
        Optional<Address> defaultAddressToDelete = addresses.stream()
                .filter(address -> address.getDefaultAddress() != null && address.getDefaultAddress())
                .findFirst();
        if (defaultAddressToDelete.isPresent()) {
            Boolean hasOtherAddress = lambdaQuery()
                    .eq(Address::getUserId, user.getUserId())
                    .notIn(Address::getAddressId, ids)
                    .exists();
            if (hasOtherAddress) {
                lambdaUpdate()
                        .eq(Address::getUserId, user.getUserId())
                        .notIn(Address::getAddressId, ids)
                        .set(Address::getDefaultAddress, true)
                        .last("limit 1")
                        .update();
            }
        }
        return removeByIds(ids);
    }

    @Override
    public Address getAddressById(Long id) {
        return getBaseMapper().getAddressById(id);
    }
}
