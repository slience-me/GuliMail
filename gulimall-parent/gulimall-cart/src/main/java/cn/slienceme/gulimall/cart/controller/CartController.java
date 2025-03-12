package cn.slienceme.gulimall.cart.controller;

import cn.slienceme.gulimall.cart.interceptor.CartInterceptor;
import cn.slienceme.gulimall.cart.service.CartService;
import cn.slienceme.gulimall.cart.vo.Cart;
import cn.slienceme.gulimall.cart.vo.CartItem;
import cn.slienceme.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems() {
        return cartService.getUserCartItems();
    }

    @RequestMapping("/checkItem.html")
    public String checkItem(@RequestParam("skuId")Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @RequestMapping("/countItem.html")
    public String changeItemCount(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @RequestMapping("/deleteItem.html")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * 浏览器有一个cookie：user-key标识用户身份、一个月过期
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     *
     * @param skuId
     * @param num
     * @param redirectAttributes
     *      redirectAttributes.addFlashAttribute() 将数据放在session里面取出，但是只能取一次
     *      redirectAttributes.addAttribute("skuId", skuId); 将数据放在URL上面
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        // 重定向到成功页面
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }

}
