package cn.slienceme.gulimall.auth.feign;

import cn.slienceme.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {

    //发送验证码
    @GetMapping("/sms/sendCode")
    R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
