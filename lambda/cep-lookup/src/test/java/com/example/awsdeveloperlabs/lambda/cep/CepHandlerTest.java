package com.example.awsdeveloperlabs.lambda.cep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CepHandlerTest {
    private final StubDynamoDbClient client = new StubDynamoDbClient();
    private final CepHandler handler = new CepHandler(client, "aws-labs-cep");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "3910000", "391000000", "39100-000", "39100A00", " 39100000", "39100000\n", "３９１０００００"})
    void rejectsInvalidCep(String cep) {
        CepResponse result = handler.handleRequest(new CepRequest(cep), null);
        assertEquals(CepResponse.error("INVALID_CEP", "CEP deve conter exatamente 8 dígitos numéricos."), result);
        assertTrue(client.requests.isEmpty());
    }

    @Test
    void rejectsNullEvent() {
        assertEquals(CepResponse.error("INVALID_CEP", "CEP deve conter exatamente 8 dígitos numéricos."),
                handler.handleRequest(null, null));
        assertTrue(client.requests.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"39100002,39100-002", "39100000,39100-000", "01001000,01001-000", "00000000,00000-000", "99999999,99999-999"})
    void savesFormattedCepAndPreservesLeadingZeros(String cep, String expected) {
        assertEquals(new CepResponse(cep, expected, "aws-lambda", null, null),
                handler.handleRequest(new CepRequest(cep), null));
        assertEquals(1, client.requests.size());
        PutItemRequest request = client.requests.getFirst();
        assertEquals("aws-labs-cep", request.tableName());
        assertEquals(Map.of(
                "cep", AttributeValue.builder().s(cep).build(),
                "formattedCep", AttributeValue.builder().s(expected).build(),
                "processedBy", AttributeValue.builder().s("aws-lambda").build()), request.item());
    }

    @Test
    void reusesClientAcrossInvocations() {
        handler.handleRequest(new CepRequest("39100002"), null);
        handler.handleRequest(new CepRequest("01001000"), null);
        assertEquals(2, client.requests.size());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rejectsMissingTableConfiguration(String tableName) {
        CepResponse result = new CepHandler(client, tableName)
                .handleRequest(new CepRequest("39100002"), null);
        assertEquals(CepResponse.error("CONFIGURATION_ERROR", "Serviço temporariamente indisponível."), result);
        assertTrue(client.requests.isEmpty());
    }

    @Test
    void usesConfiguredTableName() {
        new CepHandler(client, "another-table").handleRequest(new CepRequest("39100002"), null);
        assertEquals("another-table", client.requests.getFirst().tableName());
    }

    @Test
    void handlesAndLogsServiceFailureWithoutSensitiveDetails() {
        client.failure = DynamoDbException.builder().message("sensitive-service-details").statusCode(403).build();
        StringBuilder logs = new StringBuilder();
        LambdaLogger logger = new LambdaLogger() {
            @Override
            public void log(String message) {
                logs.append(message);
            }

            @Override
            public void log(byte[] message) {
                logs.append(new String(message, StandardCharsets.UTF_8));
            }
        };
        Context context = (Context) Proxy.newProxyInstance(Context.class.getClassLoader(),
                new Class<?>[]{Context.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getLogger")) {
                        return logger;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        CepResponse result = handler.handleRequest(new CepRequest("39100002"), context);
        assertEquals(CepResponse.error("DYNAMODB_ERROR", "Não foi possível salvar o CEP. Tente novamente."), result);
        assertTrue(logs.toString().contains("DynamoDbException"));
        assertFalse(logs.toString().contains("sensitive-service-details"));
        assertFalse(result.toString().contains("sensitive-service-details"));
    }

    @Test
    void handlesClientFailureWithoutContext() {
        client.failure = SdkClientException.create("sensitive-client-details");
        assertEquals(CepResponse.error("DYNAMODB_ERROR", "Não foi possível salvar o CEP. Tente novamente."),
                handler.handleRequest(new CepRequest("39100002"), null));
    }

    private static final class StubDynamoDbClient implements DynamoDbClient {
        private final List<PutItemRequest> requests = new ArrayList<>();
        private RuntimeException failure;

        @Override
        public PutItemResponse putItem(PutItemRequest request) {
            requests.add(request);
            if (failure != null) {
                throw failure;
            }
            return PutItemResponse.builder().build();
        }

        @Override
        public String serviceName() {
            return "dynamodb";
        }

        @Override
        public void close() {
        }
    }
}
