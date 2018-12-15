package com.mmall.controller.common;

import com.mmall.common.Const;
import com.mmall.common.RedisPool;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SessionExpireFilter implements Filter{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);

        if(StringUtils.isNotEmpty(loginToken)) {
            // 调用 logintoken是否为空或者为""
            // 如果不为空的话，符合条件，继续拿 user 信息
            String userjsonStr = RedisPoolUtil.get(loginToken);
            if(StringUtils.isNotEmpty(userjsonStr)) {
                User user = JsonUtil.String2Obj(userjsonStr, User.class);
                if (user != null) {
                    // 如果用户不为空，则重新设置session时间
                    RedisPoolUtil.expire(loginToken, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
                }
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}