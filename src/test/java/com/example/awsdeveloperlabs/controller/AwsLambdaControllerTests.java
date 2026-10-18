package com.example.awsdeveloperlabs.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.awsdeveloperlabs.aws.AwsLambdaService;
import com.example.awsdeveloperlabs.aws.CepResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.lambda.LambdaClient;

class AwsLambdaControllerTests {

    @Test
    void returnsLambdaResponseAsJson() throws Exception {
        var service = mock(AwsLambdaService.class);
        when(service.lookupCep("39100000"))
                .thenReturn(new CepResponse("39100000", "39100-000", "aws-lambda"));
        var mvc = MockMvcBuilders.standaloneSetup(new AwsLambdaController(service)).build();

        mvc.perform(get("/api/aws/cep/39100000"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().json("""
                        {"cep":"39100000","formattedCep":"39100-000","processedBy":"aws-lambda"}
                        """, true));
        verify(service).lookupCep("39100000");
    }

    @Test
    void returns400ForInvalidCepWithoutCallingAws() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(
                new AwsLambdaController(new AwsLambdaService(new ObjectMapper()))).build();
        try (var factory = mockStatic(LambdaClient.class)) {
            for (String cep : new String[] {"3910000", "391000000", "39100-000", "abcdefgh"}) {
                mvc.perform(get("/api/aws/cep/{cep}", cep)).andExpect(status().isBadRequest());
            }
            factory.verifyNoInteractions();
        }
    }

    @Test
    void returns502ForLambdaFailure() throws Exception {
        var service = mock(AwsLambdaService.class);
        when(service.lookupCep("39100000"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "CEP Lambda invocation failed"));
        var mvc = MockMvcBuilders.standaloneSetup(new AwsLambdaController(service)).build();

        mvc.perform(get("/api/aws/cep/39100000")).andExpect(status().isBadGateway());
    }
}
