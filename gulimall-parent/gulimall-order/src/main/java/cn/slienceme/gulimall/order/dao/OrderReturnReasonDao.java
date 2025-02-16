package cn.slienceme.gulimall.order.dao;

import cn.slienceme.gulimall.order.entity.OrderReturnReasonEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退货原因
 * 
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-17 21:08:41
 */
@Mapper
public interface OrderReturnReasonDao extends BaseMapper<OrderReturnReasonEntity> {
	
}
