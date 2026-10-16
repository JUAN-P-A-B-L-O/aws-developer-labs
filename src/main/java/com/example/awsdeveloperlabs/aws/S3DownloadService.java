package com.example.awsdeveloperlabs.aws;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3DownloadService {

    private final String bucketName;

    public S3DownloadService(@Value("${aws.s3.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
    }

    public String createDownloadUrl(String key) {
        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Key must not be blank");
        }
        if (bucketName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Download bucket is not configured");
        }

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(objectRequest)
                .build();

        try (S3Presigner presigner = S3Presigner.create()) {
            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }
}
