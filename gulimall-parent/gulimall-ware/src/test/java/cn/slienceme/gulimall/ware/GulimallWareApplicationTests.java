package cn.slienceme.gulimall.ware;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GulimallWareApplicationTests {

    @Test
    public void contextLoads() {
        String token = IdWorker.getTimeId();
        System.out.println(token);
        System.out.println(token.length());
    }

}
