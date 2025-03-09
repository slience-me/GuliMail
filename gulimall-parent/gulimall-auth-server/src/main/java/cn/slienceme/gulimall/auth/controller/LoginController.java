package cn.slienceme.gulimall.auth.controller;


import cn.slienceme.common.constant.AuthServerConstant;
import cn.slienceme.common.utils.R;
import cn.slienceme.common.vo.MemberRespVo;
import cn.slienceme.gulimall.auth.feign.MemberFeignService;
import cn.slienceme.gulimall.auth.feign.ThirdPartFeignService;
import cn.slienceme.gulimall.auth.vo.UserLoginVo;
import cn.slienceme.gulimall.auth.vo.UserRegistVo;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import cn.slienceme.common.exception.BizCodeEnume;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        //判断是否已经发送过验证码
        String redisCode = ops.get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            //判断是否在60s内发送过
            Long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                //发送过
                return R.error(BizCodeEnume.VALID_SMS_CODE_EXCEPTION.getCode(),
                        BizCodeEnume.VALID_SMS_CODE_EXCEPTION.getMsg());

            }
        }

        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        // sms:code: phone -> code
        ops.set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,
                code + '_' + System.currentTimeMillis(),
                10,
                TimeUnit.MINUTES);
        thirdPartFeignService.sendSmsCode(phone, code);
        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result,
                         RedirectAttributes attributes) throws InterruptedException {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,
                    FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //2.若JSR303校验通过
        //判断验证码是否正确
        String code = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        //2.1 如果对应手机的验证码不为空且与提交上的相等-》验证码正确
        if (!StringUtils.isEmpty(code) && vo.getCode().equals(code.split("_")[0])) {
            //2.1.1 使得验证后的验证码失效
            redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

            //2.1.2 远程调用会员服务注册
            R r = memberFeignService.regist(vo);
            if (r.getCode() == 0) {
                Map<String, String> errors = new HashMap<>();
                errors.put("msg", "注册成功");
                //调用成功，重定向登录页
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/login.html";
            }else {
                //调用失败，返回注册页并显示错误信息
                Map<String, String> errors = new HashMap<>();
                errors.put("msg", r.getData("msg", new TypeReference<String>(){}));
                //调用成功，重定向登录页
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            attributes.addFlashAttribute("errors", errors);
            //2.2 验证码错误
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @GetMapping("/login.html")
    private String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null){
            return "login";
        } else {
            return "redirect:http://gulimall.com";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes,
                        HttpSession session) {
        // 远程调用
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            MemberRespVo data = login.getData("memberEntity", new TypeReference<MemberRespVo>() {});
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
