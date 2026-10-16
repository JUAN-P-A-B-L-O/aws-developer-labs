package com.example.awsdeveloperlabs.controller;

import com.example.awsdeveloperlabs.aws.S3DownloadService;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aws")
public class S3DownloadController {

    private final S3DownloadService downloadService;

    public S3DownloadController(S3DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping(value = "/download-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> downloadUrl(@RequestParam("key") String key) {
        return Map.of("url", downloadService.createDownloadUrl(key));
    }
}
