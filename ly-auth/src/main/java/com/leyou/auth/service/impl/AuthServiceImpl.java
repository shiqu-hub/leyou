package com.leyou.auth.service.impl;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private UserClient userClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String USER_ROLE = "role_user";

    @Override
    public void login(String username, String password, HttpServletResponse response) {
        try {
//      查询用户
            UserDTO user = userClient.queryUserByUsernameAndPassword(username, password);
            if (user.getId()==null){
                throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
            }
//      生成userInfo, 没写权限功能，暂时都用guest
            UserInfo userInfo = new UserInfo(user.getId(), user.getUsername(), USER_ROLE);
//      生成token
            String token = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), prop.getUser().getExpire());
//      写入cookie
            CookieUtils.newCookieBuilder()
                    .response(response) /* response,用于写cookie */
                    .httpOnly(true)     /* 保证安全防止XSS攻击，不允许JS操作 */
                    .domain(prop.getUser().getCookieDomain())  /* 设置domain */
                    .name(prop.getUser().getCookieName()).value(token)  /* 设置cookie名称和值 */
                    .build(); /* 写cookie */
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }

    @Override
    public UserInfo verifyUser(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 读取cookie
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            // 获取token信息
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            // 获取token的id，校验黑名单
            String id = payload.getId();
            Boolean isKey = redisTemplate.hasKey(id);
            if (isKey!=null&&isKey){
                // 抛出异常，证明token无效，直接返回401
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }
            // 获取过期时间
            Date expiration = payload.getExpiration();
            // 获取刷新时间
            DateTime refreshTime = new DateTime(expiration.getTime()).minusMinutes(prop.getUser().getMinRefreshInterval());
            // 判断是否已经过了刷新时间
            if (refreshTime.isBefore(System.currentTimeMillis())){
                // 如果过了刷新时间，则生成新token
                token = JwtUtils.generateTokenExpireInMinutes(payload.getUserInfo(), prop.getPrivateKey(), prop.getUser().getExpire());
            //写入cookie
                CookieUtils.newCookieBuilder()
                        // response,用于写cookie
                        .response(response)
                        // 保证安全防止XSS攻击，不允许JS操作cookie
                        .httpOnly(true)
                        // 设置domain
                        .domain(prop.getUser().getCookieDomain())
                        // 设置cookie名称和值
                        .name(prop.getUser().getCookieName()).value(token)
                        // 写cookie
                        .build();
            }
            return payload.getUserInfo();
        } catch (Exception e) {
            log.error("用户信息认证失败",e);
            // 抛出异常，证明token无效，直接返回401
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 获取token
        String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
        // 解析token
        Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        // 获取id和有效期剩余时长
        String id = payload.getId();
        long time = payload.getExpiration().getTime() - System.currentTimeMillis();
        //写入redis,剩余时间超过5秒以上才写
        if (time>5000){
            redisTemplate.boundValueOps(payload.getId()).set("",time, TimeUnit.MICROSECONDS);
        }
        // 删除cookie
        CookieUtils.deleteCookie(prop.getUser().getCookieName(),prop.getUser().getCookieDomain(),response);
    }
}
