package com.yilinglin10.auctionservice.schedule;

import com.yilinglin10.auctionservice.AuctionServiceApplication;
import com.yilinglin10.auctionservice.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final AuctionService auctionService;

    static Logger logger = Logger.getLogger(AuctionServiceApplication.class.getSimpleName());

    @Scheduled(cron = "0 * * * * *")
    public void endAuctions() {
        LocalDateTime currTime = LocalDateTime.now();
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("Check bidding status, time: %s", currTime));
        }

        auctionService.endAuctions(currTime);
    }
}
