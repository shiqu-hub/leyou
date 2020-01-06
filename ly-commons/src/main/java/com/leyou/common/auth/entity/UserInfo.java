package com.leyou.common.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor   //自动生成无参数构造函数
@AllArgsConstructor  //自动生成有参数构造函数
public class UserInfo {

    private Long id;

    private String username;
    
    private String role;
}
