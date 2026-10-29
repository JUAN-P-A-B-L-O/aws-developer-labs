package com.example.awsdeveloperlabs.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.awsdeveloperlabs.aws.RdsMessage;
import com.example.awsdeveloperlabs.aws.RdsMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RdsMessageControllerTests {

    @Test
    void returnsMessagesAsJson() throws Exception {
        var service = mock(RdsMessageService.class);
        when(service.getMessages()).thenReturn(List.of(new RdsMessage(1, "RDS funcionando")));
        var mvc = MockMvcBuilders.standaloneSetup(new RdsMessageController(service)).build();

        mvc.perform(get("/api/aws/rds/messages"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":1,"message":"RDS funcionando"}]
                        """, true));
    }

    @Test
    void createsMessageAndReturns201() throws Exception {
        var service = mock(RdsMessageService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new RdsMessageController(service)).build();

        mvc.perform(post("/api/aws/rds/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Spring conectado ao RDS"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
        verify(service).createMessage("Spring conectado ao RDS");
    }

    @Test
    void rejectsMissingBlankAndOversizedMessagesBeforeWriting() throws Exception {
        var service = mock(RdsMessageService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new RdsMessageController(service)).build();
        var mapper = new ObjectMapper();

        for (String body : List.of("{}", "{\"message\":null}",
                mapper.writeValueAsString(Map.of("message", "   ")),
                mapper.writeValueAsString(Map.of("message", "x".repeat(101))))) {
            mvc.perform(post("/api/aws/rds/messages")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }
}
