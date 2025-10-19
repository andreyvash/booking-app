package com.booking.config;

import com.booking.model.Guest;
import com.booking.model.Owner;
import com.booking.model.Property;
import com.booking.repository.GuestRepository;
import com.booking.repository.OwnerRepository;
import com.booking.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final OwnerRepository ownerRepository;
    private final PropertyRepository propertyRepository;
    private final GuestRepository guestRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing sample data...");

        // Create sample owners
        Owner owner1 = new Owner();
        owner1.setFirstName("Alice");
        owner1.setLastName("Johnson");
        owner1.setEmail("alice.johnson@example.com");
        owner1.setPhone("+1-555-0101");
        owner1 = ownerRepository.save(owner1);
        log.info("Created Owner: {} {} - ID: {}", owner1.getFirstName(), owner1.getLastName(), owner1.getId());

        Owner owner2 = new Owner();
        owner2.setFirstName("Michael");
        owner2.setLastName("Brown");
        owner2.setEmail("michael.brown@example.com");
        owner2.setPhone("+1-555-0102");
        owner2 = ownerRepository.save(owner2);
        log.info("Created Owner: {} {} - ID: {}", owner2.getFirstName(), owner2.getLastName(), owner2.getId());

        Owner owner3 = new Owner();
        owner3.setFirstName("Sarah");
        owner3.setLastName("Davis");
        owner3.setEmail("sarah.davis@example.com");
        owner3.setPhone("+1-555-0103");
        owner3 = ownerRepository.save(owner3);
        log.info("Created Owner: {} {} - ID: {}", owner3.getFirstName(), owner3.getLastName(), owner3.getId());

        // Create sample properties
        Property property1 = new Property();
        property1.setName("Beachfront Villa");
        property1.setAddress("123 Ocean Drive, Miami Beach, FL 33139");
        property1.setDescription("Luxurious beachfront villa with stunning ocean views");
        property1.setOwnerId(owner1.getId());
        property1 = propertyRepository.save(property1);
        log.info("Created Property: {} - ID: {} (Owner: {})", property1.getName(), property1.getId(), owner1.getFirstName());

        Property property2 = new Property();
        property2.setName("Mountain Cabin");
        property2.setAddress("456 Pine Ridge Road, Aspen, CO 81611");
        property2.setDescription("Cozy mountain cabin perfect for winter getaways");
        property2.setOwnerId(owner1.getId());
        property2 = propertyRepository.save(property2);
        log.info("Created Property: {} - ID: {} (Owner: {})", property2.getName(), property2.getId(), owner1.getFirstName());

        Property property3 = new Property();
        property3.setName("City Apartment");
        property3.setAddress("789 Broadway, New York, NY 10003");
        property3.setDescription("Modern apartment in the heart of Manhattan");
        property3.setOwnerId(owner2.getId());
        property3 = propertyRepository.save(property3);
        log.info("Created Property: {} - ID: {} (Owner: {})", property3.getName(), property3.getId(), owner2.getFirstName());

        Property property4 = new Property();
        property4.setName("Desert Oasis");
        property4.setAddress("321 Cactus Lane, Scottsdale, AZ 85251");
        property4.setDescription("Beautiful desert retreat with pool and spa");
        property4.setOwnerId(owner2.getId());
        property4 = propertyRepository.save(property4);
        log.info("Created Property: {} - ID: {} (Owner: {})", property4.getName(), property4.getId(), owner2.getFirstName());

        Property property5 = new Property();
        property5.setName("Lake House");
        property5.setAddress("555 Lakeview Drive, Lake Tahoe, CA 96150");
        property5.setDescription("Peaceful lakeside house with private dock");
        property5.setOwnerId(owner3.getId());
        property5 = propertyRepository.save(property5);
        log.info("Created Property: {} - ID: {} (Owner: {})", property5.getName(), property5.getId(), owner3.getFirstName());

        // Create sample guests
        Guest guest1 = new Guest();
        guest1.setFirstName("John");
        guest1.setLastName("Doe");
        guest1.setEmail("john.doe@example.com");
        guest1 = guestRepository.save(guest1);
        log.info("Created Guest: {} {} - ID: {}", guest1.getFirstName(), guest1.getLastName(), guest1.getId());

        Guest guest2 = new Guest();
        guest2.setFirstName("Jane");
        guest2.setLastName("Smith");
        guest2.setEmail("jane.smith@example.com");
        guest2 = guestRepository.save(guest2);
        log.info("Created Guest: {} {} - ID: {}", guest2.getFirstName(), guest2.getLastName(), guest2.getId());

        Guest guest3 = new Guest();
        guest3.setFirstName("Bob");
        guest3.setLastName("Wilson");
        guest3.setEmail("bob.wilson@example.com");
        guest3 = guestRepository.save(guest3);
        log.info("Created Guest: {} {} - ID: {}", guest3.getFirstName(), guest3.getLastName(), guest3.getId());

        Guest guest4 = new Guest();
        guest4.setFirstName("Emily");
        guest4.setLastName("Martinez");
        guest4.setEmail("emily.martinez@example.com");
        guest4 = guestRepository.save(guest4);
        log.info("Created Guest: {} {} - ID: {}", guest4.getFirstName(), guest4.getLastName(), guest4.getId());

        Guest guest5 = new Guest();
        guest5.setFirstName("David");
        guest5.setLastName("Lee");
        guest5.setEmail("david.lee@example.com");
        guest5 = guestRepository.save(guest5);
        log.info("Created Guest: {} {} - ID: {}", guest5.getFirstName(), guest5.getLastName(), guest5.getId());

        log.info("========================================");
        log.info("Sample data initialized successfully!");
        log.info("Created {} owners, {} properties, and {} guests",
                ownerRepository.count(),
                propertyRepository.count(),
                guestRepository.count());
        log.info("========================================");
    }
}
