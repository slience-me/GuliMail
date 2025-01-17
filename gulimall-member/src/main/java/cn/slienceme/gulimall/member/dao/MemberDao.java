package cn.slienceme.gulimall.member.dao;

import cn.slienceme.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-17 20:57:49
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
