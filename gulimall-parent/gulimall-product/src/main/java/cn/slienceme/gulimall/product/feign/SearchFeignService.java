package cn.slienceme.gulimall.product.feign;

import cn.slienceme.common.to.es.SkuEsModel;
import cn.slienceme.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {


    @PostMapping("/search/save/product")
    R productStatesUp(@RequestBody List<SkuEsModel> skuEsModels);

}
