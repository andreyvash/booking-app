package com.booking.service;

import com.booking.dto.BookingRequest;
import com.booking.dto.BookingResponse;
import com.booking.dto.BookingUpdateRequest;
import com.booking.exception.BookingException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.Booking;
import com.booking.model.BookingStatus;
import com.booking.repository.BookingRepository;
import com.booking.validator.BookingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final GuestService guestService;
    private final BookingValidator bookingValidator;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for property: {} and guest: {}", request.getPropertyId(), request.getGuestEmail());

        bookingValidator.validateBookingRequest(request);

        Booking booking = createBookingReservation(request);
        log.info("Booking created successfully with id: {}", booking.getId());

        return BookingResponse.fromModel(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID bookingId) {
        log.info("Fetching booking with id: {}", bookingId);
        Booking booking = getBookingOrThrow(bookingId);
        return BookingResponse.fromModel(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByPropertyId(UUID propertyId) {
        log.info("Fetching bookings for property id: {}", propertyId);
        
        bookingValidator.validatePropertyExists(propertyId);
        
        List<Booking> bookings = bookingRepository.findByPropertyId(propertyId);
        return bookings.stream()
                .map(BookingResponse::fromModel)
                .toList();
    }

    @Transactional
    public BookingResponse updateBooking(UUID bookingId, BookingUpdateRequest request) {
        log.info("Updating booking with id: {}", bookingId);

        Booking booking = getBookingOrThrow(bookingId);
        bookingValidator.validateBookingNotCanceled(booking);

        if (hasDatesUpdate(request)) {
            updateBookingDates(booking, request, bookingId);
        }

        if (hasGuestUpdate(request)) {
            var newGuestId = guestService.updateBookingGuest(booking.getGuestId(), request);
            booking.setGuestId(newGuestId);
        }

        booking = bookingRepository.save(booking);
        log.info("Booking updated successfully with id: {}", booking.getId());

        return BookingResponse.fromModel(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        log.info("Cancelling booking with id: {}", bookingId);

        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new BookingException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELED);
        booking = bookingRepository.save(booking);
        log.info("Booking cancelled successfully with id: {}", booking.getId());

        return BookingResponse.fromModel(booking);
    }

    @Transactional
    public BookingResponse rebookCancelledBooking(UUID bookingId) {
        log.info("Rebooking cancelled booking with id: {}", bookingId);

        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.CANCELED) {
            throw new BookingException("Only cancelled bookings can be rebooked");
        }

        bookingValidator.validateDates(booking.getStartDate(), booking.getEndDate());
        bookingValidator.validatePropertyNotBooked(booking.getPropertyId(), booking.getStartDate(), booking.getEndDate());
        bookingValidator.validatePropertyNotBlocked(booking.getPropertyId(), booking.getStartDate(), booking.getEndDate());

        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);
        log.info("Booking rebooked successfully with id: {}", booking.getId());

        return BookingResponse.fromModel(booking);
    }

    @Transactional
    public void deleteBooking(UUID bookingId) {
        log.info("Deleting booking with id: {}", bookingId);

        Booking booking = getBookingOrThrow(bookingId);

        bookingRepository.delete(booking);
        log.info("Booking deleted successfully with id: {}", bookingId);
    }

    private Booking createBookingReservation(BookingRequest request) {
        Booking booking = Booking.builder()
                .propertyId(request.getPropertyId())
                .guestId(guestService.getOrCreateGuest(request).getId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking);
        return booking;
    }

    private boolean hasDatesUpdate(BookingUpdateRequest request) {
        return request.getStartDate() != null || request.getEndDate() != null;
    }

    private boolean hasGuestUpdate(BookingUpdateRequest request) {
        return request.getGuestEmail() != null 
                || request.getGuestFirstName() != null 
                || request.getGuestLastName() != null;
    }

    private void updateBookingDates(Booking booking, BookingUpdateRequest request, UUID bookingId) {
        LocalDate newStartDate = request.getStartDate() != null ? request.getStartDate() : booking.getStartDate();
        LocalDate newEndDate = request.getEndDate() != null ? request.getEndDate() : booking.getEndDate();

        bookingValidator.validateDates(newStartDate, newEndDate);
        bookingValidator.validateNoOverlappingBookingsForUpdate(booking.getPropertyId(), newStartDate, newEndDate, bookingId);
        bookingValidator.validatePropertyNotBlocked(booking.getPropertyId(), newStartDate, newEndDate);

        booking.setStartDate(newStartDate);
        booking.setEndDate(newEndDate);
    }

    private Booking getBookingOrThrow(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }
}
