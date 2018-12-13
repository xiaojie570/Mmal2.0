package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {
    private final static String COOKIE_DOMAIN = ".tomcat.com";
    private final static String COOLIE_NAME="mmall_login_token";

    /**
     * 写入登录 cookie
     * @param response
     * @param token
     */
    public static void writeLoginToken(HttpServletResponse response,String token) {
        Cookie cookie = new Cookie(COOLIE_NAME,token);
        cookie.setDomain(COOKIE_DOMAIN);
        // 单位是秒，这里设置有效期为一年。
        // 如果这个MaxAge不设置的话，不会写入硬盘，而是写在内存。只在当前页面有效
        cookie.setMaxAge(60 * 60 * 24 * 365);
        // 代表设置在根目录
        cookie.setPath("/");
        log.info("write cookieName:{}, cookieValue:{}",cookie.getName(),cookie.getValue());
        response.addCookie(cookie);
    }

    /**
     * 读取cookie信息
     */
    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie: cookies) {
                log.info("read cookieName:{}, cookieValue:{}",cookie.getName(),cookie.getValue());
                if(StringUtils.equals(cookie.getName(),COOLIE_NAME)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 注销的时候需要删除 cookie
     * 删除的时候又要读又要写
     */
    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(StringUtils.equals(cookie.getName(),COOLIE_NAME)) {
                    log.info("del cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
                    cookie.setPath("/");
                    cookie.setDomain(COOKIE_DOMAIN);
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    return;
                }
            }
        }
    }
}