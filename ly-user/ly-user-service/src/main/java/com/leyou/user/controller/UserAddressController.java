package com.leyou.user.controller;


import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.entity.UserAddress;
import com.leyou.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;


    @GetMapping("/list")
    public ResponseEntity<List<UserAddressDTO>> findAddressListByUserId(@RequestParam("userId") Long userId){
        List<UserAddressDTO> userAddressDTOList=userAddressService.findAddressListByUserId(userId);
        return ResponseEntity.ok().body(userAddressDTOList);
    }

    /**
     * 根据 地址id 获取地址信息
     * @param id 地址id
     * @return 地址信息
     */
    @GetMapping("/byId")
    public ResponseEntity<UserAddressDTO> queryAddressById(@RequestParam("id") Long id){
        UserAddressDTO userAddressDTO=userAddressService.findById(id);
        return ResponseEntity.ok().body(userAddressDTO);
    }
}
