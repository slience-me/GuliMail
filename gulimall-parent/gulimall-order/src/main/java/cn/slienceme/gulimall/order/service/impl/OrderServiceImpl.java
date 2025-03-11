package cn.slienceme.gulimall.order.service.impl;

import cn.slienceme.common.vo.MemberRespVo;
import cn.slienceme.gulimall.order.feign.CartFeignService;
import cn.slienceme.gulimall.order.feign.MemberFeignService;
import cn.slienceme.gulimall.order.feign.WareFeignService;
import cn.slienceme.gulimall.order.interceptor.LoginUserInterceptor;
import cn.slienceme.gulimall.order.vo.MemberAddressVo;
import cn.slienceme.gulimall.order.vo.OrderConfirmVo;
import cn.slienceme.gulimall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.common.utils.Query;

import cn.slienceme.gulimall.order.dao.OrderDao;
import cn.slienceme.gulimall.order.entity.OrderEntity;
import cn.slienceme.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import cn.slienceme.common.to.SkuHasStockTo;
import cn.slienceme.gulimall.order.constant.OrderConstant;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        // 获取当前登录用户信息
        MemberRespVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        OrderConfirmVo confirmVo = new OrderConfirmVo();
        // 解决 线程安全问题 使用异步 线程池
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> itemAndStockFuture = CompletableFuture.supplyAsync(() -> {
            // 解决 线程安全问题 使用异步 线程池
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 查出所有选中购物项
            List<OrderItemVo> checkedItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(checkedItems);
            return checkedItems;
        }, executor).thenAcceptAsync((items) -> {
            // 库存
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            //skuId为key,是否有库存为value
            Map<Long, Boolean> hasStockMap = wareFeignService.getSkuHasStocks(skuIds).stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
            confirmVo.setStocks(hasStockMap);
        }, executor);

        // 查出所有收货地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 当前登录用户地址信息
            List<MemberAddressVo> addressByUserId = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setMemberAddressVos(addressByUserId);
        }, executor);

        // 查询用户积分
        confirmVo.setIntegration(memberResponseVo.getIntegration());

        // 总价自动计算
        // 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        try {
            CompletableFuture.allOf(itemAndStockFuture, addressFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return confirmVo;
    }

}
