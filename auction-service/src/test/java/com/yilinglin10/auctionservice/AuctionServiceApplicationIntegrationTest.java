package com.yilinglin10.auctionservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yilinglin10.auctionservice.dto.EditItemRequest;
import com.yilinglin10.auctionservice.dto.ListAuctionRequest;
import com.yilinglin10.auctionservice.dto.PlaceBidRequest;
import com.yilinglin10.auctionservice.event.publish.AuctionFailedEvent;
import com.yilinglin10.auctionservice.event.publish.BidPlacedEvent;
import com.yilinglin10.auctionservice.event.publish.WinnerDeterminedEvent;
import com.yilinglin10.auctionservice.model.Auction;
import com.yilinglin10.auctionservice.model.AuctionStatus;
import com.yilinglin10.auctionservice.model.Bid;
import com.yilinglin10.auctionservice.model.Item;
import com.yilinglin10.auctionservice.repository.AuctionRepository;
import com.yilinglin10.auctionservice.schedule.ScheduledTasks;
import com.yilinglin10.auctionservice.service.AuctionService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EmbeddedKafka
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@AutoConfigureMockMvc
@Testcontainers
@MockBean(ScheduledTasks.class) //disable running scheduled tasks
@ActiveProfiles("test")
public class AuctionServiceApplicationIntegrationTest {
    private static final String USER_ID_HEADER_NAME = "loggedInUser";
    private static final String AUCTION_NOTIFICATION_KAFKA_TOPIC_NAME = "auctionNotificationTopic";

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0-debian");

