package com.agent.dto;

public record CustomerAddressResponse(
        Long addressId,
        String addressType,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        String phone,
        Boolean isDefault
) {
}
