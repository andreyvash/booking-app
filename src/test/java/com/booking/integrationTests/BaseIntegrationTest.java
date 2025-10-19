package com.booking.integrationTests;

import com.booking.model.*;
import com.booking.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected OwnerRepository ownerRepository;

    @Autowired
    protected PropertyRepository propertyRepository;

    @Autowired
    protected GuestRepository guestRepository;

    @Autowired
    protected BookingRepository bookingRepository;

    @Autowired
    protected BlockRepository blockRepository;

    protected Owner testOwner;
    protected Property testProperty;
    protected Guest testGuest;

    @BeforeEach
    void baseSetUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        blockRepository.deleteAll();
        bookingRepository.deleteAll();
        guestRepository.deleteAll();
        propertyRepository.deleteAll();
        ownerRepository.deleteAll();
    }

    private void createTestData() {
        testOwner = ownerRepository.save(Owner.builder()
                .firstName("Test")
                .lastName("Owner")
                .email("test.owner@example.com")
                .phone("+1234567890")
                .build());

        testProperty = propertyRepository.save(Property.builder()
                .name("Test Property")
                .address("123 Test Street")
                .description("A test property")
                .ownerId(testOwner.getId())
                .build());

        testGuest = guestRepository.save(Guest.builder()
                .firstName("Test")
                .lastName("Guest")
                .email("test.guest@example.com")
                .build());
    }

    protected Booking createBooking(LocalDate startDate, LocalDate endDate, BookingStatus status) {
        return bookingRepository.save(Booking.builder()
                .propertyId(testProperty.getId())
                .guestId(testGuest.getId())
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .build());
    }

    protected Block createBlock(LocalDate startDate, LocalDate endDate, String reason) {
        return blockRepository.save(Block.builder()
                .propertyId(testProperty.getId())
                .startDate(startDate)
                .endDate(endDate)
                .reason(reason)
                .build());
    }

    protected Owner createOwner(String firstName, String lastName, String email, String phone) {
        return ownerRepository.save(Owner.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .build());
    }

    protected Guest createGuest(String firstName, String lastName, String email) {
        return guestRepository.save(Guest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build());
    }
}
