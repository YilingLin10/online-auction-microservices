package com.yilinglin10.auctionservice.repository;

import com.yilinglin10.auctionservice.model.Auction;
import com.yilinglin10.auctionservice.model.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findBySellerId(long sellerId, Pageable pageable);

    @Query(value = "SELECT a FROM Auction a WHERE a.item.name LIKE %:query% OR a.item.description LIKE %:query%")
    Page<Auction> searchAuctionsWithPagination(@Param("query") String query, Pageable pageable);

    List<Auction> findBySellerIdAndStatus(@Param("id") long sellerId, @Param("status") AuctionStatus status, Pageable pageable);

    List<Auction> findByBidsBidderId(long bidderId, Pageable pageable);

    List<Auction> findByBidsBidderIdAndStatus(long bidderId, AuctionStatus status, Pageable pageable);

    List<Auction> findByLastBidderIdAndStatus(long lastBidderId, AuctionStatus status, Pageable pageable);

    List<Auction> findByStatus(AuctionStatus status);
}
