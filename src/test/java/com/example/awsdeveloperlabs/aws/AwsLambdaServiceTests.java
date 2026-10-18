package com.example.awsdeveloperlabs.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

class AwsLambdaServiceTests {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AwsLambdaService service = new AwsLambdaService(mapper);

    @Test
    void invokesExpectedFunctionSynchronouslyAndMapsResponse() throws Exception {
        var client = mock(LambdaClient.class);
        try (var factory = mockStatic(LambdaClient.class)) {
            factory.when(LambdaClient::create).thenReturn(client);
            when(client.invoke(any(InvokeRequest.class))).thenAnswer(invocation -> {
                InvokeRequest request = invocation.getArgument(0);
                assertEquals("aws-labs-hello", request.functionName());
                assertEquals(InvocationType.REQUEST_RESPONSE, request.invocationType());
                assertEquals(mapper.readTree("{\"cep\":\"39100000\"}"),
                        mapper.readTree(request.payload().asUtf8String()));
                return response("""
                        {"cep":"39100000","formattedCep":"39100-000","processedBy":"aws-lambda"}
                        """);
            });

            assertEquals(new CepResponse("39100000", "39100-000", "aws-lambda"),
                    service.lookupCep("39100000"));
            verify(client).close();
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"3910000", "391000000", "39100-000", "abcdefgh", " 39100000", "３９１０００００"})
    void rejectsInvalidCepBeforeCreatingClient(String cep) {
        try (var factory = mockStatic(LambdaClient.class)) {
            var error = assertThrows(ResponseStatusException.class, () -> service.lookupCep(cep));
            assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
            factory.verifyNoInteractions();
        }
    }

    @Test
    void translatesSdkFailureAndPreservesCause() {
        var client = mock(LambdaClient.class);
        var cause = SdkClientException.create("Invocation failed");
        try (var factory = mockStatic(LambdaClient.class)) {
            factory.when(LambdaClient::create).thenReturn(client);
            when(client.invoke(any(InvokeRequest.class))).thenThrow(cause);

            var error = assertThrows(ResponseStatusException.class, () -> service.lookupCep("39100000"));
            assertEquals(HttpStatus.BAD_GATEWAY, error.getStatusCode());
            assertSame(cause, error.getCause());
            verify(client).close();
        }
    }

    @Test
    void translatesCredentialOrRegionResolutionFailure() {
        var cause = SdkClientException.create("Client creation failed");
        try (var factory = mockStatic(LambdaClient.class)) {
            factory.when(LambdaClient::create).thenThrow(cause);
            var error = assertThrows(ResponseStatusException.class, () -> service.lookupCep("39100000"));
            assertEquals(HttpStatus.BAD_GATEWAY, error.getStatusCode());
            assertSame(cause, error.getCause());
        }
    }

    @Test
    void rejectsFunctionErrorEvenWhenInvokeStatusIs200() {
        assertBadGateway(InvokeResponse.builder().statusCode(200).functionError("Unhandled")
                .payload(SdkBytes.fromUtf8String("{\"errorMessage\":\"Function failed\"}")).build());
    }

    @Test
    void rejectsUnexpectedInvokeStatusAndAbsentPayload() {
        assertBadGateway(InvokeResponse.builder().statusCode(202).build());
        assertBadGateway(InvokeResponse.builder().statusCode(200).build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-json", "", "null", "{}", "{\"cep\":\"39100000\"}"})
    void rejectsMalformedOrIncompleteResponse(String payload) {
        assertBadGateway(response(payload));
    }

    private InvokeResponse response(String payload) {
        return InvokeResponse.builder().statusCode(200).payload(SdkBytes.fromUtf8String(payload)).build();
    }

    private void assertBadGateway(InvokeResponse response) {
        var client = mock(LambdaClient.class);
        try (var factory = mockStatic(LambdaClient.class)) {
            factory.when(LambdaClient::create).thenReturn(client);
            when(client.invoke(any(InvokeRequest.class))).thenReturn(response);
            var error = assertThrows(ResponseStatusException.class, () -> service.lookupCep("39100000"));
            assertEquals(HttpStatus.BAD_GATEWAY, error.getStatusCode());
            assertNotNull(error.getCause());
            verify(client).close();
        }
    }
}
