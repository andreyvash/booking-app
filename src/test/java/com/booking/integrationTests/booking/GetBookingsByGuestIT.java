package com.booking.integrationTests.booking;

import com.booking.assertion.BookingAssertion;
import com.booking.dto.BookingResponse;
import com.booking.integrationTests.BaseIntegrationTest;
import com.booking.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Get Bookings By Guest Tests")
class GetBookingsByGuestIT extends BaseIntegrationTest {

    private Booking confirmedBooking;
    private Booking canceledBooking;
    private Guest anotherGuest;
    private Booking anotherGuestBooking;

    @BeforeEach
    void setUp() {
        confirmedBooking = createBooking(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                BookingStatus.CONFIRMED
        );

        canceledBooking = createBooking(
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(20),
                BookingStatus.CANCELED
        );

        anotherGuest = new Guest();
        anotherGuest.setFirstName("Another");
        anotherGuest.setLastName("Guest");
        anotherGuest.setEmail("another.guest@example.com");
        anotherGuest = guestRepository.save(anotherGuest);

        anotherGuestBooking = new Booking();
        anotherGuestBooking.setPropertyId(testProperty.getId());
        anotherGuestBooking.setGuestId(anotherGuest.getId());
        anotherGuestBooking.setStartDate(LocalDate.now().plusDays(25));
        anotherGuestBooking.setEndDate(LocalDate.now().plusDays(30));
        anotherGuestBooking.setStatus(BookingStatus.CONFIRMED);
        anotherGuestBooking = bookingRepository.save(anotherGuestBooking);
    }

    @Test
    @DisplayName("Should get all bookings for a guest")
    void shouldGetAllBookingsForGuest() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bookings/guest/{guestId}", testGuest.getId()))
                .andExpect(status().isOk())
                .andReturn();

        List<BookingResponse> responses = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responses).hasSize(2);
        
        responses.forEach(response -> 
            BookingAssertion.assertThat(response)
                    .hasGuestId(testGuest.getId())
                    .hasGuestFirstName(testGuest.getFirstName())
                    .hasGuestLastName(testGuest.getLastName())
        );
    }

    @Test
    @DisplayName("Should return bookings with correct guest information")
    void shouldReturnBookingsWithCorrectGuestInformation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bookings/guest/{guestId}", testGuest.getId()))
                .andExpect(status().isOk())
                .andReturn();

        List<BookingResponse> responses = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        BookingResponse confirmedResponse = responses.stream()
                .filter(r -> r.getId().equals(confirmedBooking.getId()))
                .findFirst()
                .orElseThrow();

        BookingAssertion.assertThat(confirmedResponse)
                .hasId(confirmedBooking.getId())
                .hasPropertyId(testProperty.getId())
                .hasGuestId(testGuest.getId())
                .hasGuestFirstName(testGuest.getFirstName())
                .hasGuestLastName(testGuest.getLastName())
                .hasStartDate(confirmedBooking.getStartDate())
                .hasEndDate(confirmedBooking.getEndDate())
                .isConfirmed();
    }

    @Test
    @DisplayName("Should include both confirmed and canceled bookings")
    void shouldIncludeBothConfirmedAndCanceledBookings() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bookings/guest/{guestId}", testGuest.getId()))
                .andExpect(status().isOk())
                .andReturn();

        List<BookingResponse> responses = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responses).hasSize(2);
        
        BookingResponse confirmedResponse = responses.stream()
                .filter(r -> r.getId().equals(confirmedBooking.getId()))
                .findFirst()
                .orElseThrow();
        BookingAssertion.assertThat(confirmedResponse).isConfirmed();
        
        BookingResponse canceledResponse = responses.stream()
                .filter(r -> r.getId().equals(canceledBooking.getId()))
                .findFirst()
                .orElseThrow();
        BookingAssertion.assertThat(canceledResponse).isCanceled();
    }

    @Test
    @DisplayName("Should return empty list when guest has no bookings")
    void shouldReturnEmptyListWhenGuestHasNoBookings() throws Exception {
        Guest guestWithNoBookings = new Guest();
        guestWithNoBookings.setFirstName("No");
        guestWithNoBookings.setLastName("Bookings");
        guestWithNoBookings.setEmail("no.bookings@example.com");
        guestWithNoBookings = guestRepository.save(guestWithNoBookings);

        MvcResult result = mockMvc.perform(get("/api/bookings/guest/{guestId}", guestWithNoBookings.getId()))
                .andExpect(status().isOk())
                .andReturn();

        List<BookingResponse> responses = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should fail when guest does not exist")
    void shouldFailWhenGuestDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/bookings/guest/{guestId}", java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Guest not found")));
    }

    @Test
    @DisplayName("Should not return bookings from other guests")
    void shouldNotReturnBookingsFromOtherGuests() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bookings/guest/{guestId}", testGuest.getId()))
                .andExpect(status().isOk())
                .andReturn();

        List<BookingResponse> responses = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responses).hasSize(2);
        responses.forEach(response -> 
            BookingAssertion.assertThat(response)
                    .hasGuestId(testGuest.getId())
        );
    }

    @Test
    @DisplayName("Should return bookings ordered by creation")
    void shouldReturnBookingsOrderedByCreation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/bookings/guest/{guestId}", testGuest.getId()))
                .andExpect(status().isOk())
                .andReturn();

        List<BookingResponse> responses = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responses).hasSize(2);
        BookingAssertion.assertThat(responses.get(0)).hasId(confirmedBooking.getId());
        BookingAssertion.assertThat(responses.get(1)).hasId(canceledBooking.getId());
    }
}
