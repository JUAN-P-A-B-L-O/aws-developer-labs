package com.example.awsdeveloperlabs.lambda.cep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class CepHandler implements RequestHandler<CepRequest, CepResponse> {
    @Override
    public CepResponse handleRequest(CepRequest event, Context context) {
        if (event == null || event.cep() == null || !event.cep().matches("[0-9]{8}")) {
            return CepResponse.error("INVALID_CEP", "CEP deve conter exatamente 8 dígitos numéricos.");
        }

        String cep = event.cep();
        String formattedCep = cep.substring(0, 5) + "-" + cep.substring(5);
        return new CepResponse(cep, formattedCep, "aws-lambda", null, null);
    }
}
