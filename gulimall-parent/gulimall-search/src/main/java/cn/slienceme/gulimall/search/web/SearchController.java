package cn.slienceme.gulimall.search.web;

import cn.slienceme.gulimall.search.service.MallSearchService;
import cn.slienceme.gulimall.search.vo.SearchParam;
import cn.slienceme.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /**
     * 搜索请求
     * @param param
     * @param model
     * @return
     */
    @GetMapping({"/list.html"})
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {
        param.set_queryString(request.getQueryString());
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }

}
