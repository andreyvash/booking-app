package com.booking.integrationTests.booking;

import com.booking.assertion.BookingAssertion;
import com.booking.dto.BookingResponse;
import com.booking.integrationTests.BaseIntegrationTest;
import com.booking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Rebook Booking Integration Tests")
class RebookBookingIT extends BaseIntegrationTest {

    private Booking cancelledBooking;

    @BeforeEach
    void setUp() {
        cancelledBooking = createBooking(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                BookingStatus.CANCELED
        );
    }

    @Test
    @DisplayName("Should rebook cancelled booking successfully")
    void shouldRebookCancelledBookingSuccessfully() throws Exception {
        MvcResult rebookResult = mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", cancelledBooking.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse rebookResponse = objectMapper.readValue(
                rebookResult.getResponse().getContentAsString(),
                BookingResponse.class
        );

        MvcResult getResult = mockMvc.perform(get("/api/bookings/{bookingId}", cancelledBooking.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse getResponse = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(getResponse)
                .hasId(cancelledBooking.getId())
                .hasPropertyId(testProperty.getId())
                .hasGuestId(testGuest.getId())
                .hasStartDate(cancelledBooking.getStartDate())
                .hasEndDate(cancelledBooking.getEndDate())
                .isConfirmed();
    }

    @Test
    @DisplayName("Should fail when booking does not exist")
    void shouldFailWhenBookingDoesNotExist() throws Exception {
        mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Booking not found")));
    }

    @Test
    @DisplayName("Should fail when booking is not cancelled")
    void shouldFailWhenBookingIsNotCancelled() throws Exception {
        cancelledBooking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(cancelledBooking);

        mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", cancelledBooking.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Only cancelled bookings can be rebooked")));
    }

    @Test
    @DisplayName("Should fail when dates are in the past")
    void shouldFailWhenDatesAreInThePast() throws Exception {
        cancelledBooking.setStartDate(LocalDate.now().minusDays(10));
        cancelledBooking.setEndDate(LocalDate.now().minusDays(5));
        bookingRepository.save(cancelledBooking);

        mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", cancelledBooking.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date cannot be in the past")));
    }

    @Test
    @DisplayName("Should fail when property is already booked for same dates")
    void shouldFailWhenPropertyIsAlreadyBooked() throws Exception {
        createBooking(
                cancelledBooking.getStartDate(),
                cancelledBooking.getEndDate(),
                BookingStatus.CONFIRMED
        );

        mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", cancelledBooking.getId()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is already booked")));
    }

    @Test
    @DisplayName("Should fail when property is blocked for same dates")
    void shouldFailWhenPropertyIsBlocked() throws Exception {
        createBlock(
                cancelledBooking.getStartDate(),
                cancelledBooking.getEndDate(),
                "Maintenance"
        );

        mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", cancelledBooking.getId()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is blocked")));
    }

    @Test
    @DisplayName("Should persist confirmed status in database")
    void shouldPersistConfirmedStatusInDatabase() throws Exception {
        mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", cancelledBooking.getId()))
                .andExpect(status().isOk());

        Booking rebookedBooking = bookingRepository.findById(cancelledBooking.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(rebookedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should not modify booking dates when rebooking")
    void shouldNotModifyBookingDatesWhenRebooking() throws Exception {
        LocalDate originalStartDate = cancelledBooking.getStartDate();
        LocalDate originalEndDate = cancelledBooking.getEndDate();

        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", cancelledBooking.getId()))
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
    @DisplayName("Should not modify property or guest when rebooking")
    void shouldNotModifyPropertyOrGuestWhenRebooking() throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", cancelledBooking.getId()))
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

    @Test
    @DisplayName("Should allow rebooking after cancelling another booking on same dates")
    void shouldAllowRebookingAfterCancellingAnotherBooking() throws Exception {
        Booking anotherBooking = new Booking();
        anotherBooking.setPropertyId(testProperty.getId());
        anotherBooking.setGuestId(testGuest.getId());
        anotherBooking.setStartDate(cancelledBooking.getStartDate());
        anotherBooking.setEndDate(cancelledBooking.getEndDate());
        anotherBooking.setStatus(BookingStatus.CONFIRMED);
        anotherBooking = bookingRepository.save(anotherBooking);

        mockMvc.perform(patch("/api/bookings/{bookingId}/cancel", anotherBooking.getId()))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}/rebook", cancelledBooking.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(response)
                .hasId(cancelledBooking.getId())
                .isConfirmed();
    }
}
