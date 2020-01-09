package com.leyou.order.interceptor;


import com.leyou.common.threadlocals.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            //从request中获取到userid
            String userId = request.getHeader("USER_ID");
            //放到ThreadLocal中，为了线程安全
            UserHolder.setUserId(userId);
            return true;
        } catch (Exception e) {
            // 解析失败，不继续向下
            log.error("【购物车服务】解析用户信息失败！",e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUserId();
    }
}
