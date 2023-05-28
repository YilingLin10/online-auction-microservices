package com.yilinglin10.auctionservice.service;

import com.yilinglin10.auctionservice.dto.*;
import com.yilinglin10.auctionservice.event.publish.AuctionFailedEvent;
import com.yilinglin10.auctionservice.event.publish.BidPlacedEvent;
import com.yilinglin10.auctionservice.event.publish.WinnerDeterminedEvent;
import com.yilinglin10.auctionservice.exception.*;
import com.yilinglin10.auctionservice.model.Auction;
import com.yilinglin10.auctionservice.model.AuctionStatus;
import com.yilinglin10.auctionservice.model.Bid;
import com.yilinglin10.auctionservice.model.Item;
import com.yilinglin10.auctionservice.repository.AuctionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Transactional
@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AuctionBuyerView getAuction(Long id) {
        Auction auction = auctionRepository.findById(id).orElseThrow(()-> new AuctionNotFoundException(id));
        return mapEntityToBuyerView(auction);
    }

    public String listAuction(ListAuctionRequest request, String sellerId) {
        if (request.getReservePrice() < request.getStartPrice()) {
            return "the start price should be smaller than the reserve price";
        }
        Auction auction = Auction.builder()
                .sellerId(Long.parseLong(sellerId))
                .item(Item.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .build())
                .startPrice(request.getStartPrice())
                .reservePrice(request.getReservePrice())
                .currentPrice(request.getStartPrice())
                .createdAt(LocalDateTime.now())
                .endAt(request.getEndAt())
                .bids(new ArrayList<>())
                .status(AuctionStatus.ACTIVE)
                .build();

        auctionRepository.save(auction);
        return "successful";
    }

    public String updateItem(Long id, EditItemRequest request, String userId){
        Auction auction = auctionRepository.findById(id).orElseThrow(()-> new AuctionNotFoundException(id));
        boolean validSeller = validateSeller(auction.getSellerId(), userId);
        if (!validSeller) {
            throw new InvalidSellerException(userId);
        }
        auction.setItem(Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build()
        );
        auctionRepository.save(auction);
        return "successful";
    }

    public String deleteAuction(Long id, String userId) {
        Auction auction = auctionRepository.findById(id).orElseThrow(()-> new AuctionNotFoundException(id));
        boolean validSeller = validateSeller(auction.getSellerId(), userId);
        if (!validSeller) {
            throw new InvalidSellerException(userId);
        }
        if (auction.getBids().size() > 0) {
            throw new CannotDeleteAuctionException(id);
        }
        auctionRepository.delete(auction);
        return "successful";
    }

    public List<AuctionListView> findAllWithPagination(int offset, int pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("endAt"));
        Page<Auction> auctions = auctionRepository.findAll(pageable);
        return auctions.map(this::mapEntityToListView).toList();
    }

    public List<AuctionListView> searchAuctionsWithPagination(String query, int offset, int pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("endAt"));
        Page<Auction> auctions = auctionRepository.searchAuctionsWithPagination(query, pageable);
        return auctions.map(this::mapEntityToListView).toList();
    }

    public List<AuctionListView> findBySellerId(String sellerId, int offset, int pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("endAt"));
        List<Auction> auctions = auctionRepository.findBySellerId(Long.parseLong(sellerId), pageable);
        return auctions.stream().map(this::mapEntityToListView).toList();
    }

    public List<AuctionListView> findBySellerIdAndStatus(String sellerId, String status, int offset, int pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("endAt"));
        List<Auction> auctions = auctionRepository.findBySellerIdAndStatus(Long.parseLong(sellerId), convertToEnumStatus(status), pageable);
        return auctions.stream().map(this::mapEntityToListView).toList();
    }

    public List<AuctionListView> findByBidsBidderId(String bidderId, Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("endAt"));
        List<Auction> auctions = auctionRepository.findByBidsBidderId(Long.parseLong(bidderId), pageable);
        return auctions.stream().map(this::mapEntityToListView).toList();
    }

    public List<AuctionListView> findByBidsBidderIdAndStatus(String bidderId, String status, Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("endAt"));
        List<Auction> auctions = auctionRepository.findByBidsBidderIdAndStatus(Long.parseLong(bidderId), convertToEnumStatus(status), pageable);
        return auctions.stream().map(this::mapEntityToListView).toList();
    }

    public List<AuctionListView> getWonAuctions(String lastBidderId, Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by("endAt"));
        List<Auction> auctions = auctionRepository.findByLastBidderIdAndStatus(Long.parseLong(lastBidderId), AuctionStatus.SOLD, pageable);
        return auctions.stream().map(this::mapEntityToListView).toList();
    }

    public String placeBid(Long auctionId, Long userId, PlaceBidRequest request) {
        LocalDateTime currTime = LocalDateTime.now();
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(()-> new AuctionNotFoundException(auctionId));

        boolean validBidder = validateBidder(auction.getSellerId(), userId);
        boolean validBidPrice = validateBidPrice(auction, request);
        if (!validBidder) {
            throw new InvalidPlaceBidRequestException("sellers cannot place bids on their own auctions.");
        }
        if (!auction.getStatus().equals(AuctionStatus.ACTIVE) || currTime.isAfter(auction.getEndAt())) {
            throw new InvalidPlaceBidRequestException("cannot place bids on ended auction.");
        }
        if (!validBidPrice) {
            throw new InvalidPlaceBidRequestException("bid price must be greater than the current price.");
        }
        auction.getBids().add(
                Bid.builder()
                        .bidderId(userId)
                        .price(request.getPrice())
                        .createdAt(currTime)
                        .build());
        auction.setCurrentPrice(request.getPrice());
        auction.setLastBidderId(userId);
        auctionRepository.save(auction);

        List<Long> previousBidders = auction.getBids().stream()
                        .map(Bid::getBidderId)
                        .distinct()
                        .filter(id-> !id.equals(userId) )
                        .toList();

        kafkaTemplate.send("auctionNotificationTopic", BidPlacedEvent.builder()
                .id(UUID.randomUUID())
                .sellerId(auction.getSellerId())
                .auctionName(auction.getItem().getName())
                .currentPrice(auction.getCurrentPrice())
                .timestamp(currTime)
                .previousBidders(previousBidders)
                .build());

        return "successful";
    }

    public List<Auction> findByStatus(AuctionStatus status) {
        return auctionRepository.findByStatus(status);
    }
    public void updateAuctionStatus(Auction auction, AuctionStatus status) {
        auction.setStatus(status);
        auctionRepository.save(auction);
    }

    private boolean validateBidPrice(Auction auction, PlaceBidRequest request) {
        return request.getPrice() > auction.getCurrentPrice();
    }

    private boolean validateBidder(Long sellerId, Long userId) {
        return !sellerId.equals(userId);
    }

    private AuctionStatus convertToEnumStatus(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(AuctionStatus.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(()-> new IllegalArgumentException("invalid status"));
    }



    private boolean validateSeller(Long sellerId, String userId) {
        return sellerId.equals(Long.parseLong(userId));
    }

    private AuctionBuyerView mapEntityToBuyerView(Auction auction) {
        return AuctionBuyerView.builder()
                .id(auction.getId())
                .sellerId(auction.getSellerId())
                .item(auction.getItem())
                .currentPrice(auction.getCurrentPrice())
                .lastBidderId(auction.getLastBidderId())
                .createdAt(auction.getCreatedAt())
                .endAt(auction.getEndAt())
                .status(auction.getStatus())
                .bids(auction.getBids())
                .build();
    }

    private AuctionListView mapEntityToListView(Auction auction) {
        return AuctionListView.builder()
                .id(auction.getId())
                .name(auction.getItem().getName())
                .sellerId(auction.getSellerId())
                .currentPrice(auction.getCurrentPrice())
                .endAt(auction.getEndAt())
                .status(auction.getStatus())
                .build();
    }

    public void endAuctions(LocalDateTime timestamp) {
        for (Auction auction: findByStatus(AuctionStatus.ACTIVE)) {
            if (auction.getEndAt().equals(timestamp) || timestamp.isAfter(auction.getEndAt())) {
                determineWinner(auction, timestamp);
            }
        }
    }

    // Scheduled tasks
    public void determineWinner(Auction auction, LocalDateTime timestamp) {

        if (auction.getCurrentPrice().equals(auction.getStartPrice()) || auction.getCurrentPrice() < auction.getReservePrice()) {
            updateAuctionStatus(auction, AuctionStatus.FAILED);

            List<Long> bidders = auction.getBids().stream().map(Bid::getBidderId).distinct().toList();
            kafkaTemplate.send("auctionNotificationTopic", AuctionFailedEvent.builder()
                    .id(UUID.randomUUID())
                    .auctionName(auction.getItem().getName())
                    .sellerId(auction.getSellerId())
                    .bidders(bidders)
                    .timestamp(timestamp)
                    .build());
        }else {
            updateAuctionStatus(auction, AuctionStatus.SOLD);
            List<Long> previousBidders = auction.getBids()
                    .stream()
                    .map(Bid::getBidderId)
                    .distinct()
                    .filter(id-> !id.equals(auction.getLastBidderId()))
                    .toList();
            WinnerDeterminedEvent winnerDeterminedEvent = WinnerDeterminedEvent.builder()
                    .id(UUID.randomUUID())
                    .auctionId(auction.getId())
                    .auctionName(auction.getItem().getName())
                    .sellerId(auction.getSellerId())
                    .winnerId(auction.getLastBidderId())
                    .previousBidders(previousBidders)
                    .timestamp(timestamp)
                    .build();
            kafkaTemplate.send("auctionNotificationTopic", winnerDeterminedEvent);
            kafkaTemplate.send("orderPlacementTopic", winnerDeterminedEvent);
        }
    }
}
