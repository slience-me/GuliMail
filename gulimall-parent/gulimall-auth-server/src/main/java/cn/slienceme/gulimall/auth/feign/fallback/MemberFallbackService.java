//package cn.slienceme.gulimall.auth.feign.fallback;
//
//import cn.slienceme.common.exception.BizCodeEnum;
//import cn.slienceme.common.utils.R;
//import cn.slienceme.gulimall.auto.feign.MemberFeignService;
//import cn.slienceme.gulimall.auto.vo.SocialUser;
//import cn.slienceme.gulimall.auto.vo.UserLoginVo;
//import cn.slienceme.gulimall.auto.vo.UserRegisterVo;
//import org.springframework.stereotype.Service;
//
//@Service
//public class MemberFallbackService implements MemberFeignService {
//    @Override
//    public R register(UserRegisterVo registerVo) {
//        return R.error(BizCodeEnum.READ_TIME_OUT_EXCEPTION.getCode(), BizCodeEnum.READ_TIME_OUT_EXCEPTION.getMsg());
//    }
//
//    @Override
//    public R login(UserLoginVo loginVo) {
//        return R.error(BizCodeEnum.READ_TIME_OUT_EXCEPTION.getCode(), BizCodeEnum.READ_TIME_OUT_EXCEPTION.getMsg());
//    }
//
//    @Override
//    public R login(SocialUser socialUser) {
//        return R.error(BizCodeEnum.READ_TIME_OUT_EXCEPTION.getCode(), BizCodeEnum.READ_TIME_OUT_EXCEPTION.getMsg());
//    }
//}
