package cn.slienceme.gulimall.thirdparty.controller;

import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;

    /**
     * 提供给别的服务调用的
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sms/sendCode")
    public R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code){
        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }
}
