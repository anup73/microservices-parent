package com.agent.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateInventoryRequest(
        @NotNull(message = "Available quantity is required")
        Integer quantityAvailable,

        @NotNull(message = "Reserved quantity is required")
        Integer quantityReserved
) {
}
