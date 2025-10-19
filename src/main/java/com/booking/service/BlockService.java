package com.booking.service;

import com.booking.dto.BlockRequest;
import com.booking.dto.BlockResponse;
import com.booking.dto.BlockUpdateRequest;
import com.booking.exception.BookingException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.Block;
import com.booking.model.Booking;
import com.booking.model.Property;
import com.booking.repository.BlockRepository;
import com.booking.repository.BookingRepository;
import com.booking.repository.PropertyRepository;
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
public class BlockService {

    private final BlockRepository blockRepository;
    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final BookingValidator bookingValidator;

    @Transactional
    public BlockResponse createBlock(BlockRequest request) {
        log.info("Creating block for property: {}", request.getPropertyId());

        bookingValidator.validatePropertyExists(request.getPropertyId());
        validateOwnership(request.getPropertyId(), request.getOwnerId());
        bookingValidator.validateDates(request.getStartDate(), request.getEndDate());
        bookingValidator.validatePropertyNotBooked(request.getPropertyId(), request.getStartDate(), request.getEndDate());
        bookingValidator.validatePropertyNotBlocked(request.getPropertyId(), request.getStartDate(), request.getEndDate());

        Block block = saveBlock(request);
        log.info("Block created successfully with id: {}", block.getId());

        return BlockResponse.fromModel(block);
    }

    @Transactional
    public BlockResponse updateBlock(UUID blockId, BlockUpdateRequest request) {
        log.info("Updating block with id: {}", blockId);

        Block block = getBlockOrThrow(blockId);
        validateOwnership(block.getPropertyId(), request.getOwnerId());

        if (request.getStartDate() != null || request.getEndDate() != null) {
            LocalDate newStartDate = request.getStartDate() != null ? request.getStartDate() : block.getStartDate();
            LocalDate newEndDate = request.getEndDate() != null ? request.getEndDate() : block.getEndDate();

            bookingValidator.validateDates(newStartDate, newEndDate);
            bookingValidator.validatePropertyNotBooked(block.getPropertyId(), newStartDate, newEndDate);
            bookingValidator.validateNoOverlappingBlocksForUpdate(block.getPropertyId(), newStartDate, newEndDate, blockId);

            block.setStartDate(newStartDate);
            block.setEndDate(newEndDate);
        }

        if (request.getReason() != null) {
            block.setReason(request.getReason());
        }

        block = blockRepository.save(block);
        log.info("Block updated successfully with id: {}", block.getId());

        return BlockResponse.fromModel(block);
    }

    @Transactional
    public void deleteBlock(UUID blockId, UUID ownerId) {
        log.info("Deleting block with id: {}", blockId);

        Block block = getBlockOrThrow(blockId);
        validateOwnership(block.getPropertyId(), ownerId);
        blockRepository.delete(block);
        
        log.info("Block deleted successfully with id: {}", blockId);
    }

    @Transactional(readOnly = true)
    public BlockResponse getBlock(UUID blockId) {
        log.info("Fetching block with id: {}", blockId);
        Block block = getBlockOrThrow(blockId);
        return BlockResponse.fromModel(block);
    }

    @Transactional(readOnly = true)
    public List<BlockResponse> getBlocksByPropertyId(UUID propertyId) {
        log.info("Fetching blocks for property id: {}", propertyId);
        
        bookingValidator.validatePropertyExists(propertyId);
        
        List<Block> blocks = blockRepository.findByPropertyId(propertyId);
        return blocks.stream()
                .map(BlockResponse::fromModel)
                .toList();
    }


    private Block saveBlock(BlockRequest request) {
        Block block = Block.builder()
                .propertyId(request.getPropertyId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .build();

        block = blockRepository.save(block);
        return block;
    }

    private Block getBlockOrThrow(UUID blockId) {
        return blockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found with id: " + blockId));
    }

    private void validateOwnership(UUID propertyId, UUID ownerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        if (!property.getOwnerId().equals(ownerId)) {
            throw new BookingException("You are not authorized to manage blocks for this property");
        }
    }
}
