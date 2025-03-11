package cn.slienceme.gulimall.order.feign;

import cn.slienceme.common.to.SkuHasStockTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {


    @GetMapping("/ware/waresku/getSkuHasStocks")
    List<SkuHasStockTo> getSkuHasStocks(List<Long> skuIds);
}
