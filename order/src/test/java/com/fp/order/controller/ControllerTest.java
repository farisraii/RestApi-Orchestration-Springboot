package com.fp.order.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fp.order.model.ApiModel;
import com.fp.order.model.NotifModel;
import com.fp.order.model.OrderModel;
import com.fp.order.services.OrderService;


@WebMvcTest(OrderController.class)
public class ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OrderService orderApiService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void testCreateOrder() throws Exception {
        OrderModel order = new OrderModel();

        when(orderApiService.sendMessage(any(OrderModel.class))).thenReturn(Mono.just(new ApiModel()));

        mockMvc.perform(post("/api/orders")
                .contentType("application/json")
                .content(String.valueOf(new ApiModel())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
        // Verifying that the sendMessage method is called
        verify(orderApiService, times(1)).sendMessage(order);
    }

    @Test
    void testGetStatus() throws Exception {
        Integer orderId = 1;

        // Mocking the behavior of orderApiService
        when(orderApiService.getStatus(orderId)).thenReturn(Mono.just(new NotifModel()));

        // Performing the mockMvc request
        mockMvc.perform(get("/api/status/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // Verifying that the getStatus method is called
        verify(orderApiService, times(1)).getStatus(orderId);
    }
}
