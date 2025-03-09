package cn.slienceme.gulimall.auth.feign;

import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.auth.vo.GiteeVo;
import cn.slienceme.gulimall.auth.vo.UserLoginVo;
import cn.slienceme.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo registVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/gitee/login")
    R oauth2login(@RequestBody GiteeVo giteeVo);
}
