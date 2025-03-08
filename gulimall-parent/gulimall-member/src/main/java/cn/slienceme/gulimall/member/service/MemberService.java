package cn.slienceme.gulimall.member.service;

import cn.slienceme.gulimall.member.exception.PhoneExistException;
import cn.slienceme.gulimall.member.exception.UsernameExistException;
import cn.slienceme.gulimall.member.vo.MemberLoginVo;
import cn.slienceme.gulimall.member.vo.MemberRegistVo;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-17 20:57:49
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo registVo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);
}

