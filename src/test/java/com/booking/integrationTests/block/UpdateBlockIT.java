package com.booking.integrationTests.block;

import com.booking.assertion.BlockAssertion;
import com.booking.dto.BlockResponse;
import com.booking.dto.BlockUpdateRequest;
import com.booking.integrationTests.BaseIntegrationTest;
import com.booking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Update Block Integration Tests")
class UpdateBlockIT extends BaseIntegrationTest {

    private Block testBlock;

    @BeforeEach
    void setUp() {
        testBlock = createBlock(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "Maintenance"
        );
    }

    @Test
    @DisplayName("Should update block dates successfully")
    void shouldUpdateBlockDatesSuccessfully() throws Exception {
        BlockUpdateRequest request = new BlockUpdateRequest();
        request.setOwnerId(testOwner.getId());
        request.setStartDate(LocalDate.now().plusDays(15));
        request.setEndDate(LocalDate.now().plusDays(20));

        MvcResult result = mockMvc.perform(patch("/api/blocks/{blockId}", testBlock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        BlockResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BlockResponse.class
        );

        BlockAssertion.assertThat(response)
                .hasId(testBlock.getId())
                .hasPropertyId(testProperty.getId())
                .hasStartDate(request.getStartDate())
                .hasEndDate(request.getEndDate());
    }

    @Test
    @DisplayName("Should update block reason successfully")
    void shouldUpdateBlockReasonSuccessfully() throws Exception {
        BlockUpdateRequest request = new BlockUpdateRequest();
        request.setOwnerId(testOwner.getId());
        request.setReason("Renovation");

        MvcResult result = mockMvc.perform(patch("/api/blocks/{blockId}", testBlock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        BlockResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BlockResponse.class
        );

        BlockAssertion.assertThat(response)
                .hasId(testBlock.getId())
                .hasReason("Renovation");
    }

    @Test
    @DisplayName("Should update both dates and reason")
    void shouldUpdateBothDatesAndReason() throws Exception {
        BlockUpdateRequest request = new BlockUpdateRequest();
        request.setOwnerId(testOwner.getId());
        request.setStartDate(LocalDate.now().plusDays(15));
        request.setEndDate(LocalDate.now().plusDays(20));
        request.setReason("Deep cleaning");

        MvcResult result = mockMvc.perform(patch("/api/blocks/{blockId}", testBlock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        BlockResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BlockResponse.class
        );

        BlockAssertion.assertThat(response)
                .hasId(testBlock.getId())
                .hasStartDate(request.getStartDate())
                .hasEndDate(request.getEndDate())
                .hasReason("Deep cleaning");
    }

    @Test
    @DisplayName("Should fail when block does not exist")
    void shouldFailWhenBlockDoesNotExist() throws Exception {
        BlockUpdateRequest request = new BlockUpdateRequest();
        request.setOwnerId(testOwner.getId());
        request.setStartDate(LocalDate.now().plusDays(15));
        request.setEndDate(LocalDate.now().plusDays(20));

        mockMvc.perform(patch("/api/blocks/{blockId}", java.util.UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Block not found")));
    }

    @Test
    @DisplayName("Should fail when owner does not own the property")
    void shouldFailWhenOwnerDoesNotOwnProperty() throws Exception {
        Owner anotherOwner = createOwner("Another", "Owner", "another.owner@example.com", "+9876543210");

        BlockUpdateRequest request = new BlockUpdateRequest();
        request.setOwnerId(anotherOwner.getId());
        request.setStartDate(LocalDate.now().plusDays(15));
        request.setEndDate(LocalDate.now().plusDays(20));

        mockMvc.perform(patch("/api/blocks/{blockId}", testBlock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("not authorized")));
    }

    @Test
    @DisplayName("Should fail when start date is after end date")
    void shouldFailWhenStartDateIsAfterEndDate() throws Exception {
        BlockUpdateRequest request = new BlockUpdateRequest();
        request.setOwnerId(testOwner.getId());
        request.setStartDate(LocalDate.now().plusDays(20));
        request.setEndDate(LocalDate.now().plusDays(15));

        mockMvc.perform(patch("/api/blocks/{blockId}", testBlock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date must be before end date")));
    }

    @Test
    @DisplayName("Should fail when start date is in the past")
    void shouldFailWhenStartDateIsInThePast() throws Exception {
        BlockUpdateRequest request = new BlockUpdateRequest();
        request.setOwnerId(testOwner.getId());
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(patch("/api/blocks/{blockId}", testBlock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Start date cannot be in the past")));
    }

    @Test
    @DisplayName("Should fail when new dates overlap with existing booking")
    void shouldFailWhenNewDatesOverlapWithExistingBooking() throws Exception {
        createBooking(
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(20),
                BookingStatus.CONFIRMED
        );

        BlockUpdateRequest request = new BlockUpdateRequest();
        request.setOwnerId(testOwner.getId());
        request.setStartDate(LocalDate.now().plusDays(17));
        request.setEndDate(LocalDate.now().plusDays(22));

        mockMvc.perform(patch("/api/blocks/{blockId}", testBlock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is already booked")));
    }

    @Test
    @DisplayName("Should fail when new dates overlap with another block")
    void shouldFailWhenNewDatesOverlapWithAnotherBlock() throws Exception {
        createBlock(
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(20),
                "Another maintenance"
        );

        BlockUpdateRequest request = new BlockUpdateRequest();
        request.setOwnerId(testOwner.getId());
        request.setStartDate(LocalDate.now().plusDays(17));
        request.setEndDate(LocalDate.now().plusDays(22));

        mockMvc.perform(patch("/api/blocks/{blockId}", testBlock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(containsString("Property is already blocked")));
    }
}
