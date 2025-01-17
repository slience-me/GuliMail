package cn.slienceme.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * 1. 整合mybatis-plus
 *      1) 导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *         </dependency>
 *      2) 配置
 *          配置数据源
 *            导入数据库驱动
 *            配置数据源
 *          配置mybatis-plus
 *            配置mapper的扫描MapperScan
 *            告诉Mybatis-plus，sql映射文件的位置
 *            调整主键自增
 */
@MapperScan("cn.slienceme.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
