package com.f1ee.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.f1ee.reggie.common.BaseContext;
import com.f1ee.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
//       1.获取本次请求的URL
        String requestURL = request.getRequestURI();

        log.info("拦截到请求： {}",requestURL);

        String[] urls = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };
//        2.判断本次请求是否需要处理
        boolean check = check(urls, requestURL);

//        3.如果不需要处理，直接放行
        if (check) {
            log.info("本次请求{}不需要处理",requestURL);
            filterChain.doFilter(request,response);
            return;
        }
//        4.判断登录状态，如果已登录，直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，用户id为{}",request.getSession().getAttribute("employee"));
//            获取用户id，存入线程中
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }
//        4-1.判断用户登录状态
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，用户id为{}",request.getSession().getAttribute("user"));
//            获取用户id，存入线程中
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }
//        5.如果未登录则返回登录结果，通过输出流想客户端页面响应
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    public boolean check(String[] urls, String requestURL) {
        for (String url: urls) {
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {

    }
}
