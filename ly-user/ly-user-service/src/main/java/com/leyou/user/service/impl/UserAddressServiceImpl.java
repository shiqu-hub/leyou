package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.user.entity.UserAddress;
import com.leyou.user.mapper.UserAddressMapper;
import com.leyou.user.service.UserAddressService;
import org.springframework.stereotype.Service;

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

}
