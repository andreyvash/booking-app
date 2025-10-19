package com.booking.integrationTests.booking;

import com.booking.integrationTests.BaseIntegrationTest;
import com.booking.model.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Delete Booking Integration Tests")
class DeleteBookingIT extends BaseIntegrationTest {

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
    @DisplayName("Should delete booking successfully")
    void shouldDeleteBookingSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/bookings/{bookingId}", testBooking.getId()))
                .andExpect(status().isNoContent());

        Optional<Booking> deletedBooking = bookingRepository.findById(testBooking.getId());
        Assertions.assertThat(deletedBooking).isEmpty();
    }

    @Test
    @DisplayName("Should fail when booking does not exist")
    void shouldFailWhenBookingDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/bookings/{bookingId}", java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Booking not found")));
    }

    @Test
    @DisplayName("Should delete confirmed booking")
    void shouldDeleteConfirmedBooking() throws Exception {
        testBooking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(testBooking);

        mockMvc.perform(delete("/api/bookings/{bookingId}", testBooking.getId()))
                .andExpect(status().isNoContent());

        Optional<Booking> deletedBooking = bookingRepository.findById(testBooking.getId());
        Assertions.assertThat(deletedBooking).isEmpty();
    }

    @Test
    @DisplayName("Should delete cancelled booking")
    void shouldDeleteCancelledBooking() throws Exception {
        testBooking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(testBooking);

        mockMvc.perform(delete("/api/bookings/{bookingId}", testBooking.getId()))
                .andExpect(status().isNoContent());

        Optional<Booking> deletedBooking = bookingRepository.findById(testBooking.getId());
        Assertions.assertThat(deletedBooking).isEmpty();
    }

    @Test
    @DisplayName("Should remove booking from database permanently")
    void shouldRemoveBookingFromDatabasePermanently() throws Exception {
        long countBefore = bookingRepository.count();

        mockMvc.perform(delete("/api/bookings/{bookingId}", testBooking.getId()))
                .andExpect(status().isNoContent());

        long countAfter = bookingRepository.count();
        Assertions.assertThat(countAfter).isEqualTo(countBefore - 1);
    }

    @Test
    @DisplayName("Should not affect other bookings when deleting one")
    void shouldNotAffectOtherBookingsWhenDeletingOne() throws Exception {
        Booking anotherBooking = createBooking(
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(20),
                BookingStatus.CONFIRMED
        );

        mockMvc.perform(delete("/api/bookings/{bookingId}", testBooking.getId()))
                .andExpect(status().isNoContent());

        Optional<Booking> remainingBooking = bookingRepository.findById(anotherBooking.getId());
        Assertions.assertThat(remainingBooking).isPresent();
    }

    @Test
    @DisplayName("Should return 404 when trying to delete already deleted booking")
    void shouldReturn404WhenTryingToDeleteAlreadyDeletedBooking() throws Exception {
        mockMvc.perform(delete("/api/bookings/{bookingId}", testBooking.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/bookings/{bookingId}", testBooking.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Booking not found")));
    }

    @Test
    @DisplayName("Should not delete guest when deleting booking")
    void shouldNotDeleteGuestWhenDeletingBooking() throws Exception {
        mockMvc.perform(delete("/api/bookings/{bookingId}", testBooking.getId()))
                .andExpect(status().isNoContent());

        Optional<Guest> guest = guestRepository.findById(testGuest.getId());
        Assertions.assertThat(guest).isPresent();
    }

    @Test
    @DisplayName("Should not delete property when deleting booking")
    void shouldNotDeletePropertyWhenDeletingBooking() throws Exception {
        mockMvc.perform(delete("/api/bookings/{bookingId}", testBooking.getId()))
                .andExpect(status().isNoContent());

        Optional<Property> property = propertyRepository.findById(testProperty.getId());
        Assertions.assertThat(property).isPresent();
    }
}
