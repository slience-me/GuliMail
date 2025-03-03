package cn.slienceme.gulimall.search.app;


import cn.slienceme.common.exception.BizCodeEnume;
import cn.slienceme.common.to.es.SkuEsModel;
import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    // 上架商品
    @PostMapping("/product")
    public R productStatesUp(@RequestBody List<SkuEsModel> skuEsModels) throws IOException {
        boolean b = false;
        try {
            b = productSaveService.productStatesUp(skuEsModels);
        } catch (Exception e) {
            log.error("ElasticSaveController.productStatesUp error: 商品上架错误", e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (b) {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        } else {
            return R.ok();
        }
    }
}
