package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.RegexUtils;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author kai
 * @since 2019-12-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                queryWrapper.lambda().eq(User::getUsername, data);
                break;
            case 2:
                queryWrapper.lambda().eq(User::getPhone, data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        int count = this.count(queryWrapper);
        return count == 0;
    }

    /**
     * KEY_PREFIX  redis中的key前缀
     */
    private static final String KEY_PREFIX = "ly:user:verify:phone:";

    @Override
    public void sendCode(String phone) {
        // 验证手机号格式
        if (!RegexUtils.isPhone(phone)) {
            throw new LyException(ExceptionEnum.INVALID_PHONE_NUMBER);
        }
        // 生成验证码
        String code = RandomStringUtils.randomNumeric(6);
        System.out.println(code);
        String param = "{\"code\":\"" + code + "\"}";
        // 保存验证码到redis,有效期1分钟
        redisTemplate.boundValueOps(KEY_PREFIX + phone).set(code, 1, TimeUnit.MINUTES);
        // 发送消息到ly-sms
/*        Map<String, String> map = new HashMap<>(4);
        map.put(SMS_PARAM_KEY_PHONE, phone);
        map.put(SMS_PARAM_KEY_SIGN_NAME, "mk白鹭");
        map.put(SMS_PARAM_KEY_TEMPLATE_CODE, "SMS_176531210");
        map.put(SMS_PARAM_KEY_TEMPLATE_PARAM, param);
        rocketMQTemplate.convertAndSend(SMS_TOPIC_NAME + ":" + VERIFY_CODE_TAGS, map);*/
    }

    @Override
    public void register(User user, String code) {
        // 1 校验验证码
        // 1.1 取出redis中的验证码
        String cacheCode = (String) redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        System.out.println(cacheCode);
        System.out.println(code);
        if (StringUtils.isBlank(cacheCode)) {
            throw new LyException(ExceptionEnum.INVALID_TIMEOUT_CODE);
        }
        // 1.2 比较验证码
        if (!StringUtils.equals(code, cacheCode)) {
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        // 2 对密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 3 写入数据库
        boolean isSave = this.save(user);
        if (!isSave) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //删除redis中的验证码
        redisTemplate.delete(KEY_PREFIX + user.getPhone());
    }

    @Override
    public UserDTO queryUserByUsernameAndPassword(String username, String password) {
        // 根据用户名查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getUsername, username);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        // 比较密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return BeanHelper.copyProperties(user, UserDTO.class);
    }
}
