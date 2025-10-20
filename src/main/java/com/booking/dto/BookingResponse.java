package com.booking.dto;

import com.booking.model.Booking;
import com.booking.model.BookingStatus;
import com.booking.model.Guest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private UUID id;
    private UUID propertyId;
    private UUID guestId;
    private String guestFirstName;
    private String guestLastName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BookingStatus status;

    public static BookingResponse fromModel(Booking booking, Guest guest) {
        return BookingResponse.builder()
                .id(booking.getId())
                .propertyId(booking.getPropertyId())
                .guestId(booking.getGuestId())
                .guestFirstName(guest != null ? guest.getFirstName() : null)
                .guestLastName(guest != null ? guest.getLastName() : null)
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .status(booking.getStatus())
                .build();
    }
}
