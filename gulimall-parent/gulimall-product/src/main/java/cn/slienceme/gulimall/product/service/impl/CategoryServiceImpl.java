package cn.slienceme.gulimall.product.service.impl;

import cn.slienceme.gulimall.product.service.CategoryBrandRelationService;
import cn.slienceme.gulimall.product.vo.Catalog2Vo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.slienceme.common.utils.PageUtils;
import cn.slienceme.common.utils.Query;

import cn.slienceme.gulimall.product.dao.CategoryDao;
import cn.slienceme.gulimall.product.entity.CategoryEntity;
import cn.slienceme.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    /*缓存数据  局部缓存*/
    //private Map<String, Object> cache = new HashMap<>();


    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;

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
                        .filter(categoryEntity -> categoryEntity.getParentCid() == 0)  // 一级分类
                        .map((menu) -> {
                            menu.setChildren(getChildrens(menu, entities));
                            return menu;
                        }).sorted((menu1, menu2) -> {
                            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                        }).collect(Collectors.toList());

        return level1Menus;
    }

    /**
     * 获取当前菜单的子菜单
     *
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        // 使用stream流过滤出当前菜单的子菜单
        List<CategoryEntity> children = all.stream()
                .filter(categoryEntity -> {
                    // 如果子菜单的父级菜单id等于当前菜单的id，则返回true
                    return Objects.equals(categoryEntity.getParentCid(), root.getCatId());
                }).map(categoryEntity -> {
                    // 递归找到子菜单
                    categoryEntity.setChildren(getChildrens(categoryEntity, all));
                    return categoryEntity;
                }).sorted((menu1, menu2) -> {
                    // 排序
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }).collect(Collectors.toList());
        return children;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        //创建一个List<Long>类型的列表，用于存储路径
        List<Long> paths = new ArrayList<>();
        //调用findParentPath方法，传入catelogId和paths，返回一个List<Long>类型的列表
        List<Long> parentPath = findParentPath(catelogId, paths);
        //将parentPath列表中的元素顺序反转
        Collections.reverse(parentPath);
        //将parentPath列表转换为Long数组，并返回
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

        // 同时修改缓存中的数据
        // redis.del();等待下次主动查询进行更新
    }

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    // TODO 堆外内存溢出：OutofDirectMemoryError
    // 1)、spring2.0以后默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信
    // 2)、lettuce的底层netty会创建很多线程，可能会占用过多的内存，如果创建的线程过多可能会导致内存溢出，所以需要限制netty的线程数量
    // // 3)、springboot2.0以后默认使用lettuce作为操作redis的客户端，如果需要使用jedis作为客户端，需要在配置文件中配置
    // 可以通过-Dio.netty.maxDirectMemory来设置netty的堆外内存大小，默认是64M，如果不够可以调大
    // 1) 升级lettuce客户端版本  2)切换使用jedis   3) 减少netty的线程数量
    @Override
    public Map<String, List<Catalog2Vo>> getCategoryJson() {

        /**
         * 1. 空结果缓存：解决缓存穿透问题
         * 2. 设置过期时间：解决缓存雪崩问题(加随机值)
         * 3. 加锁：解决缓存击穿问题
         */

        // 给缓存中放json字符串，拿出的json字符串，还用逆转为能用的对象类型{序列号反序列化}
        // 1. 加入缓存逻辑
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String categoryJson = ops.get("categoryJson");
        if (StringUtils.isEmpty(categoryJson)) {
            // 2. 缓存中没有，查询数据库
            return getCategoryJsonFromDBWithRedisLock();
        }
        // 转为对象类型
        Map<String, List<Catalog2Vo>> result =
                JSON.parseObject(categoryJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
        return result;
    }

    /**
     * 使用redisson实现分布式锁
     * 缓存里面的数据如何和数据库保持一致
     * 缓存数据一致性
     * 1. 双写模式
     * 2. 失效模式
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCategoryJsonFromDBWithRedissonLock() {
        // 1. 占分布式锁，去数据库查询数据
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catalog2Vo>> dataFromDB = null;
        try {
            dataFromDB = getCategoryJsonFromDB();
        } finally {
            lock.unlock();
        }
        return dataFromDB;
    }

    public Map<String, List<Catalog2Vo>> getCategoryJsonFromDBWithRedisLock() {

        // 1. 占分布式锁，去redis占坑  // 设置过期时间
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300L, TimeUnit.SECONDS);
        if (lock) {
            // 加锁成功，执行业务
            try {
                Map<String, List<Catalog2Vo>> categoryJsonFromDB = getCategoryJsonFromDB();
                return categoryJsonFromDB;
            } finally {
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // 释放锁，必须保证锁的原子性，lua脚本
                redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

        } else {
            // 休眠一段时间
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCategoryJsonFromDBWithRedisLock(); // 自旋的方式

        }


    }

    private Map<String, List<Catalog2Vo>> getCategoryJsonFromDB() {
        // 得到锁以后，再去缓存查一次
        String categoryJson = redisTemplate.opsForValue().get("categoryJson");
        if (!StringUtils.isEmpty(categoryJson)) {
            return JSON.parseObject(categoryJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
        System.out.println("method:getCategoryJsonFromDB 缓存未命中");
        /*
         * 现在进行优化，多次查询数据库改为一次查询数据库
         */
        //优化业务逻辑，仅查询一次数据库
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //查出所有一级分类
        List<CategoryEntity> level1Categorys = getCategoryByParentCid(selectList, 0L);
        // 2. 封装数据
        Map<String, List<Catalog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = baseMapper.selectList(
                    new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(),
                            null, l2.getCatId().toString(), l2.getName());
                    // 查到这个二级分类的三级分类
                    List<CategoryEntity> level3Catelog = baseMapper.selectList(
                            new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                    if (level3Catelog != null) {
                        List<Catalog2Vo.Catalog3Vo> catelog3VoList = level3Catelog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catelog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(),
                                    l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catelog3VoList);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        // 1. 空结果缓存：解决缓存穿透问题
        // 2. 设置过期时间（加随机值）: 解决缓存雪崩问题
        // 3. 将数据库中查询到的数据存入缓存中   解决缓存雪崩问题(加随机值)
        redisTemplate.opsForValue().set("categoryJson", JSON.toJSONString(parentCid), 1L, TimeUnit.DAYS);
        return parentCid;
    }


    public Map<String, List<Catalog2Vo>> getCategoryJsonFromDBWithLocalLock() {


        // 加锁的方式
        // 1. 只要是分布式系统，就一定要用分布式锁
        // 2. 只要是分布式锁，就一定要考虑过期时间
        // 3. 只要涉及到过期时间，就要考虑到原子操作

        // this情况  只要是同一把锁，就能锁住需要这个锁的所有线程
        // 1. synchronized (this) 只能本类锁，锁不住其他类, SpringBoot容器中，每个bean都是单例的，所以锁不住
        // TODO: 本地锁: synchronized JUC(lock) 锁当前进程 ，锁不住其他进程
        // TODO: 分布式锁: redis(分布式锁) zookeeper(分布式锁)


        // 2. synchronized (CategoryServiceImpl.class) 锁住的是这个类，这个类只有一个实例，所以锁住了
        // 3. synchronized (new CategoryServiceImpl()) 锁住的是这个对象，这个对象是动态创建的，每次都是新的，所以锁不住


        synchronized (this) {
            // 得到锁以后，再去缓存查一次
            String categoryJson = redisTemplate.opsForValue().get("categoryJson");
            if (!StringUtils.isEmpty(categoryJson)) {
                System.out.println("method:getCategoryJsonFromDB 缓存命中");
                return JSON.parseObject(categoryJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
                });
            }
            System.out.println("method:getCategoryJsonFromDB 缓存未命中");
            // 1. 先从缓存中获取数据
            //Map<String, List<Catalog2Vo>> categoryJson = (Map<String, List<Catalog2Vo>>) cache.get("categoryJson");
            //if (cache.get("categoryJson") == null) {
            // 2. 如果缓存中没有数据，则从数据库中获取数据
            /*
             * 现在进行优化，多次查询数据库改为一次查询数据库
             */
            //优化业务逻辑，仅查询一次数据库
            List<CategoryEntity> selectList = baseMapper.selectList(null);
            //查出所有一级分类
            List<CategoryEntity> level1Categorys = getCategoryByParentCid(selectList, 0L);
            // 2. 封装数据
            Map<String, List<Catalog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                // 每一个的一级分类，查到这个一级分类的二级分类
                List<CategoryEntity> categoryEntities = baseMapper.selectList(
                        new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
                List<Catalog2Vo> catalog2Vos = null;
                if (categoryEntities != null) {
                    catalog2Vos = categoryEntities.stream().map(l2 -> {
                        Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(),
                                null, l2.getCatId().toString(), l2.getName());
                        // 查到这个二级分类的三级分类
                        List<CategoryEntity> level3Catelog = baseMapper.selectList(
                                new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                        if (level3Catelog != null) {
                            List<Catalog2Vo.Catalog3Vo> catelog3VoList = level3Catelog.stream().map(l3 -> {
                                Catalog2Vo.Catalog3Vo catelog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(),
                                        l3.getCatId().toString(), l3.getName());
                                return catelog3Vo;
                            }).collect(Collectors.toList());
                            catalog2Vo.setCatalog3List(catelog3VoList);
                        }

                        return catalog2Vo;
                    }).collect(Collectors.toList());
                }
                return catalog2Vos;
            }));
            // 1. 空结果缓存：解决缓存穿透问题
            // 2. 设置过期时间（加随机值）: 解决缓存雪崩问题
            // 3. 将数据库中查询到的数据存入缓存中   解决缓存雪崩问题(加随机值)
            redisTemplate.opsForValue().set("categoryJson", JSON.toJSONString(parentCid), 1L, TimeUnit.DAYS);
            return parentCid;
        }
    }

    private List<CategoryEntity> getCategoryByParentCid(List<CategoryEntity> categoryEntities, Long parentCid) {
        return categoryEntities.stream()
                .filter(cat -> cat.getParentCid().equals(parentCid))
                .collect(Collectors.toList());
    }



        /*// 1. 查询所有1级分类
        List<CategoryEntity> level1Categorys = getLevel1Categories();

        // 2. 封装数据
        Map<String, List<Catalog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = baseMapper.selectList(
                    new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(),
                            null, l2.getCatId().toString(), l2.getName());
                    // 查到这个二级分类的三级分类
                    List<CategoryEntity> level3Catelog = baseMapper.selectList(
                            new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                    if (level3Catelog != null) {
                        List<Catalog2Vo.Catalog3Vo> catelog3VoList = level3Catelog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catelog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(),
                                    l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catelog3VoList);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return parentCid;*/


    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }
}
