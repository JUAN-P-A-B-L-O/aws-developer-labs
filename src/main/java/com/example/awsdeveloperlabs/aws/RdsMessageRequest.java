package com.example.awsdeveloperlabs.aws;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RdsMessageRequest(@NotBlank @Size(max = 100) String message) {
}
