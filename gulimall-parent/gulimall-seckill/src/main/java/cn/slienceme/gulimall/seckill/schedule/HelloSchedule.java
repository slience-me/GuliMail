package cn.slienceme.gulimall.seckill.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * 1、@EnableScheduling 开启定时任务
 * 2、@Scheduled开启一个定时任务
 */
@Slf4j
//@EnableAsync
//@EnableScheduling
@Component
public class HelloSchedule {


    /**
     * 1、Spring中6位组成，依次为 秒 分 时 日 月 周几；不允许第七位 年
     * 2、在周几的位置 1-7代表周一到周日 MON-SUN
     * 3、定时任务不应该是阻塞的。默认是阻塞的
     *      1) 可以在方法上加注解 @Async
     *      2) 让定时任务异步执行 主动提交到线程池
     *      3) 支持定时任务线程池：设置 TaskSchedulingProperties 类的 pool 属性
     *
     *      4) 开启异步任务 @EnableAsync
     *      5) 在spring boot中，定时任务默认是阻塞的，如果定时任务有延迟，后面的定时任务会等前面的定时任务执行完再执行
     *
     * @throws InterruptedException
     */

    @Scheduled(cron = "0 0 3 * * ?")
    @Async
//    @Scheduled(cron = "* * * * * ?")
    public void hello() throws InterruptedException {
        Thread.sleep(3000);
        log.info("hello...");
    }
}
