package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Autowired
    OSS ossClient;

    @Value("${spring.cloud.alicloud.access-key}")
    String accessKeyId;
    @Value("${spring.cloud.alicloud.secret-key}")
    String accessKeySecret;
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    String endpoint;




    @Test
    public void testUpload() throws FileNotFoundException {
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "gulimall-lzhuopeng";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "uyqvwwwvzpf2.jpg";
        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        String filePath= "D:\\picture\\uyqvwwwvzpf.jpg";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = new FileInputStream(filePath);
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, inputStream);
            System.out.println("上传成功");
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    @Test
    public void testUploa2() throws FileNotFoundException {
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        String filePath= "D:\\picture\\rh2o1mweeon.jpg";
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "gulimall-lzhuopeng";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "rh2o1mweeon1.jpg";
        InputStream inputStream = new FileInputStream(filePath);
        ossClient.putObject(bucketName, objectName, inputStream);
        System.out.println("上传成功");
    }



}
