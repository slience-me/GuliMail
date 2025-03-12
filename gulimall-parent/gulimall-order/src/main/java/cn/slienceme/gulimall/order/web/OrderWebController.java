package cn.slienceme.gulimall.order.web;

import cn.slienceme.common.exception.NoStockException;
import cn.slienceme.gulimall.order.service.OrderService;
import cn.slienceme.gulimall.order.vo.OrderConfirmVo;
import cn.slienceme.gulimall.order.vo.OrderSubmitVo;
import cn.slienceme.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("confirmOrder", confirmVo);
        return "confirm";
    }

    @RequestMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes attributes) {
        // 下单： 去创建订单、验证令牌、验证价格、锁定库存
        try {
            // 下单成功 来到支付选择页
            SubmitOrderResponseVo responseVo = orderService.submitOrder(submitVo);
            Integer code = responseVo.getCode();
            if (code == 0) {
                model.addAttribute("order", responseVo.getOrder());
                return "pay";
            } else {
                // 下单失败回到订单确认页重新确认订单信息
                String msg = "下单失败;";
                switch (code) {
                    case 1:
                        msg += "防重令牌校验失败";
                        break;
                    case 2:
                        msg += "商品价格发生变化";
                        break;
                }
                attributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

            // 下单失败回到订单确认页重新确认订单信息
            if (e instanceof NoStockException) {
                String msg = "下单失败，商品无库存";
                attributes.addFlashAttribute("msg", msg);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
