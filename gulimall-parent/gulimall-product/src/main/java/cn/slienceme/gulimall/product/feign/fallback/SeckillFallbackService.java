package cn.slienceme.gulimall.product.feign.fallback;

import cn.slienceme.common.exception.BizCodeEnume;
import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillFallbackService implements SeckillFeignService {
    @Override
    public R getSeckillSkuInfo(Long skuId) {
        log.info("熔断方法被调用getSeckillSkuInfo");
        return R.error(BizCodeEnume.TOO_MANY_REQUEST_EXCEPTION.getCode(), BizCodeEnume.TOO_MANY_REQUEST_EXCEPTION.getMsg());
    }
}
