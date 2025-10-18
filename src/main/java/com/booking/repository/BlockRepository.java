package com.booking.repository;

import com.booking.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BlockRepository extends JpaRepository<Block, UUID> {
    
    List<Block> findByPropertyId(UUID propertyId);
    
    @Query("SELECT b FROM Block b WHERE b.propertyId = :propertyId " +
           "AND ((b.startDate <= :endDate AND b.endDate >= :startDate))")
    List<Block> findOverlappingBlocks(
        @Param("propertyId") UUID propertyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
