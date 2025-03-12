package cn.slienceme.gulimall.order.feign;

import cn.slienceme.common.to.SkuHasStockTo;
import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @RequestMapping("/ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);


    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @RequestMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo itemVos);
}
