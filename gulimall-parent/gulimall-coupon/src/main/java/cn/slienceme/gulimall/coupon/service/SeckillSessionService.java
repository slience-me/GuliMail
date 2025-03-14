package cn.slienceme.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-17 20:52:03
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SeckillSessionEntity> getSeckillSessionsIn3Days();
}

