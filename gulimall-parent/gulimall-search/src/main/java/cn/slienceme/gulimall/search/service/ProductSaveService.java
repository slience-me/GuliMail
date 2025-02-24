package cn.slienceme.gulimall.search.service;


import cn.slienceme.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStatesUp(List<SkuEsModel> skuEsModels) throws IOException;
}
