//package cn.slienceme.gulimall.thirdparty;
//
//import com.aliyun.oss.OSSClient;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//
//@SpringBootTest
//public class testUpload {
//
//    @Autowired
//    OSSClient ossClient;
//
//
//    @Test
//    public void testUploadFile() throws FileNotFoundException {
//        InputStream inputStream = new FileInputStream("D:\\codeHub\\Java\\GuliMall\\gulimall-parent\\images\\huawei.png");
//        ossClient.putObject("slienceme", "huawei.png", inputStream);
//        ossClient.shutdown();
//        System.out.println("上传成功");
//    }
//}
