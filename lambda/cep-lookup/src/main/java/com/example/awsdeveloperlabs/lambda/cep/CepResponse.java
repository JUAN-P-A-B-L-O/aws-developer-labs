package com.example.awsdeveloperlabs.lambda.cep;

public record CepResponse(
        String cep,
        String formattedCep,
        String processedBy,
        String errorCode,
        String message) {

    static CepResponse error(String code, String message) {
        return new CepResponse(null, null, "aws-lambda", code, message);
    }
}
