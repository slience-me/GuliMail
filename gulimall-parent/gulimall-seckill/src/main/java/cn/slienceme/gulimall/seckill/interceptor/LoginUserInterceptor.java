package cn.slienceme.gulimall.seckill.interceptor;

import cn.slienceme.common.constant.AuthServerConstant;
import cn.slienceme.common.vo.MemberRespVo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器，未登录的用户不能进入订单服务
 */
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/kill", requestURI);
        if (match) {
            HttpSession session = request.getSession();
            MemberRespVo memberResponseVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
            if (memberResponseVo != null) {
                loginUser.set(memberResponseVo);
                return true;
            }else {
                session.setAttribute("msg","请先登录");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        }
        return true;
    }
}
