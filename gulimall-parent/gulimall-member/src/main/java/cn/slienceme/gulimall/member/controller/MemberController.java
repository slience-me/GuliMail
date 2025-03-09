package cn.slienceme.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import cn.slienceme.common.exception.BizCodeEnume;
import cn.slienceme.gulimall.member.exception.PhoneExistException;
import cn.slienceme.gulimall.member.exception.UsernameExistException;
import cn.slienceme.gulimall.member.feign.CouponFeignService;
import cn.slienceme.gulimall.member.vo.GiteeUserVo;
import cn.slienceme.gulimall.member.vo.GiteeVo;
import cn.slienceme.gulimall.member.vo.MemberLoginVo;
import cn.slienceme.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.slienceme.gulimall.member.entity.MemberEntity;
import cn.slienceme.gulimall.member.service.MemberService;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.common.utils.R;



/**
 * 会员
 *
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-17 20:57:49
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member",memberEntity).put("coupons",memberCoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }

    @PostMapping("/oauth2/gitee/login")
    public R oauth2login(@RequestBody GiteeVo giteeVo){
        MemberEntity login = memberService.login(giteeVo);
        if (login == null){
            return R.error(BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getCode(),
                    BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getMsg());
        }
        return R.ok().put("memberEntity", login);
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity login = memberService.login(vo);
        if (login == null){
            return R.error(BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getCode(),
                    BizCodeEnume.LOGIN_ACCT_PASSWORD_EXCEPTION.getMsg());
        }
        return R.ok().put("memberEntity", login);
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo registVo){

        try {
            memberService.regist(registVo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(),BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
