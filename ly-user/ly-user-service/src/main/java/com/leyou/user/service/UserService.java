package com.leyou.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;


/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author kai
 * @since 2019-12-25
 */
public interface UserService extends IService<User> {

    Boolean checkData(String data, Integer type);

    void sendCode(String phone);

    void register(User user, String code);

    UserDTO queryUserByUsernameAndPassword(String username, String password);
}
