package cn.slienceme.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//@SpringBootTest
class GulimallMemberApplicationTests {

    @Test
    void contextLoads() {
//        String s = DigestUtils.md5Hex("123456");
//        System.out.println(s);
//        String s1 = Md5Crypt.md5Crypt("123456".getBytes(), "$1$gulimall");
//        System.out.println(s1);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode("123456");
        System.out.println(encode);
        boolean matches = bCryptPasswordEncoder.matches("123456", encode);
        System.out.println(matches);
        // $2a$10$BdxTPBRkb5ShnItoRcGKWe7CXUcRvxc.h5ZDptDPylso7KOfdgyeG
        // $2a$10$EnevNGYc7W5e.CqdLB7OKufh4j5ZxFCqjDfFR.oxw.BB2fQcQ.m1y
    }

}
