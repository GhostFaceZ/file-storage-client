package com.fs;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

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
}
