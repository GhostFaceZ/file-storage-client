package com.fs;

import lombok.Builder;
import lombok.Data;

/**
 * 对象存储初始化参数
 *
 * @Author runze.shi
 * @Date 2022-06-20
 */
@Data
@Builder
public class FileStorageInitProperties {
    // 对象存储类型
    private String storageType;

    // 对象存储终端地址
    private String endpoint;

    // 通行证
    private String accessKey;

    // 密钥
    private String secretKey;

    // 地区
    private String region;

    // 操作的桶名称
    private String bucketName;
}
