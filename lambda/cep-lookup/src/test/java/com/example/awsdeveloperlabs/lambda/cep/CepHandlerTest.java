package com.example.awsdeveloperlabs.lambda.cep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CepHandlerTest {
    private final CepHandler handler = new CepHandler();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "3910000", "391000000", "39100-000", "39100A00", " 39100000", "39100000\n", "３９１０００００"})
    void rejectsInvalidCep(String cep) {
        CepResponse result = handler.handleRequest(new CepRequest(cep), null);

        assertEquals(new CepResponse(null, null, "aws-lambda", "INVALID_CEP",
                "CEP deve conter exatamente 8 dígitos numéricos."), result);
    }

    @Test
    void rejectsNullEvent() {
        assertEquals(new CepResponse(null, null, "aws-lambda", "INVALID_CEP",
                "CEP deve conter exatamente 8 dígitos numéricos."), handler.handleRequest(null, null));
    }

    @ParameterizedTest
    @CsvSource({"39100000,39100-000", "01001000,01001-000", "00000000,00000-000", "99999999,99999-999"})
    void formatsCepLocallyAndPreservesLeadingZeros(String cep, String expected) {
        assertEquals(new CepResponse(cep, expected, "aws-lambda", null, null),
                handler.handleRequest(new CepRequest(cep), null));
    }
}
