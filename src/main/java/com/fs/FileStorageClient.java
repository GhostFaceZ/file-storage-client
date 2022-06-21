package com.fs;

import cn.hutool.core.lang.Assert;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * 对象存储操作客户端
 *
 * @Author runze.shi
 * @Date 2022-06-20
 */
@Data
@AllArgsConstructor
public class FileStorageClient {
    private static final Logger LOG = LoggerFactory.getLogger(FileStorageClient.class);

    // Object临时URL保留时长，单位: 小时
    private static final long OBJECT_SIGNATURE_DURATION = 1;

    private FileStorageInitProperties initProperties;

    private S3Client s3Client;

    /**
     * 判断Bucket是否存在
     *
     * @param bucketName
     * @return
     */
    public boolean doesBucketExist(String bucketName) {
        // 检查Bucket，如不存在则初始化Bucket
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            LOG.info("Bucket [{}] not exist.", bucketName);
            return false;
        }

        return true;
    }

    /**
     * 判断Object是否存在
     *
     * @param objectKey
     * @return
     */
    public boolean doesObjectExist(String objectKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(initProperties.getBucketName())
                    .key(convertObjectKey(objectKey))
                    .build());
        } catch (NoSuchKeyException e) {
            LOG.error("Object key [{}] not exist.", objectKey, e);
            return false;
        }

        return true;
    }

    /**
     * 上传Object文件
     *
     * @param objectKey
     * @return
     */
    public boolean putObject(String objectKey, InputStream inputStream) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(initProperties.getBucketName())
                            .key(convertObjectKey(objectKey))
                            .build(),
                    RequestBody.fromInputStream(inputStream, inputStream.available()));
        } catch (NoSuchKeyException | IOException e) {
            LOG.error("Put object key [{}] error.", objectKey, e);
            return false;
        }

        return true;
    }

    /**
     * 下载Object文件
     *
     * @param objectKey
     * @return
     */
    public URL generateObjectTmpUrl(String objectKey) {
        try {
            S3Presigner s3Presigner = S3Presigner.builder()
                    .endpointOverride(new URI(initProperties.getEndpoint()))
                    .credentialsProvider(() -> AwsBasicCredentials.create(initProperties.getAccessKey(), initProperties.getSecretKey()))
                    .region(Region.of(initProperties.getRegion()))
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(initProperties.getBucketName())
                    .key(convertObjectKey(objectKey))
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .getObjectRequest(getObjectRequest)
                            .signatureDuration(Duration.ofHours(OBJECT_SIGNATURE_DURATION))
                            .build());
            return presignedGetObjectRequest.url();
        } catch (Exception e) {
            LOG.error("Generate object temporary url error, key:{}.", objectKey, e);
            return null;
        }
    }

    /**
     * 获取Object文件访问地址, 注:限支持公共读的Bucket
     *
     * @param objectKey
     * @return
     */
    public URL getObjectUrl(String objectKey) {
        try {
            URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(initProperties.getBucketName())
                    .key(convertObjectKey(objectKey))
                    .build());
            return url;
        } catch (Exception e) {
            LOG.error("Get object error, key:{}.", objectKey, e);
            return null;
        }
    }

    /**
     * 删除Object
     *
     * @param objectKey
     * @return
     */
    public boolean deleteObject(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(initProperties.getBucketName())
                    .key(convertObjectKey(objectKey))
                    .build());
            return true;
        } catch (Exception e) {
            LOG.error("Delete object error, key:{}.", objectKey, e);
            return false;
        }
    }

    /**
     * 去除ObjectKey开头的'/'
     */
    private String convertObjectKey(String objectKey) {
        Assert.notNull(objectKey, "Object key can't be null.");

        return objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
    }
}
