package cn.slienceme.gulimall.product.service.impl;

import cn.slienceme.gulimall.product.entity.SkuImagesEntity;
import cn.slienceme.gulimall.product.entity.SpuInfoDescEntity;
import cn.slienceme.gulimall.product.service.*;
import cn.slienceme.gulimall.product.vo.SkuItemSaleAttrVo;
import cn.slienceme.gulimall.product.vo.SkuItemVo;
import cn.slienceme.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.common.utils.Query;

import cn.slienceme.gulimall.product.dao.SkuInfoDao;
import cn.slienceme.gulimall.product.entity.SkuInfoEntity;

import org.springframework.util.StringUtils;

@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AttrGroupService attrGroupService;
//    @Autowired
//    private Product productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {

            queryWrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");

        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);

                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                log.error("价格参数有误");
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        //1、sku基本信息的获取  pms_sku_info
        SkuInfoEntity skuInfoEntity = this.getById(skuId);
        skuItemVo.setInfo(skuInfoEntity);
        Long spuId = skuInfoEntity.getSpuId();
        Long catalogId = skuInfoEntity.getCatalogId();

        // 判断是否有货
//        Boolean hasStock = productFeignService.getHasStock(skuInfoEntity.getHasStock());

        //2、sku的图片信息    pms_sku_images
        List<SkuImagesEntity> skuImagesEntities = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
        skuItemVo.setImages(skuImagesEntities);

        //4、获取spu的介绍-> 依赖1 获取spuId
        SpuInfoDescEntity byId = spuInfoDescService.getById(spuId);
        skuItemVo.setDesc(byId);

        //3、获取spu的销售属性组合-> 依赖1 获取spuId
        List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
        skuItemVo.setSaleAttr(saleAttrVos);

        //5、获取spu的规格参数信息-> 依赖1 获取spuId catalogId
        List<SpuItemAttrGroupVo> spuItemAttrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        skuItemVo.setGroupAttrs(spuItemAttrGroupVos);
        //TODO 6、秒杀商品的优惠信息

        return skuItemVo;
    }

}
