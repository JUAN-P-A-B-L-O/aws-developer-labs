package com.example.awsdeveloperlabs.controller;

import com.example.awsdeveloperlabs.aws.AwsIdentityService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aws")
public class AwsIdentityController {

    private final AwsIdentityService awsIdentityService;

    public AwsIdentityController(AwsIdentityService awsIdentityService) {
        this.awsIdentityService = awsIdentityService;
    }

    @GetMapping(value = "/identity", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getCurrentIdentityArn() {
        return awsIdentityService.getCurrentIdentityArn();
    }
}
