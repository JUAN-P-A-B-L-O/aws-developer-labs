package com.example.awsdeveloperlabs.controller;

import com.example.awsdeveloperlabs.aws.S3UploadService;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/aws")
public class S3UploadController {

    private final S3UploadService uploadService;

    public S3UploadController(S3UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        return uploadService.upload(file);
    }
}
