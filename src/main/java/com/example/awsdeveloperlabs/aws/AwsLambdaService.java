package com.example.awsdeveloperlabs.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

@Service
public class AwsLambdaService {

    private static final Logger log = LoggerFactory.getLogger(AwsLambdaService.class);
    private final ObjectMapper objectMapper;

    public AwsLambdaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CepResponse lookupCep(String cep) {
        if (cep == null || !cep.matches("[0-9]{8}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP must contain exactly 8 digits");
        }

        try (LambdaClient client = LambdaClient.create()) {
            var request = InvokeRequest.builder()
                    .functionName("aws-labs-hello")
                    .invocationType(InvocationType.REQUEST_RESPONSE)
                    .payload(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(new CepRequest(cep))))
                    .build();
            var result = client.invoke(request);
            if (!Integer.valueOf(200).equals(result.statusCode()) || result.functionError() != null
                    || result.payload() == null) {
                throw new IllegalStateException("Lambda invocation returned an unsuccessful response");
            }

            var response = objectMapper.readValue(result.payload().asUtf8String(), CepResponse.class);
            if (response == null || response.cep() == null || response.formattedCep() == null
                    || response.processedBy() == null) {
                throw new IllegalStateException("Lambda response is missing required fields");
            }
            return response;
        } catch (SdkException | JsonProcessingException | IllegalStateException exception) {
            // Do not log SDK messages, payloads or credentials; preserve the cause for diagnostics.
            log.error("CEP Lambda invocation failed (exception type: {})", exception.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "CEP Lambda invocation failed", exception);
        }
    }
}
