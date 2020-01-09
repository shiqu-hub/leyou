package com.leyou.cart.interceptor;


import com.leyou.common.threadlocals.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kai
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            //从request中获取到userid
            String userId = request.getHeader("USER_ID");
            // 保存用户
            UserHolder.setUserId(userId); //放到ThreadLocal中，为了线程安全
            return true;
        } catch (Exception e) {
            // 解析失败，不继续向下
            log.error("【购物车服务】解析用户信息失败！", e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.removeUserId();
    }
}