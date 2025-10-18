package com.booking.validator;

import com.booking.dto.BookingRequest;
import com.booking.exception.BookingException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.Booking;
import com.booking.model.BookingStatus;
import com.booking.repository.BlockRepository;
import com.booking.repository.BookingRepository;
import com.booking.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final BlockRepository blockRepository;

    public void validateBookingRequest(BookingRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        validatePropertyExists(request.getPropertyId());
        validatePropertyNotBooked(request.getPropertyId(), request.getStartDate(), request.getEndDate());
        validatePropertyNotBlocked(request.getPropertyId(), request.getStartDate(), request.getEndDate());
    }

    public void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BookingException("Start date must be before end date");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new BookingException("Start date cannot be in the past");
        }
    }

    public void validatePropertyExists(UUID propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found with id: " + propertyId);
        }
    }

    public void validatePropertyNotBlocked(UUID propertyId, LocalDate startDate, LocalDate endDate) {
        var overlappingBlocks = blockRepository.findOverlappingBlocks(propertyId, startDate, endDate);

        if (!overlappingBlocks.isEmpty()) {
            throw new BookingException("Property is blocked for the selected dates");
        }
    }

    public void validatePropertyNotBooked(UUID propertyId, LocalDate startDate, LocalDate endDate) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                propertyId,
                startDate,
                endDate
        );

        if (!overlappingBookings.isEmpty()) {
            throw new BookingException("Property is already booked for the selected dates");
        }
    }

    public void validateNoOverlappingBookingsForUpdate(UUID propertyId, LocalDate startDate, LocalDate endDate, UUID excludeBookingId) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(propertyId, startDate, endDate)
                .stream()
                .filter(b -> !b.getId().equals(excludeBookingId))
                .toList();

        if (!overlappingBookings.isEmpty()) {
            throw new BookingException("Property is already booked for the selected dates");
        }
    }

    public void validateBookingNotCanceled(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new BookingException("Cannot update a cancelled booking. Please rebook it first.");
        }
    }
}
