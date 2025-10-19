package com.booking.integrationTests.block;

import com.booking.integrationTests.BaseIntegrationTest;
import com.booking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Delete Block Integration Tests")
class DeleteBlockIT extends BaseIntegrationTest {

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
    @DisplayName("Should delete block successfully")
    void shouldDeleteBlockSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/blocks/{blockId}", testBlock.getId())
                        .param("ownerId", testOwner.getId().toString()))
                .andExpect(status().isNoContent());

        Optional<Block> deletedBlock = blockRepository.findById(testBlock.getId());
        org.assertj.core.api.Assertions.assertThat(deletedBlock).isEmpty();
    }

    @Test
    @DisplayName("Should fail when block does not exist")
    void shouldFailWhenBlockDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/blocks/{blockId}", java.util.UUID.randomUUID())
                        .param("ownerId", testOwner.getId().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Block not found")));
    }

    @Test
    @DisplayName("Should fail when owner does not own the property")
    void shouldFailWhenOwnerDoesNotOwnProperty() throws Exception {
        Owner anotherOwner = createOwner("Another", "Owner", "another.owner@example.com", "+9876543210");

        mockMvc.perform(delete("/api/blocks/{blockId}", testBlock.getId())
                        .param("ownerId", anotherOwner.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("not authorized")));
    }

    @Test
    @DisplayName("Should remove block from database permanently")
    void shouldRemoveBlockFromDatabasePermanently() throws Exception {
        long countBefore = blockRepository.count();

        mockMvc.perform(delete("/api/blocks/{blockId}", testBlock.getId())
                        .param("ownerId", testOwner.getId().toString()))
                .andExpect(status().isNoContent());

        long countAfter = blockRepository.count();
        org.assertj.core.api.Assertions.assertThat(countAfter).isEqualTo(countBefore - 1);
    }

    @Test
    @DisplayName("Should not affect other blocks when deleting one")
    void shouldNotAffectOtherBlocksWhenDeletingOne() throws Exception {
        Block anotherBlock = createBlock(
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(20),
                "Another maintenance"
        );

        mockMvc.perform(delete("/api/blocks/{blockId}", testBlock.getId())
                        .param("ownerId", testOwner.getId().toString()))
                .andExpect(status().isNoContent());

        Optional<Block> remainingBlock = blockRepository.findById(anotherBlock.getId());
        org.assertj.core.api.Assertions.assertThat(remainingBlock).isPresent();
    }

    @Test
    @DisplayName("Should return 404 when trying to delete already deleted block")
    void shouldReturn404WhenTryingToDeleteAlreadyDeletedBlock() throws Exception {
        mockMvc.perform(delete("/api/blocks/{blockId}", testBlock.getId())
                        .param("ownerId", testOwner.getId().toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/blocks/{blockId}", testBlock.getId())
                        .param("ownerId", testOwner.getId().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Block not found")));
    }

    @Test
    @DisplayName("Should not delete property when deleting block")
    void shouldNotDeletePropertyWhenDeletingBlock() throws Exception {
        mockMvc.perform(delete("/api/blocks/{blockId}", testBlock.getId())
                        .param("ownerId", testOwner.getId().toString()))
                .andExpect(status().isNoContent());

        Optional<Property> property = propertyRepository.findById(testProperty.getId());
        org.assertj.core.api.Assertions.assertThat(property).isPresent();
    }

    @Test
    @DisplayName("Should fail when ownerId parameter is missing")
    void shouldFailWhenOwnerIdParameterIsMissing() throws Exception {
        mockMvc.perform(delete("/api/blocks/{blockId}", testBlock.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Required parameter 'ownerId' is missing")));
    }
}
