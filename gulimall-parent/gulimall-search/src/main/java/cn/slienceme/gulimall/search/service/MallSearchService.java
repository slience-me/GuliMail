package cn.slienceme.gulimall.search.service;

import cn.slienceme.gulimall.search.vo.SearchParam;
import cn.slienceme.gulimall.search.vo.SearchResult;

public interface MallSearchService {


    /**
     * 根据用户输入的条件进行检索
     * @param params 用户输入的检索条件
     * @return  检索结果
     */
    SearchResult search(SearchParam params);
}
