package cn.slienceme.gulimall.seckill.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 远程调用前 通过拦截器 进行处理
                //1. 使用RequestContextHolder拿到老请求的请求数据 RequestContextHolder 上下文环境保持器
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                // 同步cookies
                if (requestAttributes != null) {
                    HttpServletRequest request = requestAttributes.getRequest(); // 老请求
                    if (request != null) {
                        //2. 将老请求得到cookie信息放到feign请求上
                        String cookie = request.getHeader("Cookie");
                        template.header("Cookie", cookie);  // 将老请求的cookie信息放到新请求上
                    }
                }
            }
        };
    }
}
