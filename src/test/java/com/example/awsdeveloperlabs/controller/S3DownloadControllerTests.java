package com.example.awsdeveloperlabs.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.awsdeveloperlabs.aws.S3DownloadService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class S3DownloadControllerTests {

    @Test
    void returnsOnlyTheGeneratedUrl() throws Exception {
        var service = mock(S3DownloadService.class);
        when(service.createDownloadUrl("folder/file.txt"))
                .thenReturn("https://example.com/file?signature=local-test");
        var mvc = MockMvcBuilders.standaloneSetup(new S3DownloadController(service)).build();

        mvc.perform(get("/api/aws/download-url").param("key", "folder/file.txt"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().json("""
                        {"url":"https://example.com/file?signature=local-test"}
                        """, true));
        verify(service).createDownloadUrl("folder/file.txt");
    }

    @Test
    void rejectsMissingOrBlankKey() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(
                new S3DownloadController(new S3DownloadService("test-bucket"))).build();

        mvc.perform(get("/api/aws/download-url")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/aws/download-url").param("key", ""))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/aws/download-url").param("key", "   "))
                .andExpect(status().isBadRequest());
    }
}
