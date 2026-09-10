package com.nye.backend.alert.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectAlertRequest(
        @NotBlank(message = "Un motif de rejet est obligatoire") String reason
) {
}
