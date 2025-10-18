package com.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockUpdateRequest {

    @NotNull(message = "Owner ID is required")
    private UUID ownerId;

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
