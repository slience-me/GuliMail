package cn.slienceme.gulimall.product.dao;

import cn.slienceme.gulimall.product.entity.SkuSaleAttrValueEntity;
import cn.slienceme.gulimall.product.vo.SkuItemSaleAttrVo;
import cn.slienceme.gulimall.product.vo.SkuItemVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-16 21:54:47
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {
    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);
}
