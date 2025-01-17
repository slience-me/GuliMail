package cn.slienceme.gulimall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("cn.slienceme.gulimall.coupon.dao")
@SpringBootApplication
@EnableDiscoveryClient
public class GulimailCouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimailCouponApplication.class, args);
	}

}
