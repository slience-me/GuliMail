package cn.slienceme.gulimall.thirdparty;

import cn.slienceme.gulimall.thirdparty.component.SmsComponent;
import cn.slienceme.gulimall.thirdparty.utils.HttpUtils;
import com.aliyun.oss.OSSClient;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {

    @Autowired
    private SmsComponent smsComponent;

    @Test
    public void sendSms() {
        //smsComponent.sendSmsCode("19822086556","666666");
    }


}
