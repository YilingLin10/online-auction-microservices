package com.yilinglin10.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yilinglin10.orderservice.event.consume.WinnerDeterminedEvent;
import com.yilinglin10.orderservice.event.publish.OrderFulfilledOrShippingDueEvent;
import com.yilinglin10.orderservice.event.publish.OrderStatusUpdatedEvent;
import com.yilinglin10.orderservice.model.Order;
import com.yilinglin10.orderservice.model.OrderStatus;
import com.yilinglin10.orderservice.repository.OrderRepository;
import com.yilinglin10.orderservice.schedule.ScheduledTasks;
import com.yilinglin10.orderservice.service.OrderService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EmbeddedKafka
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@AutoConfigureMockMvc
@Testcontainers
@MockBean(ScheduledTasks.class)
@ActiveProfiles("test")
public class OrderServiceApplicationIntegrationTest {
    private static final String USER_ID_HEADER_NAME = "loggedInUser";
    private static final String ORDER_NOTIFICATION_KAFKA_TOPIC_NAME = "orderNotificationTopic";
    private static final String ORDER_REVIEW_KAFKA_TOPIC_NAME = "orderReviewTopic";
    private static final String ORDER_PLACEMENT_KAFKA_TOPIC_NAME = "orderPlacementTopic";

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0-debian");
    @Autowired
    private MockMvc mockMVC;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderService orderService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    private BlockingQueue<ConsumerRecord<String, String>> records;
    private KafkaMessageListenerContainer<String, String> container;
    private KafkaTemplate<String, Object> producer;


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.driverClassName", () -> mySQLContainer.getDriverClassName());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
    }

    @Test
    void getOrder_validUserAndOrderExists_returnsOrder() throws Exception {
        // given
        Order order = Order.builder()
                .auctionId((long)1)
                .sellerId((long)1)
                .buyerId((long)2)
                .status(OrderStatus.AWAITING_PAYMENT)
                .build();
        orderRepository.save(order);

        //when
        mockMVC.perform(get("/api/orders/"+order.getId())
                .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order").exists());
    }

    @Test
    void getOrder_invalidUser_returnsBadRequestAndError() throws Exception {
        // given
        Order order = Order.builder()
                .auctionId((long)1)
                .sellerId((long)1)
                .buyerId((long)2)
                .status(OrderStatus.AWAITING_PAYMENT)
                .build();
        orderRepository.save(order);

        //when
        mockMVC.perform(get("/api/orders/"+order.getId())
                        .header(USER_ID_HEADER_NAME, 3))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User is not allowed to access order with id "+ order.getId()));
    }

    @Test
    void getOrder_orderDoesNotExist_returnsBadRequestAndError() throws Exception {
        // given
        orderRepository.deleteAll();

        //when
        mockMVC.perform(get("/api/orders/"+1)
                        .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderList_statusNotProvided_returnsAllOrders() throws Exception {
        // given
        for (int i=0; i<5; i++) {
            Order order = Order.builder()
                    .auctionId((long)i)
                    .sellerId((long)1)
                    .buyerId((long)2)
                    .status(OrderStatus.AWAITING_PAYMENT)
                    .createdAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
        }

        // when
        mockMVC.perform(get("/api/orders")
                .param("offset", String.valueOf(0))
                .param("page-size", String.valueOf(10))
                .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders", Matchers.hasSize(5)));
    }

    @Test
    void getOrderList_statusProvided_returnsOrderList() throws Exception {
        // given
        for (int i=0; i<5; i++) {
            Order order = Order.builder()
                    .auctionId((long)i)
                    .sellerId((long)1)
                    .buyerId((long)2)
                    .status(OrderStatus.AWAITING_PAYMENT)
                    .createdAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
        }
        for (int i=6; i<=10; i++) {
            Order order = Order.builder()
                    .auctionId((long)i)
                    .sellerId((long)2)
                    .buyerId((long)1)
                    .status(OrderStatus.AWAITING_SHIPPING)
                    .createdAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
        }

        // when
        mockMVC.perform(get("/api/orders")
                        .param("status", "awaiting_payment","awaiting_shipping")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10))
                        .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders", Matchers.hasSize(10)))
                .andDo(print());
    }

    @Test
    void getOrderList_invalidStatus_returnsBadRequest() throws Exception {
        // given
        for (int i=0; i<5; i++) {
            Order order = Order.builder()
                    .auctionId((long)i)
                    .sellerId((long)1)
                    .buyerId((long)2)
                    .status(OrderStatus.AWAITING_PAYMENT)
                    .createdAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
        }

        // when
        mockMVC.perform(get("/api/orders")
                        .param("status", "xxx")
                        .param("offset", String.valueOf(0))
                        .param("page-size", String.valueOf(10))
                        .header(USER_ID_HEADER_NAME, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid status"));
    }

    @Test
    void updateOrderStatus_validSellerAndStatusIsAwaitingPayment_returnSuccessfulAndPublishOrderStatusUpdatedEvent() throws Exception {
        setUpKafkaContainer(ORDER_NOTIFICATION_KAFKA_TOPIC_NAME);
        // given
        Order order = Order.builder()
                .auctionId((long)1)
                .auctionName("iPhone 13")
                .sellerId((long)1)
                .buyerId((long)2)
                .status(OrderStatus.AWAITING_PAYMENT)
                .build();
        orderRepository.save(order);
        // when
        mockMVC.perform(put("/api/orders/"+order.getId())
                .header(USER_ID_HEADER_NAME, order.getSellerId()))
                .andExpect(status().isOk());
        // then
        Order updatedOrder = orderRepository.findById(order.getId()).get();
        assertEquals(OrderStatus.AWAITING_SHIPPING, updatedOrder.getStatus());

        ConsumerRecord<String, String> message = records.poll(200, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(message);
        OrderStatusUpdatedEvent orderStatusUpdatedEvent = objectMapper.readValue(message.value(), OrderStatusUpdatedEvent.class);
        Assertions.assertNotNull(orderStatusUpdatedEvent);
        assertEquals(updatedOrder.getAuctionName(), orderStatusUpdatedEvent.getAuctionName());
        assertEquals(updatedOrder.getStatus().getCode(), orderStatusUpdatedEvent.getStatus());

        stopKafkaContainer();
    }

    @Test
    void updateOrderStatus_validBuyerAndStatusIsAwaitingShipping_returnsSuccessfulAndPublishOrderFulfilledOrShippingDueEvent() throws Exception {
        orderRepository.deleteAll();
        setUpKafkaContainer(ORDER_REVIEW_KAFKA_TOPIC_NAME);
        // given
        Order order = Order.builder()
                .auctionId((long)1)
                .auctionName("iPhone 13 MAX")
                .sellerId((long)1)
                .buyerId((long)2)
                .status(OrderStatus.AWAITING_SHIPPING)
                .build();
        orderRepository.save(order);
        // when
        mockMVC.perform(put("/api/orders/"+order.getId())
                        .header(USER_ID_HEADER_NAME, order.getBuyerId()))
                .andExpect(status().isOk());
        // then
        Order updatedOrder = orderRepository.findById(order.getId()).get();
        assertEquals(OrderStatus.FULFILLED, updatedOrder.getStatus());

        ConsumerRecord<String, String> message = records.poll(200, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(message);
        OrderFulfilledOrShippingDueEvent orderFulfilledOrShippingDueEvent = objectMapper.readValue(message.value(), OrderFulfilledOrShippingDueEvent.class);
        Assertions.assertNotNull(orderFulfilledOrShippingDueEvent);
        assertEquals(updatedOrder.getId(), orderFulfilledOrShippingDueEvent.getOrderId());

        stopKafkaContainer();
    }

    @Test
    void updateOrderStatus_validSellerAndStatusIsAwaitingShipping_returnsBadRequest() throws Exception {
        // given
        Order order = Order.builder()
                .auctionId((long)1)
                .auctionName("iPhone 13")
                .sellerId((long)1)
                .buyerId((long)2)
                .status(OrderStatus.AWAITING_SHIPPING)
                .build();
        orderRepository.save(order);
        // when
        mockMVC.perform(put("/api/orders/"+order.getId())
                        .header(USER_ID_HEADER_NAME, order.getSellerId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response").value("cannot update order status"));
    }

    @Test
    void updateOrderStatus_validSellerAndStatusIsPaymentDue_returnBadRequest() throws Exception {
        // given
        Order order = Order.builder()
                .auctionId((long)1)
                .auctionName("iPhone 13")
                .sellerId((long)1)
                .buyerId((long)2)
                .status(OrderStatus.PAYMENT_DUE)
                .build();
        orderRepository.save(order);
        // when
        mockMVC.perform(put("/api/orders/"+order.getId())
                        .header(USER_ID_HEADER_NAME, order.getSellerId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("order with id " + order.getId() + " is in status " + order.getStatus().getCode()));
    }

    @Test
    void updateOrderStatus_invalidUser_returnBadRequest() throws Exception {
        // given
        Order order = Order.builder()
                .auctionId((long)1)
                .auctionName("iPhone 13")
                .sellerId((long)1)
                .buyerId((long)2)
                .status(OrderStatus.AWAITING_SHIPPING)
                .build();
        orderRepository.save(order);
        // when
        mockMVC.perform(put("/api/orders/"+order.getId())
                        .header(USER_ID_HEADER_NAME, 3))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User is not allowed to access order with id "+order.getId()));
    }

    @Test
    void checkOrderDeadline_PaymentDue_publishOrderStatusUpdatedEvent() throws Exception {
        orderRepository.deleteAll();
        setUpKafkaContainer(ORDER_NOTIFICATION_KAFKA_TOPIC_NAME);
        // given
        LocalDateTime timestamp = LocalDateTime.now();
        Order order = Order.builder()
                .auctionId((long)1)
                .auctionName("iPhone 13")
                .sellerId((long)1)
                .buyerId((long)2)
                .status(OrderStatus.AWAITING_PAYMENT)
                .paymentDeadline(timestamp.minusMinutes(1))
                .shippingDeadline(timestamp.plusDays(3))
                .build();
        orderRepository.save(order);

        // when
        orderService.checkOrderDeadline(timestamp);

        // then
        Order updatedOrder = orderRepository.findById(order.getId()).get();
        assertEquals(OrderStatus.PAYMENT_DUE, updatedOrder.getStatus());

        ConsumerRecord<String, String> message = records.poll(200, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(message);
        OrderStatusUpdatedEvent orderStatusUpdatedEvent = objectMapper.readValue(message.value(), OrderStatusUpdatedEvent.class);
        Assertions.assertNotNull(orderStatusUpdatedEvent);
        assertEquals(updatedOrder.getAuctionName(), orderStatusUpdatedEvent.getAuctionName());
        assertEquals(updatedOrder.getStatus().getCode(), orderStatusUpdatedEvent.getStatus());

        stopKafkaContainer();
    }

    @Test
    void checkOrderDeadline_ShippingDue_publishOrderFulfilledOrShippingDueEvent() throws Exception {
        orderRepository.deleteAll();
        setUpKafkaContainer(ORDER_REVIEW_KAFKA_TOPIC_NAME);
        // given
        LocalDateTime timestamp = LocalDateTime.now();
        Order order = Order.builder()
                .auctionId((long)1)
                .auctionName("iPhone 13 Pro")
                .sellerId((long)1)
                .buyerId((long)2)
                .status(OrderStatus.AWAITING_SHIPPING)
                .paymentDeadline(timestamp.minusDays(3))
                .shippingDeadline(timestamp.minusMinutes(1))
                .build();
        orderRepository.save(order);

        // when
        orderService.checkOrderDeadline(timestamp);

        // then
        Order updatedOrder = orderRepository.findById(order.getId()).get();
        assertEquals(OrderStatus.SHIPPING_DUE, updatedOrder.getStatus());

        ConsumerRecord<String, String> message = records.poll(200, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(message);
        OrderFulfilledOrShippingDueEvent orderFulfilledOrShippingDueEvent = objectMapper.readValue(message.value(), OrderFulfilledOrShippingDueEvent.class);
        Assertions.assertNotNull(orderFulfilledOrShippingDueEvent);
        assertEquals(updatedOrder.getId(), orderFulfilledOrShippingDueEvent.getOrderId());

        stopKafkaContainer();
    }

    @Test
    void handleWinnerDeterminedEvent_placesOrder() throws Exception {
        orderRepository.deleteAll();
        setUpKafkaProducer();
        // Given
        WinnerDeterminedEvent winnerDeterminedEvent = WinnerDeterminedEvent.builder()
                        .id(UUID.randomUUID())
                        .auctionName("MacBook Pro")
                        .auctionId((long) 3)
                        .sellerId((long) 2)
                        .winnerId((long) 4)
                        .timestamp(LocalDateTime.now())
                        .previousBidders(new ArrayList<>())
                        .build();
        producer.send(ORDER_PLACEMENT_KAFKA_TOPIC_NAME, winnerDeterminedEvent);

        // then
        sleep(500);
        assertTrue(orderRepository.findByAuctionId(winnerDeterminedEvent.getAuctionId()).isPresent());
    }

    private void setUpKafkaContainer(String topic) {
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties = new ContainerProperties(topic);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    private Map<String, Object> getConsumerProperties() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "consumer",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false", //Consumers won’t have a committed offset for that partition, and they will read from the beginning no matter what.
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
//                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
    }

    private void stopKafkaContainer() {
        container.stop();
    }

    private void setUpKafkaProducer() {
        DefaultKafkaProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(getProducerProperties());
        producer = new KafkaTemplate<>(producerFactory);
    }

    private Map<String, Object> getProducerProperties() {
        return Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
    }

}
