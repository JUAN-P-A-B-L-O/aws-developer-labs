package com.example.awsdeveloperlabs.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class S3UploadServiceTests {

    @Test
    void uploadsContentWithSafeUniqueKeyAndNoAcl() throws Exception {
        S3Client client = mock(S3Client.class);
        try (var factory = mockStatic(S3Client.class)) {
            factory.when(S3Client::create).thenReturn(client);
            when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenAnswer(invocation -> {
                        PutObjectRequest request = invocation.getArgument(0);
                        RequestBody body = invocation.getArgument(1);
                        assertEquals("test-bucket", request.bucket());
                        assertNull(request.acl());
                        assertNull(request.grantRead());
                        assertEquals("hello", new String(body.contentStreamProvider().newStream().readAllBytes(),
                                StandardCharsets.UTF_8));
                        return PutObjectResponse.builder().build();
                    });
            var file = new MockMultipartFile("file", "C:\\fakepath\\../../hello ?.txt", "text/plain",
                    "hello".getBytes(StandardCharsets.UTF_8));
            var service = new S3UploadService("test-bucket");

            String key = service.upload(file);
            assertTrue(key.matches("[0-9a-f-]{36}-hello__\\.txt"));
            assertNotEquals(key, service.upload(file));
            verify(client, times(2)).close();
        }
    }

    @Test
    void rejectsEmptyFileBeforeContactingAws() {
        try (var factory = mockStatic(S3Client.class)) {
            var exception = assertThrows(ResponseStatusException.class,
                    () -> new S3UploadService("test-bucket").upload(new MockMultipartFile("file", new byte[0])));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            factory.verifyNoInteractions();
        }
    }

    @Test
    void rejectsMissingBucketBeforeContactingAws() {
        try (var factory = mockStatic(S3Client.class)) {
            var exception = assertThrows(ResponseStatusException.class,
                    () -> new S3UploadService("").upload(new MockMultipartFile("file", new byte[] {1})));
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
            factory.verifyNoInteractions();
        }
    }
}
