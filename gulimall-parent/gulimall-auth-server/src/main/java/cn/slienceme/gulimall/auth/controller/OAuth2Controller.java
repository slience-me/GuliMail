package cn.slienceme.gulimall.auth.controller;


import cn.slienceme.common.constant.AuthServerConstant;
import cn.slienceme.gulimall.auth.feign.MemberFeignService;
import cn.slienceme.gulimall.auth.utils.HttpUtils;
import cn.slienceme.gulimall.auth.vo.GiteeUserVo;
import cn.slienceme.gulimall.auth.vo.GiteeVo;
import cn.slienceme.common.vo.MemberRespVo;
import cn.slienceme.gulimall.auth.vo.SocialUser;
import com.alibaba.fastjson.JSON;
import cn.slienceme.common.utils.R;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/*
 * 处理社交登录请求
 * */

@Slf4j
@Controller("/")
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    @RequestMapping("/oauth2.0/gitee/success")
    public String gitee(String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {
        //1. 使用code换取token，换取成功则继续2，否则重定向至登录页
        Map<String, String> query = new HashMap<>();
        query.put("client_id", "a7214341cfec72ff34b11d8e78c2801830ef1d31337e4f4ba1cb09260ec3f5b6");
        query.put("client_secret", "dc24d5729a6152c25d20b28c18693ed5826196eee6bea3c5186c0edb25f791e2");
        query.put("grant_type", "authorization_code");
        query.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/success");
        query.put("code", code);
        //发送post请求换取token
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<String, String>(), query, new HashMap<String, String>());
        Map<String, String> errors = new HashMap<>();
        if (response.getStatusLine().getStatusCode() == 200) {
            //2. 调用member远程接口进行oauth登录，登录成功则转发至首页并携带返回用户信息，否则转发至登录页
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            HashMap<String, String> params = new HashMap<>();
            params.put("access_token", socialUser.getAccess_token());
            HttpResponse getData = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<String, String>(), params);

            if (getData.getStatusLine().getStatusCode() == 200) {
                String userJson = EntityUtils.toString(getData.getEntity());
                GiteeUserVo giteeUser = JSON.parseObject(userJson, GiteeUserVo.class);
                GiteeVo giteeVo = new GiteeVo();
                BeanUtils.copyProperties(giteeUser, giteeVo);
                giteeVo.setSocial_access_token(socialUser.getAccess_token());
                giteeVo.setSocial_token_type(socialUser.getToken_type());
                giteeVo.setSocial_expires_in(socialUser.getExpires_in());
                giteeVo.setSocial_refresh_token(socialUser.getRefresh_token());
                giteeVo.setSocial_scope(socialUser.getScope());
                giteeVo.setSocial_created_at(socialUser.getCreated_at());
                R oauth2login = memberFeignService.oauth2login(giteeVo);

                //2.1 远程调用成功，返回首页并携带用户信息
                if (oauth2login.getCode() == 0) {
                    MemberRespVo data = oauth2login.getData("memberEntity", new TypeReference<MemberRespVo>() {
                    });
                    log.info("登录成功:{}", data.toString());
                    // 将用户信息存入session
                    session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                    // new Cookie("JSESSIONID", "").setDomain("gulimall.com")
                    // servletResponse.addCookie();
                    return "redirect:http://gulimall.com";
                } else {
                    //2.2 否则返回登录页
                    errors.put("msg", "登录失败，请重试");
                    session.setAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/login.html";
                }
            } else {
                //2.2 否则返回登录页
                errors.put("msg", "登录失败，请重试");
                session.setAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            errors.put("msg", "获得第三方授权失败，请重试");
            session.setAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
