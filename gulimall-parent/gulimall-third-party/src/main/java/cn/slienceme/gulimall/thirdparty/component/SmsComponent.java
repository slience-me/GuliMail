package cn.slienceme.gulimall.thirdparty.component;

import cn.slienceme.gulimall.thirdparty.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "sms")
@Data
@Component
public class SmsComponent {

    private String appcode;
    private String host;
    private String path;
    private String smsSignId;
    private String templateId;

    public void sendSmsCode(String phone, String code) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", phone);
        String params = "**code**:"+code+",**minute**:5";
        querys.put("param", params);
        querys.put("smsSignId", smsSignId);
        querys.put("templateId", templateId);
        Map<String, String> bodys = new HashMap<>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, "POST", headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body {"msg":"成功","smsid":"174124752435316889016228661","code":"0","balance":"4"}
            System.out.println("短信发送成功: phone="+phone+", code="+code);
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
