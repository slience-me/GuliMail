package cn.slienceme.gulimall.product.feign;

import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {


    /**
     * R设计的时候可以加上泛型
     * @param skuIds
     * @return
     */
    @RequestMapping("/ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

}
