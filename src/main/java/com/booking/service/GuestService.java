package com.booking.service;

import com.booking.dto.BookingRequest;
import com.booking.dto.BookingUpdateRequest;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.Guest;
import com.booking.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestService {

    private final GuestRepository guestRepository;

    @Transactional
    public Guest getOrCreateGuest(BookingRequest request) {
        Optional<Guest> existingGuest = guestRepository.findByEmail(request.getGuestEmail());
        if (existingGuest.isPresent()) {
            return existingGuest.get();
        }

        Guest guest = Guest.builder()
                .email(request.getGuestEmail())
                .firstName(request.getGuestFirstName())
                .lastName(request.getGuestLastName())
                .build();

        guest = guestRepository.save(guest);
        log.info("Guest created successfully with id: {}", guest.getId());

        return guest;
    }

    @Transactional
    public java.util.UUID updateBookingGuest(java.util.UUID currentGuestId, BookingUpdateRequest request) {
        Guest guest = getGuestOrThrow(currentGuestId);

        if (request.getGuestEmail() != null) {
            Guest updatedGuest = handleGuestEmailUpdate(guest, request.getGuestEmail());
            if (!updatedGuest.getId().equals(guest.getId())) {
                // Switched to existing guest, return the new guest ID
                return updatedGuest.getId();
            }
            guest = updatedGuest;
        }

        updateGuestDetails(guest, request);
        return guest.getId();
    }

    @Transactional
    public Guest updateGuestDetails(Guest guest, BookingUpdateRequest request) {
        if (request.getGuestFirstName() != null) {
            guest.setFirstName(request.getGuestFirstName());
        }

        if (request.getGuestLastName() != null) {
            guest.setLastName(request.getGuestLastName());
        }

        return guestRepository.save(guest);
    }

    @Transactional
    public Guest handleGuestEmailUpdate(Guest currentGuest, String newEmail) {
        Optional<Guest> existingGuest = guestRepository.findByEmail(newEmail);
        
        if (existingGuest.isPresent() && !existingGuest.get().getId().equals(currentGuest.getId())) {
            return existingGuest.get();
        }
        
        currentGuest.setEmail(newEmail);
        return guestRepository.save(currentGuest);
    }

    public Guest getGuestOrThrow(java.util.UUID guestId) {
        return guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));
    }

}
