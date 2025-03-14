package cn.slienceme.gulimall.member.web;


import cn.slienceme.common.utils.R;
import cn.slienceme.gulimall.member.feign.OrderFeignService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MemberWebController {

    private final OrderFeignService orderFeignService;

    public MemberWebController(OrderFeignService orderFeignService) {
        this.orderFeignService = orderFeignService;
    }

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, Model model){
        // 查出当前登录的用户的所有订单
        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(page);
        model.addAttribute("orders", r);
        return "orderList";
    }
}
