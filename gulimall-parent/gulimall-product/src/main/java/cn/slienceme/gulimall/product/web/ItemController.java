package cn.slienceme.gulimall.product.web;

import cn.slienceme.gulimall.product.service.SkuInfoService;
import cn.slienceme.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 商品详情页
     *
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable Long skuId, Model model) {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }
}
