package com.yilinglin10.messageservice.kafka;

import com.yilinglin10.messageservice.event.consume.AuctionFailedEvent;
import com.yilinglin10.messageservice.event.consume.BidPlacedEvent;
import com.yilinglin10.messageservice.event.consume.WinnerDeterminedEvent;
import com.yilinglin10.messageservice.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@KafkaListener(id="auction", topics = "auctionNotificationTopic")
public class AuctionNotificationTopicListener {

    @Autowired
    private MessageService messageService;

    @KafkaHandler
    public void handleBidPlacedEvent(BidPlacedEvent bidPlacedEvent) {
        log.info("AUCTION {} PRICE UPDATED: notifying seller and previous bidders...", bidPlacedEvent.getAuctionName());
        String seller_subject = "Notification: Price Update on Your Auction "+bidPlacedEvent.getAuctionName();
        String seller_content = "Auction "+bidPlacedEvent.getAuctionName()+"'s price has been updated to "+bidPlacedEvent.getCurrentPrice()+".";
        messageService.sendNotification(seller_subject, seller_content, bidPlacedEvent.getTimestamp(), List.of(bidPlacedEvent.getSellerId()));

        if (bidPlacedEvent.getPreviousBidders()== null || bidPlacedEvent.getPreviousBidders().size()==0) return;
        String bidder_subject = "Notification: Price Update on Auction "+bidPlacedEvent.getAuctionName()+" price update";
        String bidder_content = "Someone outbid you! Auction "+bidPlacedEvent.getAuctionName()+"'s price has been updated to "+bidPlacedEvent.getCurrentPrice()+".";
        messageService.sendNotification(bidder_subject, bidder_content, bidPlacedEvent.getTimestamp(), bidPlacedEvent.getPreviousBidders());
    }

    @KafkaHandler
    public void handleAuctionFailedEvent(AuctionFailedEvent auctionFailedEvent) {
        log.info("AUCTION {} FAILED: notifying seller and bidders of auction...", auctionFailedEvent.getAuctionName());
        String seller_subject = "Notification: Your Auction "+auctionFailedEvent.getAuctionName() + " Failed";
        String seller_content = "We're sorry. Your auction " + auctionFailedEvent.getAuctionName() + " has failed.";
        messageService.sendNotification(seller_subject, seller_content, auctionFailedEvent.getTimestamp(), List.of(auctionFailedEvent.getSellerId()));

        if (auctionFailedEvent.getBidders()==null || auctionFailedEvent.getBidders().size()==0) return;
        String bidder_subject = "Notification: Your bidding auction "+auctionFailedEvent.getAuctionName() + " has failed";
        String bidder_content = "We're sorry. Your bid price is lower than the reserve price for auction " + auctionFailedEvent.getAuctionName()+". The auction has failed.";
        messageService.sendNotification(bidder_subject, bidder_content, auctionFailedEvent.getTimestamp(), auctionFailedEvent.getBidders());
    }

    @KafkaHandler
    public void handleWinnerDeterminedEvent(WinnerDeterminedEvent winnerDeterminedEvent) {
        log.info("AUCTION {} SOLD: notifying seller, winner, and previous bidders...", winnerDeterminedEvent.getAuctionName());
        String seller_subject = "Notification: Your Auction "+winnerDeterminedEvent.getAuctionName() + " Is Sold";
        String seller_content = "Congratulations! Your auction "+winnerDeterminedEvent.getAuctionName()+"is sold!"+".";
        messageService.sendNotification(seller_subject, seller_content, winnerDeterminedEvent.getTimestamp(), List.of(winnerDeterminedEvent.getSellerId()));

        String winner_subject = "Notification: Your Are the Winner of Auction "+winnerDeterminedEvent.getAuctionName();
        String winner_content = "Congratulations! You won the auction "+winnerDeterminedEvent.getAuctionName()+"!";
        messageService.sendNotification(winner_subject, winner_content, winnerDeterminedEvent.getTimestamp(), List.of(winnerDeterminedEvent.getWinnerId()));

        if (winnerDeterminedEvent.getPreviousBidders()== null || winnerDeterminedEvent.getPreviousBidders().size()==0) return;
        String bidder_subject = "Notification: Auction "+winnerDeterminedEvent.getAuctionName()+" IS SOLD TO SOMEONE ELSE";
        String bidder_content = "We're sorry to inform you that the auction "+winnerDeterminedEvent.getAuctionName()+" is sold to another bidder.";
        messageService.sendNotification(bidder_subject, bidder_content, winnerDeterminedEvent.getTimestamp(), winnerDeterminedEvent.getPreviousBidders());
    }
}
