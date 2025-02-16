package cn.slienceme.gulimall.coupon.dao;

import cn.slienceme.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-17 20:52:03
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