    @Autowired
    private MockMvc mockMVC;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private AuctionService auctionService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    private BlockingQueue<ConsumerRecord<String, String>> auctionNotificationRecords;
    private KafkaMessageListenerContainer<String, String> container;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.driverClassName", () -> mySQLContainer.getDriverClassName());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
    }

    @Test
    void listAuction_validRequest_returnsSuccessful() throws Exception {
        // given
        ListAuctionRequest listAuctionRequest = ListAuctionRequest.builder()
                .name("M1 Macbook Pro")
                .startPrice((double) 100)
                .reservePrice((double) 150)
                .endAt(LocalDateTime.now().plusDays(3))
                .build();
        String listAuctionRequestString = objectMapper.writeValueAsString(listAuctionRequest);

        // when
        mockMVC.perform(post("/api/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(listAuctionRequestString)
                .header(USER_ID_HEADER_NAME, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("successful"));

        // then
        Pageable pageable = PageRequest.of(0, 10);
        assertEquals(1, auctionRepository.findBySellerId(10, pageable).size());
    }

    @Test
    void listAuction_blankFields_returnsBadRequestAndErrors() throws Exception {
        // given
        ListAuctionRequest listAuctionRequest = ListAuctionRequest.builder()
                .name("")
                .startPrice((double) 100)
                .reservePrice((double) 150)
                .endAt(LocalDateTime.now())
                .build();
        String listAuctionRequestString = objectMapper.writeValueAsString(listAuctionRequest);

        // when
        mockMVC.perform(post("/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listAuctionRequestString)
                        .header(USER_ID_HEADER_NAME, 10))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listAuction_reservePriceGreaterThanStartPrice_returnsBadRequestAndError() throws Exception {
        // given
        ListAuctionRequest listAuctionRequest = ListAuctionRequest.builder()
                .name("M1 Macbook Pro")
                .startPrice((double) 150)
                .reservePrice((double) 100)
                .endAt(LocalDateTime.now().plusDays(3))
                .build();
        String listAuctionRequestString = objectMapper.writeValueAsString(listAuctionRequest);

        // when
        mockMVC.perform(post("/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listAuctionRequestString)
                        .header(USER_ID_HEADER_NAME, 10))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response").value("the start price should be smaller than the reserve price"));
    }

    @Test
    void getAuctionDetails_existingAuction_returnsAuctionDetails() throws Exception {
        // given
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 100)
                .status(AuctionStatus.ACTIVE)
                .endAt(LocalDateTime.now())
                .bids(new ArrayList<>())
                .build();
        auctionRepository.save(auction);

        //when
        mockMVC.perform(get("/api/auctions/" + auction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auction").exists())
                .andExpect(jsonPath("$.auction.item.name").exists())
                .andExpect(jsonPath("$.auction.status").value("ACTIVE"))
                .andExpect(jsonPath("$.auction.reservePrice").doesNotExist());
    }

    @Test
    void getAuctionDetails_auctionDoesNotExist_returnsNotFound() throws Exception {
        // given
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .status(AuctionStatus.ACTIVE)
                .build();
        auctionRepository.save(auction);
        long id = auction.getId();
        auctionRepository.delete(auction);

        // when
        mockMVC.perform(get("/api/auctions/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_validRequest_returnsSuccessful() throws Exception {
        // given
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .status(AuctionStatus.ACTIVE)
                .build();
        auctionRepository.save(auction);
        EditItemRequest editItemRequest = EditItemRequest.builder()
                .name("new item name")
                .build();
        String editItemRequestString = objectMapper.writeValueAsString(editItemRequest);

        // when
        mockMVC.perform(post("/api/auctions/"+auction.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(editItemRequestString)
                .header(USER_ID_HEADER_NAME, auction.getSellerId()))
                .andExpect(status().isOk());

        // then
        Auction updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow(()->null);
        assertEquals("new item name", updatedAuction.getItem().getName());
    }

    @Test
    void updateItem_invalidSeller_returnsBadRequest() throws Exception {
        // given
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .status(AuctionStatus.ACTIVE)
                .build();
        auctionRepository.save(auction);
        EditItemRequest editItemRequest = EditItemRequest.builder()
                .name("new item name")
                .build();
        String editItemRequestString = objectMapper.writeValueAsString(editItemRequest);

        // when
        mockMVC.perform(post("/api/auctions/"+auction.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editItemRequestString)
                        .header(USER_ID_HEADER_NAME, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid user " + 2 + " attempts to edit/delete an auction"));
    }

    @Test
    void getAuctionList_returnsAuctionList() throws Exception {
        // given
        auctionRepository.deleteAll();
        for (int i=0; i<5; i++) {
            Auction auction = Auction.builder()
                    .sellerId((long) 1)
                    .item(Item.builder().name("item_" + i).build())
                    .status(AuctionStatus.ACTIVE)
                    .build();
            auctionRepository.save(auction);
        }

        // when
        mockMVC.perform(get("/api/auctions")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctions", Matchers.hasSize(5)));
    }

    @Test
    void searchAuctions_validQuery_returnsAuctionList() throws Exception {
        // given
        auctionRepository.deleteAll();
        for (int i=0; i<5; i++) {
            Auction auction = Auction.builder()
                    .sellerId((long) 1)
                    .item(Item.builder().name("item_" + i).build())
                    .status(AuctionStatus.ACTIVE)
                    .build();
            auctionRepository.save(auction);
        }
        for (int i=0; i<5; i++) {
            Auction auction = Auction.builder()
                    .sellerId((long) 1)
                    .item(Item.builder().name("description_" + i).description("item").build())
                    .status(AuctionStatus.ACTIVE)
                    .build();
            auctionRepository.save(auction);
        }

        // when
        mockMVC.perform(get("/api/auctions/search")
                        .param("query", "item")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctions", Matchers.hasSize(10)));
    }

    @Test
    void searchAuctions_noMatchingResults_returnsEmptyList() throws Exception {
        // given
        auctionRepository.deleteAll();
        for (int i=0; i<5; i++) {
            Auction auction = Auction.builder()
                    .sellerId((long) 1)
                    .item(Item.builder().name("item_" + i).build())
                    .status(AuctionStatus.ACTIVE)
                    .build();
            auctionRepository.save(auction);
        }

        // when
        mockMVC.perform(get("/api/auctions/search")
                        .param("query", "xxx")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctions", Matchers.hasSize(0)));
    }

    @Test
    void findBySellerIdAndStatus_StatusNotProvided_returnsAllListedAuctions() throws Exception {
        //given
        auctionRepository.deleteAll();
        for (int i=0; i<5; i++) {
            Auction auction = Auction.builder()
                    .sellerId((long) 1)
                    .item(Item.builder().name("item_" + i).build())
                    .status(AuctionStatus.ACTIVE)
                    .build();
            auctionRepository.save(auction);
        }

        //when
        mockMVC.perform(get("/api/auctions/seller")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10))
                        .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctions", Matchers.hasSize(5)));
    }

    @Test
    void findBySellerIdAndStatus_statusIsSold_returnsAllSoldAuctions() throws Exception {
        //given
        auctionRepository.deleteAll();
        for (int i=0; i<5; i++) {
            Auction auction = Auction.builder()
                    .sellerId((long) 1)
                    .item(Item.builder().name("item_" + i).build())
                    .status(AuctionStatus.SOLD)
                    .build();
            auctionRepository.save(auction);
        }

        //when
        mockMVC.perform(get("/api/auctions/seller")
                        .param("status", "sold")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10))
                        .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctions", Matchers.hasSize(5)));
    }

    @Test
    void findBySellerIdAndStatus_invalidStatus_returnsBadRequestAndError() throws Exception {
        mockMVC.perform(get("/api/auctions/seller")
                        .param("status", "xxx")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10))
                        .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid status"));
    }

    @Test
    void getBiddingHistory_noStatusProvided_returnsBiddingHistory() throws Exception {
        // given
        Bid bid = Bid.builder()
                .bidderId((long) 3)
                .price((double) 130)
                .createdAt(LocalDateTime.now())
                .build();
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 130)
                .status(AuctionStatus.ACTIVE)
                .endAt(LocalDateTime.now().plusDays(1))
                .bids(List.of(bid))
                .build();
        auctionRepository.save(auction);

        // when
        mockMVC.perform(get("/api/auctions/buyer")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10))
                        .header(USER_ID_HEADER_NAME, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctions", Matchers.hasSize(1)));
    }

    @Test
    void getBiddingHistory_statusIsActive_returnsBiddingHistory() throws Exception {
        // given
        auctionRepository.deleteAll();
        Bid bid = Bid.builder()
                .bidderId((long) 3)
                .price((double) 130)
                .createdAt(LocalDateTime.now())
                .build();
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 130)
                .status(AuctionStatus.ACTIVE)
                .endAt(LocalDateTime.now().plusDays(1))
                .bids(List.of(bid))
                .build();
        auctionRepository.save(auction);

        // when
        mockMVC.perform(get("/api/auctions/buyer")
                        .param("status", "active")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10))
                        .header(USER_ID_HEADER_NAME, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctions", Matchers.hasSize(1)));
    }

    @Test
    void getWonAuctions_returnsWonAuctions() throws Exception {
        // given
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 300)
                .status(AuctionStatus.SOLD)
                .lastBidderId((long) 3)
                .endAt(LocalDateTime.now().plusDays(1))
                .bids(new ArrayList<>())
                .build();
        auctionRepository.save(auction);

        // when
        mockMVC.perform(get("/api/auctions/buyer/won")
                .param("offset", String.valueOf(0))
                .param("page-size", String.valueOf(10))
                .header(USER_ID_HEADER_NAME, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctions", Matchers.hasSize(1)));
    }

    @Test
    void deleteAuction_auctionDoesNotExist_returnsBadRequest() throws Exception {
        // given
        auctionRepository.deleteAll();

        // when
        mockMVC.perform(delete("/api/auctions/1")
                .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuction_auctionHasBeenBidOn_returnsBadRequest() throws Exception {
        // given
        Bid bid = Bid.builder()
                .bidderId((long) 3)
                .price((double) 130)
                .createdAt(LocalDateTime.now())
                .build();
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 130)
                .status(AuctionStatus.ACTIVE)
                .endAt(LocalDateTime.now().plusDays(1))
                .bids(List.of(bid))
                .build();
        auctionRepository.save(auction);

        // when
        mockMVC.perform(delete("/api/auctions/"+ auction.getId())
                        .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value( "auction with id " + auction.getId() + " has been bid on"));
    }

    @Test
    void deleteAuction_invalidSeller_returnsBadRequest() throws Exception {
        // given
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 100)
                .status(AuctionStatus.ACTIVE)
                .endAt(LocalDateTime.now().plusDays(1))
                .bids(new ArrayList<>())
                .build();
        auctionRepository.save(auction);

        // when
        mockMVC.perform(delete("/api/auctions/"+ auction.getId())
                        .header(USER_ID_HEADER_NAME, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value( "Invalid user " + 2 + " attempts to edit/delete an auction"));
    }

    @Test
    void placeBid_bidPriceLowerThanCurrentPrice_returnsBadRequest() throws Exception {
        // given
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 100)
                .status(AuctionStatus.ACTIVE)
                .endAt(LocalDateTime.now().plusDays(1))
                .bids(new ArrayList<>())
                .build();
        auctionRepository.save(auction);

        PlaceBidRequest placeBidRequest = new PlaceBidRequest((double) 80);
        String placeBidRequestString = objectMapper.writeValueAsString(placeBidRequest);
        // when
        mockMVC.perform(post("/api/auctions/"+auction.getId()+"/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(placeBidRequestString)
                        .header(USER_ID_HEADER_NAME, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bid price must be greater than the current price."));
    }

    @Test
    void placeBid_auctionEndedOrSold_returnsBadRequest() throws Exception {
        // given
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 100)
                .status(AuctionStatus.ACTIVE)
                .endAt(LocalDateTime.now().minusMinutes(30))
                .bids(new ArrayList<>())
                .build();
        auctionRepository.save(auction);

        PlaceBidRequest placeBidRequest = new PlaceBidRequest((double) 150);
        String placeBidRequestString = objectMapper.writeValueAsString(placeBidRequest);
        // when
        mockMVC.perform(post("/api/auctions/"+auction.getId()+"/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(placeBidRequestString)
                        .header(USER_ID_HEADER_NAME, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("cannot place bids on ended auction."));
    }

    @Test
    void placeBid_sellerAttemptsToPlaceBid_returnsBadRequest() throws Exception {
        // given
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 100)
                .status(AuctionStatus.ACTIVE)
                .endAt(LocalDateTime.now().plusDays(1))
                .bids(new ArrayList<>())
                .build();
        auctionRepository.save(auction);

        PlaceBidRequest placeBidRequest = new PlaceBidRequest((double) 150);
        String placeBidRequestString = objectMapper.writeValueAsString(placeBidRequest);
        // when
        mockMVC.perform(post("/api/auctions/"+auction.getId()+"/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(placeBidRequestString)
                        .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("sellers cannot place bids on their own auctions."));
    }

    @Test
    void placeBid_validPrice_returnsSuccessfulAndPublishesEvent() throws Exception {
        setUpKafkaContainer();

        // given
        Bid bid = Bid.builder()
                .bidderId((long) 3)
                .price((double) 130)
                .createdAt(LocalDateTime.now())
                .build();
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 130)
                .status(AuctionStatus.ACTIVE)
                .endAt(LocalDateTime.now().plusDays(1))
                .bids(List.of(bid))
                .build();
        auctionRepository.save(auction);

        PlaceBidRequest placeBidRequest = new PlaceBidRequest((double) 150);
        String placeBidRequestString = objectMapper.writeValueAsString(placeBidRequest);
        // when
        mockMVC.perform(post("/api/auctions/"+auction.getId()+"/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(placeBidRequestString)
                .header(USER_ID_HEADER_NAME, 2))
                .andExpect(status().isOk());
        // then
        Auction updatedAuction =  auctionRepository.findById(auction.getId()).get();
        Assertions.assertEquals(placeBidRequest.getPrice(), updatedAuction.getCurrentPrice());

        // Read the message (BidPlacedEvent) with a test consumer from Kafka and assert its properties
        ConsumerRecord<String, String> message = auctionNotificationRecords.poll(200, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(message);
        BidPlacedEvent result = objectMapper.readValue(message.value(), BidPlacedEvent.class);
        Assertions.assertNotNull(result);
        assertEquals(updatedAuction.getItem().getName(), result.getAuctionName());
        assertEquals(updatedAuction.getSellerId(), result.getSellerId());
        assertEquals(updatedAuction.getCurrentPrice(), result.getCurrentPrice());
        assertEquals(1, result.getPreviousBidders().size());

        stopKafkaContainer();
    }

    @Test
    void endAuctions_noBids_publishAuctionFailedEvent() throws Exception {
        auctionRepository.deleteAll();
        setUpKafkaContainer();

        // given
        LocalDateTime currTime = LocalDateTime.now();
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 130)
                .status(AuctionStatus.ACTIVE)
                .endAt(currTime)
                .bids(new ArrayList<>())
                .build();
        auctionRepository.save(auction);
        // when
        auctionService.endAuctions(currTime);
        // then
        Auction updatedAuction = auctionRepository.findById(auction.getId()).get();
        ConsumerRecord<String, String> message = auctionNotificationRecords.poll(200, TimeUnit.MILLISECONDS);
        assertEquals(updatedAuction.getStatus(), AuctionStatus.FAILED);
        Assertions.assertNotNull(message);
        AuctionFailedEvent result = objectMapper.readValue(message.value(), AuctionFailedEvent.class);
        Assertions.assertNotNull(result);
        assertEquals(updatedAuction.getItem().getName(), result.getAuctionName());


        stopKafkaContainer();
    }

    @Test
    void endAuctions_currentPriceLowerThanReservePrice_publishAuctionFailedEvent() throws Exception {
        auctionRepository.deleteAll();
        setUpKafkaContainer();

        // given
        LocalDateTime currTime = LocalDateTime.now();
        Bid bid = Bid.builder()
                .bidderId((long) 3)
                .price((double) 130)
                .createdAt(LocalDateTime.now())
                .build();
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 130)
                .status(AuctionStatus.ACTIVE)
                .endAt(currTime)
                .lastBidderId((long) 3)
                .bids(List.of(bid))
                .build();
        auctionRepository.save(auction);
        // when
        auctionService.endAuctions(currTime);
        // then
        Auction updatedAuction = auctionRepository.findById(auction.getId()).get();
        ConsumerRecord<String, String> message = auctionNotificationRecords.poll(200, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(message);
        AuctionFailedEvent result = objectMapper.readValue(message.value(), AuctionFailedEvent.class);
        Assertions.assertNotNull(result);
        assertEquals(updatedAuction.getItem().getName(), result.getAuctionName());
        assertEquals(1, result.getBidders().size());
        assertEquals(updatedAuction.getStatus(), AuctionStatus.FAILED);

        stopKafkaContainer();
    }

    @Test
    void endAuctions_currentPriceHigherThanReservePrice_publishWinnerDeterminedEvent() throws Exception {
        auctionRepository.deleteAll();
        setUpKafkaContainer();

        // given
        LocalDateTime currTime = LocalDateTime.now();
        Bid bid1 = Bid.builder()
                .bidderId((long) 2)
                .price((double) 150)
                .createdAt(currTime.minusMinutes(10))
                .build();
        Bid bid2 = Bid.builder()
                .bidderId((long) 3)
                .price((double) 300)
                .createdAt(currTime.minusMinutes(5))
                .build();
        Auction auction = Auction.builder()
                .sellerId((long) 1)
                .item(Item.builder().name("item").build())
                .startPrice((double) 100)
                .reservePrice((double) 200)
                .currentPrice((double) 300)
                .status(AuctionStatus.ACTIVE)
                .endAt(currTime)
                .lastBidderId((long) 3)
                .bids(List.of(bid1, bid2))
                .build();
        auctionRepository.save(auction);
        // when
        auctionService.endAuctions(currTime);
        // then
        Auction updatedAuction = auctionRepository.findById(auction.getId()).get();
        ConsumerRecord<String, String> auctionMessage = auctionNotificationRecords.poll(200, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(auctionMessage);
        WinnerDeterminedEvent result = objectMapper.readValue(auctionMessage.value(), WinnerDeterminedEvent.class);
        Assertions.assertNotNull(result);
        assertEquals(1, result.getPreviousBidders().size());
        assertEquals(updatedAuction.getLastBidderId(), result.getWinnerId());
        assertEquals(updatedAuction.getStatus(), AuctionStatus.SOLD);

        stopKafkaContainer();
    }

    private void setUpKafkaContainer() {
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties = new ContainerProperties(AUCTION_NOTIFICATION_KAFKA_TOPIC_NAME);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        auctionNotificationRecords = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) auctionNotificationRecords::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    private Map<String, Object> getConsumerProperties() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "consumer",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false", //Consumers won’t have a committed offset for that partition, and they will read from the beginning no matter what.
//                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true",
//                ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10",
//                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }

    private void stopKafkaContainer() {
        container.stop();
    }
}