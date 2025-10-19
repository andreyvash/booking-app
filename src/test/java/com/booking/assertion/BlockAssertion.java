package com.booking.assertion;

import com.booking.dto.BlockResponse;
import org.assertj.core.api.Assertions;

import java.time.LocalDate;
import java.util.UUID;

public class BlockAssertion {

    private final BlockResponse response;

    private BlockAssertion(BlockResponse response) {
        this.response = response;
    }

    public static BlockAssertion assertThat(BlockResponse response) {
        return new BlockAssertion(response);
    }

    public BlockAssertion hasId() {
        Assertions.assertThat(response.getId()).isNotNull();
        return this;
    }

    public BlockAssertion hasId(UUID id) {
        Assertions.assertThat(response.getId()).isEqualTo(id);
        return this;
    }

    public BlockAssertion hasPropertyId(UUID propertyId) {
        Assertions.assertThat(response.getPropertyId()).isEqualTo(propertyId);
        return this;
    }

    public BlockAssertion hasStartDate(LocalDate startDate) {
        Assertions.assertThat(response.getStartDate()).isEqualTo(startDate);
        return this;
    }

    public BlockAssertion hasEndDate(LocalDate endDate) {
        Assertions.assertThat(response.getEndDate()).isEqualTo(endDate);
        return this;
    }

    public BlockAssertion hasReason(String reason) {
        Assertions.assertThat(response.getReason()).isEqualTo(reason);
        return this;
    }

    public BlockAssertion hasNoReason() {
        Assertions.assertThat(response.getReason()).isNull();
        return this;
    }
}
