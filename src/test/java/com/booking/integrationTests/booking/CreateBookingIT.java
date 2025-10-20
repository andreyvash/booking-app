package com.booking.integrationTests.booking;

import com.booking.assertion.BookingAssertion;
import com.booking.dto.BookingRequest;
import com.booking.dto.BookingResponse;
import com.booking.integrationTests.BaseIntegrationTest;
import com.booking.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Create Booking Integration Tests")
class CreateBookingIT extends BaseIntegrationTest {

    @Test
    @DisplayName("Should create booking successfully with valid data")
    void shouldCreateBookingSuccessfully() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setPropertyId(testProperty.getId());
        request.setGuestEmail("new.guest@example.com");
        request.setGuestFirstName("New");
        request.setGuestLastName("Guest");
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        MvcResult createResult = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        BookingResponse createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                BookingResponse.class
        );

        MvcResult getResult = mockMvc.perform(get("/api/bookings/{bookingId}", createResponse.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse getResponse = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(getResponse)
                .hasId(createResponse.getId())
                .hasPropertyId(testProperty.getId())
                .hasGuestId()
                .hasGuestFirstName("New")
                .hasGuestLastName("Guest")
                .hasStartDate(request.getStartDate())
                .hasEndDate(request.getEndDate())
                .isConfirmed();
    }

    @Test
    @DisplayName("Should create booking with existing guest email")
    void shouldCreateBookingWithExistingGuest() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setPropertyId(testProperty.getId());
        request.setGuestEmail(testGuest.getEmail());
        request.setGuestFirstName("Updated");
        request.setGuestLastName("Name");
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        MvcResult createResult = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        BookingResponse createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                BookingResponse.class
        );

        MvcResult getResult = mockMvc.perform(get("/api/bookings/{bookingId}", createResponse.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse getResponse = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(getResponse)
                .hasGuestId(testGuest.getId())
                .hasGuestFirstName(testGuest.getFirstName())
                .hasGuestLastName(testGuest.getLastName());
    }

    @Test
    @DisplayName("Should fail when property does not exist")
    void shouldFailWhenPropertyDoesNotExist() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setPropertyId(UUID.randomUUID());
        request.setGuestEmail("test@example.com");
        request.setGuestFirstName("Test");
        request.setGuestLastName("Guest");
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Property not found")));
    }

    @Test
    @DisplayName("Should fail when start date is after end date")
    void shouldFailWhenStartDateIsAfterEndDate() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setPropertyId(testProperty.getId());
        request.setGuestEmail("test@example.com");
        request.setGuestFirstName("Test");
        request.setGuestLastName("Guest");
        request.setStartDate(LocalDate.now().plusDays(10));
        request.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date must be before end date")));
    }

    @Test
    @DisplayName("Should fail when start date is in the past")
    void shouldFailWhenStartDateIsInThePast() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setPropertyId(testProperty.getId());
        request.setGuestEmail("test@example.com");
        request.setGuestFirstName("Test");
        request.setGuestLastName("Guest");
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date cannot be in the past")));
    }

    @Test
    @DisplayName("Should fail when booking overlaps with existing booking")
    void shouldFailWhenBookingOverlapsWithExistingBooking() throws Exception {
        Booking existingBooking = new Booking();
        existingBooking.setPropertyId(testProperty.getId());
        existingBooking.setGuestId(testGuest.getId());
        existingBooking.setStartDate(LocalDate.now().plusDays(5));
        existingBooking.setEndDate(LocalDate.now().plusDays(10));
        existingBooking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(existingBooking);

        BookingRequest request = new BookingRequest();
        request.setPropertyId(testProperty.getId());
        request.setGuestEmail("another@example.com");
        request.setGuestFirstName("Another");
        request.setGuestLastName("Guest");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(12));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is already booked")));
    }

    @Test
    @DisplayName("Should fail when booking overlaps with property block")
    void shouldFailWhenBookingOverlapsWithBlock() throws Exception {
        createBlock(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "Maintenance"
        );

        BookingRequest request = new BookingRequest();
        request.setPropertyId(testProperty.getId());
        request.setGuestEmail("test@example.com");
        request.setGuestFirstName("Test");
        request.setGuestLastName("Guest");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(12));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is blocked")));
    }

    @Test
    @DisplayName("Should fail when required fields are missing")
    void shouldFailWhenRequiredFieldsAreMissing() throws Exception {
        BookingRequest request = new BookingRequest();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should not create booking when dates are adjacent to existing booking")
    void shouldCreateBookingWhenDatesAreAdjacent() throws Exception {
        createBooking(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                BookingStatus.CONFIRMED
        );

        BookingRequest request = new BookingRequest();
        request.setPropertyId(testProperty.getId());
        request.setGuestEmail("adjacent@example.com");
        request.setGuestFirstName("Adjacent");
        request.setGuestLastName("Guest");
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    @DisplayName("Should create booking when canceled booking exists for same dates")
    void shouldCreateBookingWhenCanceledBookingExists() throws Exception {
        createBooking(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                BookingStatus.CANCELED
        );

        BookingRequest request = new BookingRequest();
        request.setPropertyId(testProperty.getId());
        request.setGuestEmail("new@example.com");
        request.setGuestFirstName("New");
        request.setGuestLastName("Guest");
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(10));

        MvcResult createResult = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        BookingResponse createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                BookingResponse.class
        );

        MvcResult getResult = mockMvc.perform(get("/api/bookings/{bookingId}", createResponse.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BookingResponse getResponse = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                BookingResponse.class
        );

        BookingAssertion.assertThat(getResponse)
                .hasId(createResponse.getId())
                .hasGuestFirstName("New")
                .hasGuestLastName("Guest")
                .isConfirmed();
    }
}
