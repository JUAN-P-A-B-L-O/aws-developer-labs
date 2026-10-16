package com.example.awsdeveloperlabs.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

class S3DownloadServiceTests {

    @Test
    void signsDownloadForConfiguredBucketAndExactKeyForFiveMinutes() throws Exception {
        S3Presigner presigner = mock(S3Presigner.class);
        PresignedGetObjectRequest signedRequest = mock(PresignedGetObjectRequest.class);
        String url = "https://example.com/file?signature=local-test";
        when(signedRequest.url()).thenReturn(URI.create(url).toURL());
        try (var factory = mockStatic(S3Presigner.class)) {
            factory.when(S3Presigner::create).thenReturn(presigner);
            when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenAnswer(invocation -> {
                GetObjectPresignRequest request = invocation.getArgument(0);
                assertEquals(Duration.ofMinutes(5), request.signatureDuration());
                assertEquals("test-bucket", request.getObjectRequest().bucket());
                assertEquals("folder/report + á.pdf", request.getObjectRequest().key());
                return signedRequest;
            });

            assertEquals(url, new S3DownloadService("test-bucket")
                    .createDownloadUrl("folder/report + á.pdf"));
            verify(presigner).close();
        }
    }

    @Test
    void rejectsMissingOrBlankKeyBeforeCreatingPresigner() {
        try (var factory = mockStatic(S3Presigner.class)) {
            for (String key : new String[] {null, "", "   "}) {
                var exception = assertThrows(ResponseStatusException.class,
                        () -> new S3DownloadService("test-bucket").createDownloadUrl(key));
                assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            }
            factory.verifyNoInteractions();
        }
    }

    @Test
    void rejectsMissingBucketBeforeCreatingPresigner() {
        try (var factory = mockStatic(S3Presigner.class)) {
            var exception = assertThrows(ResponseStatusException.class,
                    () -> new S3DownloadService("").createDownloadUrl("file.txt"));
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
            factory.verifyNoInteractions();
        }
    }
}
