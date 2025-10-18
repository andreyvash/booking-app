package com.booking.dto;

import com.booking.model.Block;
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
public class BlockResponse {

    private UUID id;
    private UUID propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

    public static BlockResponse fromModel(Block block) {
        return BlockResponse.builder()
                .id(block.getId())
                .propertyId(block.getPropertyId())
                .startDate(block.getStartDate())
                .endDate(block.getEndDate())
                .reason(block.getReason())
                .build();
    }
}
