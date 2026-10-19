package com.example.awsdeveloperlabs.lambda.cep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

public class CepHandler implements RequestHandler<CepRequest, CepResponse> {
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public CepHandler() {
        this(DynamoDbClient.create(), System.getenv("DYNAMODB_TABLE_NAME"));
    }

    CepHandler(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public CepResponse handleRequest(CepRequest event, Context context) {
        if (event == null || event.cep() == null || !event.cep().matches("[0-9]{8}")) {
            return CepResponse.error("INVALID_CEP", "CEP deve conter exatamente 8 dígitos numéricos.");
        }

        String cep = event.cep();
        String formattedCep = cep.substring(0, 5) + "-" + cep.substring(5);
        if (tableName == null || tableName.isBlank()) {
            logError(context, "DYNAMODB_TABLE_NAME não configurada.");
            return CepResponse.error("CONFIGURATION_ERROR", "Serviço temporariamente indisponível.");
        }

        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(Map.of(
                            "cep", AttributeValue.builder().s(cep).build(),
                            "formattedCep", AttributeValue.builder().s(formattedCep).build(),
                            "processedBy", AttributeValue.builder().s("aws-lambda").build()))
                    .build());
        } catch (SdkException exception) {
            logError(context, "Falha no DynamoDB PutItem: " + exception.getClass().getSimpleName());
            return CepResponse.error("DYNAMODB_ERROR", "Não foi possível salvar o CEP. Tente novamente.");
        }
        return new CepResponse(cep, formattedCep, "aws-lambda", null, null);
    }

    private static void logError(Context context, String message) {
        if (context != null && context.getLogger() != null) {
            context.getLogger().log(message);
        }
    }
}
