package com.example.awsdeveloperlabs.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3UploadService {

    private final String bucketName;

    public S3UploadService(@Value("${aws.s3.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
    }

    public String upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }
        if (bucketName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Upload bucket is not configured");
        }

        String filename = file.getOriginalFilename();
        filename = filename == null ? "file" : filename.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1)
                .replaceAll("[^A-Za-z0-9._-]", "_");
        if (filename.isBlank() || filename.equals(".") || filename.equals("..")) {
            filename = "file";
        }
        filename = filename.substring(0, Math.min(filename.length(), 150));
        String key = UUID.randomUUID() + "-" + filename;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/octet-stream")
                .build();

        try (S3Client client = S3Client.create(); InputStream input = file.getInputStream()) {
            client.putObject(request, RequestBody.fromInputStream(input, file.getSize()));
        }
        return key;
    }
}
