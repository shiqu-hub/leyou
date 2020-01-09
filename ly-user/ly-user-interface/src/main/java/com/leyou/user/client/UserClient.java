package com.leyou.user.client;


import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("user-service")
public interface UserClient {


    /**
     * 根据用户名和密码查询用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @GetMapping("/query")
    public UserDTO queryUserByUsernameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    );

    /**
     * 根据
     *
     * @param id 地址id
     * @return 地址信息
     */
    @GetMapping("/address/byId")
    public UserAddressDTO queryAddressById(@RequestParam("id") Long id);

    /**
     * 减库存
     *
     * @param cartMap 商品id及数量的map
     */
    @PutMapping("/stock/minus")
    public void minusStock(Map<Long, Integer> cartMap);
}
