package cn.slienceme.gulimall.member.service.impl;

import cn.slienceme.gulimall.member.dao.MemberLevelDao;
import cn.slienceme.gulimall.member.entity.MemberLevelEntity;
import cn.slienceme.gulimall.member.exception.PhoneExistException;
import cn.slienceme.gulimall.member.exception.UsernameExistException;
import cn.slienceme.gulimall.member.vo.MemberLoginVo;
import cn.slienceme.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.common.utils.Query;

import cn.slienceme.gulimall.member.dao.MemberDao;
import cn.slienceme.gulimall.member.entity.MemberEntity;
import cn.slienceme.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;
    @Autowired
    private MemberService memberService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo registVo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = new MemberEntity();

        // 设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevelId();
        entity.setLevelId(levelEntity.getId());

        // 检查用户名和手机号是否唯一  异常处理
        checkPhoneUnique(registVo.getPhone());
        checkUsernameUnique(registVo.getUserName());

        entity.setUsername(registVo.getUserName());
        entity.setMobile(registVo.getPhone());

        // 设置密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        entity.setPassword(passwordEncoder.encode(registVo.getPassword()));

        // 其他信息

        // 保存
        memberDao.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }


    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        MemberDao memberDao = this.baseMapper;
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        MemberEntity memberEntity = memberDao.selectOne(
                new QueryWrapper<MemberEntity>().eq("username", vo.getLoginacct())
                        .or()
                        .eq("mobile", vo.getLoginacct()));
        if (memberEntity == null) {
            // 登录失败
            return null;
        }
        // 密码匹配

        boolean matches = passwordEncoder.matches(vo.getPassword(), memberEntity.getPassword());
        if (matches) {
            return memberEntity;
        } else {
            return null;
        }
    }
}
