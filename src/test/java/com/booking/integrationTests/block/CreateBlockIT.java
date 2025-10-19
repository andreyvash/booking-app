package com.booking.integrationTests.block;

import com.booking.assertion.BlockAssertion;
import com.booking.dto.BlockRequest;
import com.booking.dto.BlockResponse;
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

@DisplayName("Create Block Integration Tests")
class CreateBlockIT extends BaseIntegrationTest {

    @Test
    @DisplayName("Should create block successfully with valid data")
    void shouldCreateBlockSuccessfully() throws Exception {
        BlockRequest request = new BlockRequest();
        request.setOwnerId(testOwner.getId());
        request.setPropertyId(testProperty.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));
        request.setReason("Maintenance");

        MvcResult createResult = mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        BlockResponse createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                BlockResponse.class
        );

        MvcResult getResult = mockMvc.perform(get("/api/blocks/{blockId}", createResponse.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BlockResponse getResponse = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                BlockResponse.class
        );

        BlockAssertion.assertThat(getResponse)
                .hasId(createResponse.getId())
                .hasPropertyId(testProperty.getId())
                .hasStartDate(request.getStartDate())
                .hasEndDate(request.getEndDate())
                .hasReason("Maintenance");
    }

    @Test
    @DisplayName("Should create block without reason")
    void shouldCreateBlockWithoutReason() throws Exception {
        BlockRequest request = new BlockRequest();
        request.setOwnerId(testOwner.getId());
        request.setPropertyId(testProperty.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        MvcResult createResult = mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        BlockResponse createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                BlockResponse.class
        );

        MvcResult getResult = mockMvc.perform(get("/api/blocks/{blockId}", createResponse.getId()))
                .andExpect(status().isOk())
                .andReturn();

        BlockResponse getResponse = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                BlockResponse.class
        );

        BlockAssertion.assertThat(getResponse)
                .hasId(createResponse.getId())
                .hasPropertyId(testProperty.getId());
    }

    @Test
    @DisplayName("Should fail when property does not exist")
    void shouldFailWhenPropertyDoesNotExist() throws Exception {
        BlockRequest request = new BlockRequest();
        request.setOwnerId(testOwner.getId());
        request.setPropertyId(UUID.randomUUID());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Property not found")));
    }

    @Test
    @DisplayName("Should fail when owner does not own the property")
    void shouldFailWhenOwnerDoesNotOwnProperty() throws Exception {
        Owner anotherOwner = createOwner("Another", "Owner", "another.owner@example.com", "+9876543210");

        BlockRequest request = new BlockRequest();
        request.setOwnerId(anotherOwner.getId());
        request.setPropertyId(testProperty.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("not authorized")));
    }

    @Test
    @DisplayName("Should fail when start date is after end date")
    void shouldFailWhenStartDateIsAfterEndDate() throws Exception {
        BlockRequest request = new BlockRequest();
        request.setOwnerId(testOwner.getId());
        request.setPropertyId(testProperty.getId());
        request.setStartDate(LocalDate.now().plusDays(10));
        request.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date must be before end date")));
    }

    @Test
    @DisplayName("Should fail when start date is in the past")
    void shouldFailWhenStartDateIsInThePast() throws Exception {
        BlockRequest request = new BlockRequest();
        request.setOwnerId(testOwner.getId());
        request.setPropertyId(testProperty.getId());
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date cannot be in the past")));
    }

    @Test
    @DisplayName("Should fail when block overlaps with existing booking")
    void shouldFailWhenBlockOverlapsWithExistingBooking() throws Exception {
        createBooking(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                BookingStatus.CONFIRMED
        );

        BlockRequest request = new BlockRequest();
        request.setOwnerId(testOwner.getId());
        request.setPropertyId(testProperty.getId());
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(12));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is already booked")));
    }

    @Test
    @DisplayName("Should fail when block overlaps with existing block")
    void shouldFailWhenBlockOverlapsWithExistingBlock() throws Exception {
        createBlock(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "Existing maintenance"
        );

        BlockRequest request = new BlockRequest();
        request.setOwnerId(testOwner.getId());
        request.setPropertyId(testProperty.getId());
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(12));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is blocked")));
    }

    @Test
    @DisplayName("Should fail when required fields are missing")
    void shouldFailWhenRequiredFieldsAreMissing() throws Exception {
        BlockRequest request = new BlockRequest();

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
