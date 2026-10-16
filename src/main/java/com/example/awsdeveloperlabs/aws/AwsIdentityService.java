package com.example.awsdeveloperlabs.aws;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sts.StsClient;

@Service
public class AwsIdentityService {

    public String getCurrentIdentityArn() {
        try (StsClient stsClient = StsClient.create()) {
            return stsClient.getCallerIdentity().arn();
        }
    }
}
