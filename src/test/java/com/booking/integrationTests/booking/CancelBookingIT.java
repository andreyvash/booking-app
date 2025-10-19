package com.booking.integrationTests.booking;

import com.booking.assertion.BookingAssertion;
import com.booking.dto.BookingResponse;
import com.booking.integrationTests.BaseIntegrationTest;
import com.booking.model.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Cancel Booking Integration Tests")
class CancelBookingIT extends BaseIntegrationTest {

    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testBooking = createBooking(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                BookingStatus.CONFIRMED
        );
    }

    @Test
    @DisplayName("Should cancel booking successfully")
    void shouldCancelBookingSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}/cancel", testBooking.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(response)
                .hasId(testBooking.getId())
                .hasPropertyId(testProperty.getId())
                .hasGuestId(testGuest.getId())
                .hasStartDate(testBooking.getStartDate())
                .hasEndDate(testBooking.getEndDate())
                .isCanceled();
    }

    @Test
    @DisplayName("Should fail when booking does not exist")
    void shouldFailWhenBookingDoesNotExist() throws Exception {
        mockMvc.perform(patch("/api/bookings/{bookingId}/cancel", java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Booking not found")));
    }

    @Test
    @DisplayName("Should fail when booking is already canceled")
    void shouldFailWhenBookingIsAlreadyCanceled() throws Exception {
        testBooking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(testBooking);

        mockMvc.perform(patch("/api/bookings/{bookingId}/cancel", testBooking.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already cancelled")));
    }

    @Test
    @DisplayName("Should persist canceled status in database")
    void shouldPersistCanceledStatusInDatabase() throws Exception {
        mockMvc.perform(patch("/api/bookings/{bookingId}/cancel", testBooking.getId()))
                .andExpect(status().isOk());

        Booking updatedBooking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        Assertions.assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    @DisplayName("Should not modify booking dates when canceling")
    void shouldNotModifyBookingDatesWhenCanceling() throws Exception {
        LocalDate originalStartDate = testBooking.getStartDate();
        LocalDate originalEndDate = testBooking.getEndDate();

        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}/cancel", testBooking.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(response)
                .hasStartDate(originalStartDate)
                .hasEndDate(originalEndDate);
    }

    @Test
    @DisplayName("Should not modify property or guest when canceling")
    void shouldNotModifyPropertyOrGuestWhenCanceling() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}/cancel", testBooking.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(response)
                .hasPropertyId(testProperty.getId())
                .hasGuestId(testGuest.getId());
    }
}
