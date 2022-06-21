package com.fs;

import cn.hutool.core.lang.Assert;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/**
 * 对象存储构造器
 *
 * @Author runze.shi
 * @Date 2022-06-20
 */
public class FileStorageBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(FileStorageBuilder.class);

    // 为避免频繁初始化，在内存中保留构建结果
    private static Map<String, FileStorageClient> fileStorageClientMap = new ConcurrentHashMap<>();

    /**
     * 构造对象存储客户端
     *
     * @param properties
     * @return
     */
    public static FileStorageClient buildFileStorageClient(FileStorageInitProperties properties) throws Exception {
        // 校验参数
        Assert.isTrue(StringUtils.isNotBlank(properties.getEndpoint()), "File storage param [endpoint] can't be blank.");
        Assert.isTrue(StringUtils.isNotBlank(properties.getAccessKey()), "File storage param [accessKey] can't be blank.");
        Assert.isTrue(StringUtils.isNotBlank(properties.getSecretKey()), "File storage param [secretKey] can't be blank.");
        Assert.isTrue(StringUtils.isNotBlank(properties.getRegion()), "File storage param [region] can't be blank.");

        // 校验连接地址格式
        URI endpointUri = null;
        try {
            endpointUri = new URI(properties.getEndpoint());
        } catch (URISyntaxException e) {
            LOG.error("File storage param [endpoint] invalid.", e);
            throw e;
        }

        // 初始化过相应Bucket的FileStorageClient直接返回
        FileStorageClient fileStorageClient = fileStorageClientMap.get(properties.getBucketName());
        if (fileStorageClient != null) {
            LOG.info("Bucket [{}] had init file storage client, just return.", properties.getBucketName());
            return fileStorageClient;
        }

        // 构建S3 Client
        S3Client s3Client = S3Client.builder()
                .endpointOverride(endpointUri)
                .credentialsProvider(() -> AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()))
                .region(Region.of(properties.getRegion()))
                .build();

        // 构建FileStorageClient
        fileStorageClient = new FileStorageClient(properties, s3Client);
        fileStorageClientMap.putIfAbsent(properties.getBucketName(), fileStorageClient);

        // 检查Bucket，如不存在则初始化Bucket
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.getBucketName()).build());
        } catch (NoSuchBucketException e) {
            LOG.info("Bucket [{}] not exist, create bucket.", properties.getBucketName());
            CreateBucketResponse createBucketResponse = s3Client
                    .createBucket(CreateBucketRequest.builder().bucket(properties.getBucketName()).build());
            LOG.info("Bucket create success, response:{}", createBucketResponse.toString());
        }

        return fileStorageClient;
    }
}
