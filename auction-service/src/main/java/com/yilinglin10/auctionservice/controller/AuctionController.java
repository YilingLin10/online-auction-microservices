package com.yilinglin10.auctionservice.controller;

import com.yilinglin10.auctionservice.dto.AuctionListView;
import com.yilinglin10.auctionservice.dto.EditItemRequest;
import com.yilinglin10.auctionservice.dto.ListAuctionRequest;
import com.yilinglin10.auctionservice.dto.PlaceBidRequest;
import com.yilinglin10.auctionservice.exception.InvalidSellerException;
import com.yilinglin10.auctionservice.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    @PostMapping
    public ResponseEntity<Object> listAuction(@Valid @RequestBody ListAuctionRequest request, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        String result = auctionService.listAuction(request, userId);
        response.put("response", result);
        return ResponseEntity.status(result.equals("successful") ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getAuctionDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("auction",  auctionService.getAuction(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}")
    public ResponseEntity<Object> updateItem(@PathVariable Long id, @Valid @RequestBody EditItemRequest request, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        String result = auctionService.updateItem(id, request, userId);
        response.put("response", result);
        if (result.equals("successful")) {
            return ResponseEntity.ok(response);
        }else {
            response.put("response", "failed to edit item with id "+ id);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteAuction(@PathVariable Long id, @RequestHeader("loggedInUser") String userId)  {
        Map<String, Object> response = new HashMap<>();
        String result = auctionService.deleteAuction(id, userId);
        response.put("response", result);
        if (result.equals("successful")) {
            return ResponseEntity.ok(response);
        }else {
            response.put("response", "failed to delete auction with id "+ id);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAuctionList(@RequestParam("offset") Integer offset, @RequestParam("page-size") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();
        List<AuctionListView> auctions = auctionService.findAllWithPagination(offset, pageSize);
        response.put("auctions", auctions);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Object> searchAuctions(@RequestParam("query") String query, @RequestParam("offset") Integer offset, @RequestParam("page-size") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();
        List<AuctionListView> auctions = auctionService.searchAuctionsWithPagination(query, offset, pageSize);
        response.put("auctions", auctions);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/seller")
    public ResponseEntity<Object> findBySellerIdAndStatus(@RequestParam(name = "status", required = false) String status, @RequestParam("offset") Integer offset, @RequestParam("page-size") Integer pageSize, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        List<AuctionListView> auctions = (status == null) ? auctionService.findBySellerId(userId, offset, pageSize) : auctionService.findBySellerIdAndStatus(userId, status, offset, pageSize);
        response.put("auctions", auctions);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/buyer")
    public ResponseEntity<Object> getBiddingHistory(@RequestParam(name = "status", required = false) String status, @RequestParam("offset") Integer offset, @RequestParam("page-size") Integer pageSize, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        List<AuctionListView> auctions = (status == null) ? auctionService.findByBidsBidderId(userId, offset, pageSize) : auctionService.findByBidsBidderIdAndStatus(userId, status, offset, pageSize);
        response.put("auctions", auctions);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/buyer/won")
    public ResponseEntity<Object> getWonAuctions(@RequestParam("offset") Integer offset, @RequestParam("page-size") Integer pageSize, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        List<AuctionListView> auctions = auctionService.getWonAuctions(userId, offset, pageSize);
        response.put("auctions", auctions);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/bids")
    public ResponseEntity<Object> placeBid(@PathVariable Long id, @Valid @RequestBody PlaceBidRequest request, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        String result = auctionService.placeBid(id, Long.parseLong(userId), request);
        response.put("response", result);
        return ResponseEntity.status(result.equals("successful") ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }
}
