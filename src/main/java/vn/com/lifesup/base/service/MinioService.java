package vn.com.lifesup.base.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.lifesup.base.config.properties.MinioProperties;
import vn.com.lifesup.base.exception.UncheckBusinessException;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /** Đảm bảo bucket tồn tại **/
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucketName()).build()
            );
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucketName()).build());
                log.info("Created bucket '{}'", properties.getBucketName());
            }
        } catch (Exception e) {
            log.error("Error checking/creating bucket: {}", e.getMessage(), e);
        }
    }

    /** Upload file **/
    public String uploadFile(MultipartFile file, String objectName) {
        ensureBucketExists();
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("Uploaded file: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Upload error: {}", e.getMessage(), e);
            throw new UncheckBusinessException("Upload failed", e);
        }
    }

    /** Download file **/
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Download error: {}", e.getMessage(), e);
            throw new UncheckBusinessException("Download failed", e);
        }
    }

    /** Xóa file **/
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("🗑️ Deleted object: {}", objectName);
        } catch (Exception e) {
            log.error("Delete error: {}", e.getMessage(), e);
        }
    }

    /** Tạo pre-signed URL (link tạm thời download) **/
    public String getPresignedUrl(String objectName, int durationSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(durationSeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Presigned URL error: {}", e.getMessage(), e);
            throw new UncheckBusinessException("Generate URL failed", e);
        }
    }
}

