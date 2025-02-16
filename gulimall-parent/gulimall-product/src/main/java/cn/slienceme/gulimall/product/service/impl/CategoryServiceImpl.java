package cn.slienceme.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.common.utils.Query;

import cn.slienceme.gulimall.product.dao.CategoryDao;
import cn.slienceme.gulimall.product.entity.CategoryEntity;
import cn.slienceme.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1. 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 组装成父子的树形结构
        //2. 找出所有一级分类
        List<CategoryEntity> level1Menus =
                entities.stream()
                        .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                        .map((menu) -> {
                            menu.setChildren(getChildrens(menu, entities));
                            return menu;
                        }).sorted((menu1, menu2) -> {
                            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
                        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> list) {
        // TODO

    }

    /**
     * 获取当前菜单的子菜单
     *
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream()
                .filter(categoryEntity -> {
                    return categoryEntity.getParentCid() == root.getCatId();
                }).map(categoryEntity -> {
                    // 递归找到子菜单
                    categoryEntity.setChildren(getChildrens(categoryEntity, all));
                    return categoryEntity;
                }).sorted((menu1, menu2) -> {
                    // 排序
                    return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
                }).collect(Collectors.toList());
        return children;
    }
}
