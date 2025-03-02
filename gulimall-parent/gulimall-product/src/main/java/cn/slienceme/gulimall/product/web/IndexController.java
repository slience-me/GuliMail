package cn.slienceme.gulimall.product.web;

import cn.slienceme.gulimall.product.entity.CategoryEntity;
import cn.slienceme.gulimall.product.service.CategoryService;
import cn.slienceme.gulimall.product.vo.Catalog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {

        // TODO: 1、查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categories();

        // classpath:/templates/+返回值+.html
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @GetMapping("/index/json/catalog.json")
    @ResponseBody
    public Map<String, List<Catalog2Vo>> getCategoryJson() {
        return categoryService.getCategoryJson();
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        // 1. 获取一把锁，只要锁的名字一样，就是同一把锁
        RLock mylock = redisson.getLock("mylock");

        // mylock.lock()
        // 2. 加锁，并且设置锁过期时间，防止死锁
        // 1) 锁的自动续期 如果业务超长， 运行期间自动给锁续上新的30s;不用担心业务时间长，过期被删除
        // 2) 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认会在30s内自动过期，不会产生死锁问题
        // 阻塞式等待 加锁 默认加的锁都是30s 自动续期，如果业务超长，运行期间自动给锁续上30s，不用担心业务时间长，过期被删除
        mylock.lock(10, TimeUnit.SECONDS); // 10s自动解锁，自动解锁时间一定要大于业务执行时间
        // 问题：lock.lock(10, TimeUnit.SECONDS); 在锁时间到了以后，不会自动续期
        // 1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时时间就是 我们制定的时间
        // 2、如果我们未指定锁的超时时间，就使用 lockWatchdogTimeout = 30 * 1000 【看门狗默认时间】
        // 只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】,每隔10s都会自动续期，续成30s
        // internalLockLeaseTime 【看门狗时间】 / 3， 10s

        // 最佳实战
        // 1. lock.lock(30, TimeUnit.SECONDS); 省掉了整个续期操作。手动解锁
        try {
            System.out.println("加锁成功,执行业务..." + Thread.currentThread().getId());
            Thread.sleep(30000);
            return "hello";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 3. 解锁，必须要在finally中解锁，防止死锁
            mylock.unlock();
            System.out.println("解锁成功" + Thread.currentThread().getId());
        }
    }

    // 保证一定能读取到最新的数据，修改期间，写锁是一个排他锁，读锁是共享锁
    // 读 + 读 ：相当于无锁，并发读，只会在redis中记录好，所有当前的读锁。他们都会同时加锁成功
    // 写 + 读 ：写锁没释放 读就必须等待
    // 写 + 写 ：阻塞方式
    // 读 + 写 ：有读锁。写也需要等待
    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock wLock = lock.writeLock();
        try {
            // 1. 改数据加写锁，读数据加读锁
            wLock.lock();  // 加锁
            System.out.println("wLock加锁成功" + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            wLock.unlock(); // 释放锁
        }
        return "write:"+s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        String s = "";
        try {
            rLock.lock();
            System.out.println("rLock加锁成功" + Thread.currentThread().getId());
            Thread.sleep(30000);
            s = redisTemplate.opsForValue().get("writeValue");
            System.out.println(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
        }
        return "read:"+s;
    }
}
