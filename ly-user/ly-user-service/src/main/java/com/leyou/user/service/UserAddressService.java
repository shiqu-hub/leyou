package com.leyou.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.entity.UserAddress;

import java.util.List;


/**
 * <p>
 * 用户收货地址表 服务类
 * </p>
 *
 * @author kai
 * @since 2019-12-25
 */
public interface UserAddressService extends IService<UserAddress> {

    List<UserAddressDTO> findAddressListByUserId(Long userId);

    UserAddressDTO findById(Long id);
}
