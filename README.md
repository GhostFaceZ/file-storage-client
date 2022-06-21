# file-storage-client
#对接云端对象存储工具包
    支持基于S3协议的对象存储服务（如：阿里云OSS、华为云OBS、自建MinIO）
##一、变量准备
    1 对象存储访问地址 endpoint
    2 对象存储所在区域 region
    3 对象存储访问 access key
    4 对象存储访问 secret key

##二、使用说明
###1 引入依赖
        <dependency>
            <groupId>com.fs</groupId>
            <artifactId>file-storage-client</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

###2 构建FileStorageClient
        // 构建初始化所需参数
        FileStorageInitProperties initProperties = FileStorageInitProperties.builder()
                .accessKey("9LPDCUJV08DA8AHDIQJC") // 访问AccessKey
                .secretKey("KIHSIBHOyRS1aedH3UYHtY0A8cvahrIeJvpsgPuZ") // 访问SecretKey
                .endpoint("http://obs.cn-east-3.myhuaweicloud.com") // 对象存储服务地址
                .region("cn-east-3") // 对象存储所在区域
                .bucketName("runze-fs") // 使用的BucketName
                .build();
                
        // 构建FileStorageClient
        FileStorageClient fileStorageClient = FileStorageBuilder.buildFileStorageClient(initProperties);

###3 使用FileStorageClient操作示例
        FileStorageClient fileStorageClient = FileStorageBuilder.buildFileStorageClient(initProperties);
        
        // 将本地文件上传到对象存储中
        InputStream inputStream = new FileInputStream(new File("/User/Admin/File/test.jpg"));
        boolean putObjectResult = fileStorageClient.putObject("/demo/test.jpg", inputStream);

###4 注意事项
        1 首次构建 FileStorageClient 会自动初始化 Bukect，自动初始化的 Bucket 为[私读私写]
        2 操作 Object 时需要提供 ObjectKey，由于S3 SDK约束，当 ObjectKey 以'/'开头，工具会自动删除开头的'/'
