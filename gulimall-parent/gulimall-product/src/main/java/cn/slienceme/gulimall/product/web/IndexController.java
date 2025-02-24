package cn.slienceme.gulimall.product.web;

import cn.slienceme.gulimall.product.entity.CategoryEntity;
import cn.slienceme.gulimall.product.service.CategoryService;
import cn.slienceme.gulimall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {

        // TODO: 1、查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categories();

        // classpath:/templates/+返回值+.html
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @GetMapping("index/json/catalog.json")
    @ResponseBody
    public Map<String, List<Catalog2Vo>> getCategoryJson() {
        return categoryService.getCategoryJson();
    }
}
