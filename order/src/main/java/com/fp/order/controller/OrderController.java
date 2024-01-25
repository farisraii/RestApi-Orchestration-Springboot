package com.fp.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fp.order.model.ApiModel;
import com.fp.order.model.NotifModel;
import com.fp.order.model.OrderModel;
import com.fp.order.services.OrderService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderApiService;

    @PostMapping("/orders")
    public Mono<ApiModel> createOrder(@RequestBody OrderModel order) {
        return orderApiService.sendMessage(order);
    }

    @GetMapping("/status/{orderId}")
    public Mono<NotifModel> getStatus(@PathVariable("orderId") Integer orderId) {
        return orderApiService.getStatus(orderId);
    }

}
