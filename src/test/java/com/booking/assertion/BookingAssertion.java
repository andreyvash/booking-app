package com.booking.assertion;

import com.booking.dto.BookingResponse;
import com.booking.model.BookingStatus;
import org.assertj.core.api.Assertions;

import java.time.LocalDate;
import java.util.UUID;

public class BookingAssertion {

    private final BookingResponse response;

    private BookingAssertion(BookingResponse response) {
        this.response = response;
    }

    public static BookingAssertion assertThat(BookingResponse response) {
        return new BookingAssertion(response);
    }

    public BookingAssertion hasId() {
        Assertions.assertThat(response.getId()).isNotNull();
        return this;
    }

    public BookingAssertion hasId(UUID id) {
        Assertions.assertThat(response.getId()).isEqualTo(id);
        return this;
    }

    public BookingAssertion hasPropertyId(UUID propertyId) {
        Assertions.assertThat(response.getPropertyId()).isEqualTo(propertyId);
        return this;
    }

    public BookingAssertion hasGuestId() {
        Assertions.assertThat(response.getGuestId()).isNotNull();
        return this;
    }

    public BookingAssertion hasGuestId(UUID guestId) {
        Assertions.assertThat(response.getGuestId()).isEqualTo(guestId);
        return this;
    }

    public BookingAssertion hasStartDate(LocalDate startDate) {
        Assertions.assertThat(response.getStartDate()).isEqualTo(startDate);
        return this;
    }

    public BookingAssertion hasEndDate(LocalDate endDate) {
        Assertions.assertThat(response.getEndDate()).isEqualTo(endDate);
        return this;
    }

    public BookingAssertion hasStatus(BookingStatus status) {
        Assertions.assertThat(response.getStatus()).isEqualTo(status);
        return this;
    }

    public BookingAssertion isConfirmed() {
        return hasStatus(BookingStatus.CONFIRMED);
    }

    public BookingAssertion isCanceled() {
        return hasStatus(BookingStatus.CANCELED);
    }
}
