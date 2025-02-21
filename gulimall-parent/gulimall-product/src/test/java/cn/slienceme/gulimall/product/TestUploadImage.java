//package cn.slienceme.gulimall.product;
//
//import com.aliyun.oss.ClientBuilderConfiguration;
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClientBuilder;
//import com.aliyun.oss.OSSException;
//import com.aliyun.oss.common.auth.CredentialsProviderFactory;
//import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
//import com.aliyun.oss.common.comm.SignVersion;
//import com.aliyun.oss.model.PutObjectRequest;
//import com.aliyun.oss.model.PutObjectResult;
//import com.aliyuncs.exceptions.ClientException;
//
//import java.io.File;
//import org.joda.time.DateTime;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//import java.util.UUID;
//
//public class TestUploadImage {
//
//    public static void main(String[] args) throws ClientException, FileNotFoundException {
//        // 工具类获取值
//        String endpoint = "https://oss-cn-beijing.aliyuncs.com";
//        String bucketName = "slienceme";
//        String region = "cn-beijing";
//        // RAM用户的访问密钥（AccessKey ID和AccessKey Secret）。
//
//        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
//
//        String fileName = UUID.randomUUID().toString().replaceAll("-","") + ".png";
//        String filePath= "D:\\codeHub\\Java\\GuliMall\\gulimall-parent\\images\\huawei.png";
//
//        InputStream inputStream = new FileInputStream(filePath);
//
//        // 创建OSSClient实例。
//        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
//        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
//        OSS ossClient = OSSClientBuilder.create()
//                .endpoint(endpoint)
//                .credentialsProvider(credentialsProvider)
//                .clientConfiguration(clientBuilderConfiguration)
//                .region(region)
//                .build();
//
//        try {
//            String datePath = new DateTime().toString("yyyy/MM/dd");
//            fileName = datePath + "/" + fileName;
//            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);
//            PutObjectResult result = ossClient.putObject(putObjectRequest);
//            System.out.println(result);
//            String url = "https://oss.slienceme.cn/" + fileName;
//            System.out.println(url);
//
//        } catch (OSSException oe) {
//            System.out.println("Caught an OSSException, which means your request made it to OSS, "
//                    + "but was rejected with an error response for some reason.");
//            System.out.println("Error Message:" + oe.getErrorMessage());
//            System.out.println("Error Code:" + oe.getErrorCode());
//            System.out.println("Request ID:" + oe.getRequestId());
//            System.out.println("Host ID:" + oe.getHostId());
//        } finally {
//            if (ossClient != null) {
//                ossClient.shutdown();
//            }
//        }
//    }
//}
