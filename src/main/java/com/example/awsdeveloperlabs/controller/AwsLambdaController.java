package com.example.awsdeveloperlabs.controller;

import com.example.awsdeveloperlabs.aws.AwsLambdaService;
import com.example.awsdeveloperlabs.aws.CepResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aws")
public class AwsLambdaController {

    private final AwsLambdaService lambdaService;

    public AwsLambdaController(AwsLambdaService lambdaService) {
        this.lambdaService = lambdaService;
    }

    @GetMapping(value = "/cep/{cep}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CepResponse lookupCep(@PathVariable("cep") String cep) {
        return lambdaService.lookupCep(cep);
    }
}
