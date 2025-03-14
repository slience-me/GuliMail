package cn.slienceme.gulimall.member.interceptor;


import cn.slienceme.common.constant.AuthServerConstant;
import cn.slienceme.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", requestURI);
        if (match) {
            return true;
        }
        HttpSession session = request.getSession();
        MemberRespVo memberRespVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberRespVo != null) {
            loginUser.set(memberRespVo);
            return true;
        }else {
            session.setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }

}
