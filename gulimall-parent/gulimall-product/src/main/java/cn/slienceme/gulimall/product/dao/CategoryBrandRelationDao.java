package cn.slienceme.gulimall.product.dao;

import cn.slienceme.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 *
 * @author slience_me
 * @email slienceme.cn@gmail.com
 * @date 2025-01-16 21:54:47
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);

}
