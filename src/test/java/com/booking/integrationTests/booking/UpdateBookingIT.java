package com.booking.integrationTests.booking;

import com.booking.assertion.BookingAssertion;
import com.booking.dto.BookingResponse;
import com.booking.dto.BookingUpdateRequest;
import com.booking.integrationTests.BaseIntegrationTest;
import com.booking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Update Booking Integration Tests")
class UpdateBookingIT extends BaseIntegrationTest {

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
    @DisplayName("Should update booking dates successfully")
    void shouldUpdateBookingDatesSuccessfully() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStartDate(LocalDate.now().plusDays(15));
        request.setEndDate(LocalDate.now().plusDays(20));

        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
                .hasGuestFirstName(testGuest.getFirstName())
                .hasGuestLastName(testGuest.getLastName())
                .hasStartDate(request.getStartDate())
                .hasEndDate(request.getEndDate())
                .isConfirmed();
    }

    @Test
    @DisplayName("Should update guest information successfully")
    void shouldUpdateGuestInformationSuccessfully() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setGuestEmail("updated.guest@example.com");
        request.setGuestFirstName("Updated");
        request.setGuestLastName("Guest");

        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(response)
                .hasId(testBooking.getId())
                .hasGuestFirstName("Updated")
                .hasGuestLastName("Guest")
                .hasStartDate(testBooking.getStartDate())
                .hasEndDate(testBooking.getEndDate());
    }

    @Test
    @DisplayName("Should update both dates and guest information")
    void shouldUpdateBothDatesAndGuestInformation() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStartDate(LocalDate.now().plusDays(15));
        request.setEndDate(LocalDate.now().plusDays(20));
        request.setGuestEmail("new.guest@example.com");
        request.setGuestFirstName("New");
        request.setGuestLastName("Guest");

        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(response)
                .hasId(testBooking.getId())
                .hasGuestFirstName("New")
                .hasGuestLastName("Guest")
                .hasStartDate(request.getStartDate())
                .hasEndDate(request.getEndDate());
    }

    @Test
    @DisplayName("Should fail when booking does not exist")
    void shouldFailWhenBookingDoesNotExist() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStartDate(LocalDate.now().plusDays(15));
        request.setEndDate(LocalDate.now().plusDays(20));

        mockMvc.perform(patch("/api/bookings/{bookingId}", java.util.UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Booking not found")));
    }

    @Test
    @DisplayName("Should fail when start date is after end date")
    void shouldFailWhenStartDateIsAfterEndDate() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStartDate(LocalDate.now().plusDays(20));
        request.setEndDate(LocalDate.now().plusDays(15));

        mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date must be before end date")));
    }

    @Test
    @DisplayName("Should fail when start date is in the past")
    void shouldFailWhenStartDateIsInThePast() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date cannot be in the past")));
    }

    @Test
    @DisplayName("Should fail when new dates overlap with existing booking")
    void shouldFailWhenNewDatesOverlapWithExistingBooking() throws Exception {
        createBooking(
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(20),
                BookingStatus.CONFIRMED
        );

        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStartDate(LocalDate.now().plusDays(17));
        request.setEndDate(LocalDate.now().plusDays(22));

        mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is already booked")));
    }

    @Test
    @DisplayName("Should fail when new dates overlap with property block")
    void shouldFailWhenNewDatesOverlapWithBlock() throws Exception {
        createBlock(
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(20),
                "Maintenance"
        );

        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStartDate(LocalDate.now().plusDays(17));
        request.setEndDate(LocalDate.now().plusDays(22));

        mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is blocked")));
    }

    @Test
    @DisplayName("Should fail when booking is canceled")
    void shouldFailWhenBookingIsCanceled() throws Exception {
        testBooking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(testBooking);

        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStartDate(LocalDate.now().plusDays(15));
        request.setEndDate(LocalDate.now().plusDays(20));

        mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Cannot update a cancelled booking")));
    }

    @Test
    @DisplayName("Should update only start date when end date is not provided")
    void shouldUpdateOnlyStartDateWhenEndDateIsNotProvided() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStartDate(LocalDate.now().plusDays(6));

        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(response)
                .hasGuestFirstName(testGuest.getFirstName())
                .hasGuestLastName(testGuest.getLastName())
                .hasStartDate(request.getStartDate())
                .hasEndDate(testBooking.getEndDate());
    }

    @Test
    @DisplayName("Should update only end date when start date is not provided")
    void shouldUpdateOnlyEndDateWhenStartDateIsNotProvided() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setEndDate(LocalDate.now().plusDays(12));

        MvcResult result = mockMvc.perform(patch("/api/bookings/{bookingId}", testBooking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(response)
                .hasGuestFirstName(testGuest.getFirstName())
                .hasGuestLastName(testGuest.getLastName())
                .hasStartDate(testBooking.getStartDate())
                .hasEndDate(request.getEndDate());
    }
}
