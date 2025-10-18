package com.booking.controller;

import com.booking.dto.BlockRequest;
import com.booking.dto.BlockResponse;
import com.booking.dto.BlockUpdateRequest;
import com.booking.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@Slf4j
public class BlockController {

    private final BlockService blockService;

    @PostMapping
    public ResponseEntity<BlockResponse> createBlock(@Valid @RequestBody BlockRequest request) {
        log.info("Received request to create block for property: {}", request.getPropertyId());
        BlockResponse response = blockService.createBlock(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{blockId}")
    public ResponseEntity<BlockResponse> getBlock(@PathVariable UUID blockId) {
        log.info("Received request to get block with id: {}", blockId);
        BlockResponse response = blockService.getBlock(blockId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<BlockResponse>> getBlocksByPropertyId(@PathVariable UUID propertyId) {
        log.info("Received request to get blocks for property id: {}", propertyId);
        List<BlockResponse> responses = blockService.getBlocksByPropertyId(propertyId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{blockId}")
    public ResponseEntity<BlockResponse> updateBlock(
            @PathVariable UUID blockId,
            @Valid @RequestBody BlockUpdateRequest request) {
        log.info("Received request to update block with id: {}", blockId);
        BlockResponse response = blockService.updateBlock(blockId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> deleteBlock(
            @PathVariable UUID blockId,
            @RequestParam UUID ownerId) {
        log.info("Received request to delete block with id: {} by owner: {}", blockId, ownerId);
        blockService.deleteBlock(blockId, ownerId);
        return ResponseEntity.noContent().build();
    }
}
