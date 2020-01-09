package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.utils.BeanHelper;
import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.entity.UserAddress;
import com.leyou.user.mapper.UserAddressMapper;
import com.leyou.user.service.UserAddressService;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.List;

/**
 * <p>
 * 用户收货地址表 服务实现类
 * </p>
 *
 * @author kai
 * @since 2019-12-25
 */
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    public List<UserAddressDTO> findAddressListByUserId(Long userId) {
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        //根据userId查询userAddress的集合
        queryWrapper.lambda().eq(UserAddress::getId,userId);
        List<UserAddress> userAddressList = this.list(queryWrapper);
        return BeanHelper.copyWithCollection(userAddressList,UserAddressDTO.class);
    }

    @Override
    public UserAddressDTO findById(Long id) {
        UserAddress userAddress = this.getById(id);
        return BeanHelper.copyProperties(userAddress,UserAddressDTO.class);

    }
}
