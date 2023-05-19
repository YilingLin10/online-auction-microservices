package com.yilinglin10.orderservice.controller;

import com.yilinglin10.orderservice.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yilinglin10.orderservice.service.OrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getOrder(@PathVariable Long id, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("order", orderService.getOrder(id, Long.parseLong(userId)));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateOrderStatus(@PathVariable Long id, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        String result = orderService.updateOrderStatus(id, Long.parseLong(userId));
        response.put("response", result);
        return ResponseEntity.status(result.equals("successful") ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping
    public ResponseEntity<Object> getOrderList(@RequestParam(name = "status", required = false) List<String> statusList, @RequestParam("offset") Integer offset, @RequestParam("page-size") Integer pageSize, @RequestHeader("loggedInUser") String userId) {
        Map<String, Object> response = new HashMap<>();
        List<OrderResponse> orders = (statusList == null) ? orderService.findByUserId(Long.parseLong(userId), offset, pageSize) : orderService.findByUserIdAndStatusIn(statusList, Long.parseLong(userId), offset, pageSize);
        response.put("orders", orders);
        return ResponseEntity.ok(response);
    }
}
