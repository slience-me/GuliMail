package cn.slienceme.gulimall.ware.service;

import cn.slienceme.gulimall.ware.vo.FareVo;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.gulimall.ware.entity.WareInfoEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-17 21:07:08
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long addrId);
}

