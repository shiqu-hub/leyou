package com.leyou.user.controller;

import com.leyou.common.exceptions.LyException;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;


@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 校验数据是否可用
     *
     * @param data 用户输入的数据
     * @param type 1- 用户名  2 手机号
     * @return 是否正确
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        return ResponseEntity.ok(userService.checkData(data, type));
    }

    /**
     * 发送短信验证码
     *
     * @param phone 用户输入的手机号
     * @return 状态码204
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone) {
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 用户注册
     *
     * @param user 用户注册信息
     * @param code 短信验证码
     * @return 状态码
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code") String code) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.joining("|"));
            throw new LyException(500, errorMessage);
        }
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @GetMapping("/query")
    public ResponseEntity<UserDTO> queryUserByUsernameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username, password));
    }

    /**
     * 校验数据是否可用
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/checkTest/{data}/{type}")
    @ApiOperation(value = "校验用户名数据是否可用，如果不存在则可用")
    @ApiResponses({
            @ApiResponse(code = 200, message = "校验结果有效，true或false代表可用或不可用"),
            @ApiResponse(code = 400, message = "请求参数有误，比如type不是指定值")
    })
    public ResponseEntity<Boolean> checkUser(
            @ApiParam(value = "要校验的数据", example = "lisi") @PathVariable("data") String data,
            @ApiParam(value = "数据类型，1：用户名，2：手机号", example = "1") @PathVariable(value = "type") Integer type) {
        return ResponseEntity.ok(userService.checkData(data, type));
    }
}
