package com.yilinglin10.auctionservice;

import com.yilinglin10.auctionservice.model.Auction;
import com.yilinglin10.auctionservice.model.AuctionStatus;
import com.yilinglin10.auctionservice.model.Item;
import com.yilinglin10.auctionservice.repository.AuctionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling
public class AuctionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuctionServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(AuctionRepository auctionRepository) {
        return args -> {
            for (int i=1; i<=1; i++) {
                Auction auction = Auction.builder()
                        .sellerId((long) 1)
                        .item(Item.builder()
                                .name("iPhone_"+i)
                                .description("")
                                .build())
                        .createdAt(LocalDateTime.now())
                        .endAt(LocalDateTime.now().plusMinutes(3))
                        .startPrice((double) 100)
                        .reservePrice((double) 500)
                        .currentPrice((double) 100)
                        .status(AuctionStatus.ACTIVE)
                        .build();
                auctionRepository.save(auction);
            }
            for (int i=11; i<=20; i++) {
                Auction auction = Auction.builder()
                        .sellerId((long) 2)
                        .item(Item.builder()
                                .name("iPhone_"+i)
                                .description("")
                                .build())
                        .createdAt(LocalDateTime.now())
                        .endAt(LocalDateTime.now().plusMinutes(3))
                        .startPrice((double) 100)
                        .reservePrice((double) 500)
                        .currentPrice((double) 100)
                        .status(AuctionStatus.ACTIVE)
                        .build();
                auctionRepository.save(auction);
            }

        };
    }
}
