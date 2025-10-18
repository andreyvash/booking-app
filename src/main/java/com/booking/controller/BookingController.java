package com.booking.controller;

import com.booking.dto.BookingRequest;
import com.booking.dto.BookingResponse;
import com.booking.dto.BookingUpdateRequest;
import com.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("Received request to create booking for property: {}", request.getPropertyId());
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable UUID bookingId) {
        log.info("Received request to get booking with id: {}", bookingId);
        BookingResponse response = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByPropertyId(@PathVariable UUID propertyId) {
        log.info("Received request to get bookings for property id: {}", propertyId);
        List<BookingResponse> responses = bookingService.getBookingsByPropertyId(propertyId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingUpdateRequest request) {
        log.info("Received request to update booking with id: {}", bookingId);
        BookingResponse response = bookingService.updateBooking(bookingId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID bookingId) {
        log.info("Received request to cancel booking with id: {}", bookingId);
        BookingResponse response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{bookingId}/rebook")
    public ResponseEntity<BookingResponse> rebookCancelledBooking(@PathVariable UUID bookingId) {
        log.info("Received request to rebook cancelled booking with id: {}", bookingId);
        BookingResponse response = bookingService.rebookCancelledBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID bookingId) {
        log.info("Received request to delete booking with id: {}", bookingId);
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
