package cn.slienceme.gulimall.order.config;

import cn.slienceme.gulimall.order.vo.PayVo;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "9021000146625903";
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCkQzY23TZJ8LjRZKJ0TrwESehltWAsXvu+NiOMKhMf5K3RXHIwcBIZKZjggNVDbwG1wdNNR7kIHtG2fqVV/UTOG9MPcnkvCdiX15R/1TLoIwHoO4lLdSG7G34+YfgmTak0XGJw9DHw0s0Iku4y0Oh3REzP90xCRbA/cOhKDYtXOKefByUb6K9Mq3xu2AajNLhdiDbD0QjI7I29rsvCwDtcXn/TlmjoI6q2FPB4CCvZFdHOn2jM+XfpUndLYnHAT/Zfaf8mVgtNydBcjBftZiP9GHYmun1yfgAvamfR0sUhReolYMO7UfaHBKdnVhzh9OWY5JbixbhSjHedEz3qhwWrAgMBAAECggEARkrQz2eYjMmrfUewUU9EHB24ipW0QyieWAZ47ckvh+1nCHmkD+BVjlkDp8bZ/FJri+kW0DJKFxSBL7RAcmzAXecASdL10tBG1KuXS6DjeP1KGqQnm5fTSGt6eZ2ZAGMpblO+eJG3MVQ0E2A0+J/3atpaeIBeLNn/kxZxAQeMwlS0IREkPHB1pFeqgV8oNQcdqiFaijejRoSMUFo1Ak6HplIeewzovoKT5TgSCgbYeSK1y3fy/ASA/irxeXBEKfKP10GV/yWY23wYrSMxJpLpO/oI7kZOLQkEgoN31aWf2r0NU4EQJ9Kug8CLpkLDybCMYPJAkA2k2s+YM4mMQcvEKQKBgQD7EUUc7DhSRtvao4A2VnoYEY+eHEQxreKB6C76x/4CP9jkcuRQ2F9v8HwhiK0buYqrlg7O5aYSeXMCNWWsLlUjY1/6x6R1wbq+ZuyACk/mb2OIl/tkEAkWtxdEwmmlfaeDl6Pxc8pQIUvmtnhwSilK4ssHq7hj4bJqh5Sv5EIJ9wKBgQCnfVx6Zuh5PV8cOKD2MGtnyoF+pvbeaCDeRFjj88xPZQnP8gWgvkRit/pb4ebdoFKExzj1E4UeDGdTFssEhCmGoP/0m6MdryOPvA/wvjMF+28pfY5SuYmuyggG7RNJY13FvxcTLDhG/qVJqT13BkxPp6Tn2ygfNdDQMp/rkSeU7QKBgQD4cnWXEncFjFNBv+w/WUIHEVGyn7woAS+VaAMdbc0BwtbJ8pvXGNcQecUMlijft7Lu61aKDR1TSNWziM2tHkhn9Y3kJ62qTeSTSFyetBWvLeksIWHG+ktULCidhVlwR7D9+pBkfjXlJ53pmAYE8I5+KYHKfHbCeHTLEJHloRZsrwKBgEtHfRlGx5Y2j1kP2sjMWnn7+tgCE1NDWgKMladfHKVStGZSUVU+L15vcod5sVpLhMtI/8CnVV18FbSyuez9uexY0LIQgfAFl+YykLeTxTsbF4t+c0mbOtbk11bFl7WUg8EtciGb7m96ZLAG8SleG05x+xx61D2y4nexSU+HnlLBAoGAZt0+1dPfKO/uv9bE7ecfoVOHaZYqxX6d8niLJAZr7uBDxxUFv1CA2J3q+mXIQcgF4/AvE4j+Lz3rGiAHHiXY5tWQ+XxchQJ0qG0wZ/jxuEOzyqC074kScZKbjEp/RkYfb7ZX+ydTQROlJUWZXDdT5XMjL3fe0Yju3vOtD72lbLY=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1Gb6BhnY5cSHeGjJpNO4u+Cl72aDGShquOyDnEZZjiQrR/Yj4Bc9g4bRIxsrE1Bcoc4KUmW/272ogpc1GJIMgMP4cb+r0+WDlK8xgd8EaHNWrq6ffQyYChQHMXvM7XWJa4+a86cfZ90xXD+3srlCtgyuSROEpyneL7n+azm9AO9bxr+N8Q4e9f+8ueYHWT29rwjEGsYQutlYYgQ/95G0GtdZjfWaLNhWF3uch1XFJPQyHd9P4jPNjJLSrgn5Ej5cCcYk1UaAeQ+9yYj0vu0ZPuil6zOv7GqFudmMQOEn6D7iuAK7gl0ou2+f9MfJyztxbkcKzasH1moekxuZ0foabQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://4np15by71716.vicp.fun/payed/notify";
    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    private String return_url = "http://member.gulimall.com/memberOrder.html";
    private String sign_type = "RSA2"; // 签名方
    private String charset = "UTF-8"; // 字符编码格式
    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String server_url = "https://openapi-sandbox.dl.alipaydev.com/gateway.do"; //沙箱环境

    public String pay(PayVo vo) throws AlipayApiException {

        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(server_url);
        alipayConfig.setAppId(app_id);
        alipayConfig.setPrivateKey(merchant_private_key);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipay_public_key);
        alipayConfig.setCharset(charset);
        alipayConfig.setSignType(sign_type);

        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();

        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        model.setOutTradeNo(vo.getOut_trade_no());  //商户订单号，商户网站订单系统中唯一订单号，必填
        model.setTotalAmount(vo.getTotal_amount());  //付款金额，必填
        model.setSubject(vo.getSubject());  //订单名称，必填
        model.setBody(vo.getBody());  //商品描述，可空
        model.setTimeoutExpress("10m");  // 设置订单超时时间，可空
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        alipayRequest.setBizModel(model);

        AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest, "POST");
        String pageRedirectionData = response.getBody();
        //System.out.println(pageRedirectionData);
        return pageRedirectionData;

    }
}
