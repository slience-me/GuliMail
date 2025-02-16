package cn.slienceme.gulimall.member.feign;

import cn.slienceme.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/coupon/member/list")
    R memberCoupons();
}
