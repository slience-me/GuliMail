package cn.slienceme.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GulimallCorsConfiguration {

    /**
     * 实现跨域
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter(){
        // 创建一个UrlBasedCorsConfigurationSource对象
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 创建一个CorsConfiguration对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        //1、配置跨域
        // 允许所有请求头
        corsConfiguration.addAllowedHeader("*");
        // 允许所有请求方法
        corsConfiguration.addAllowedMethod("*");
        // 允许所有请求来源
        corsConfiguration.addAllowedOrigin("*");
        // 允许发送Cookie
        corsConfiguration.setAllowCredentials(true);

        // 将CorsConfiguration对象注册到UrlBasedCorsConfigurationSource对象中
        source.registerCorsConfiguration("/**",corsConfiguration);
        // 返回CorsWebFilter对象
        return new CorsWebFilter(source);
    }
}
