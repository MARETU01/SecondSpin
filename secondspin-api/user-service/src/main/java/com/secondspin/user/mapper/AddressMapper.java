package com.secondspin.user.mapper;

import com.secondspin.user.pojo.Address;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2025-04-23
 */
public interface AddressMapper extends BaseMapper<Address> {

    @Select("select * from address where address_id = #{id}")
    Address getAddressById(Long Id);
}
