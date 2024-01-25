package com.fp.order.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.fp.order.model.NotifModel;

import reactor.core.publisher.Mono;

public interface NotifRepository extends R2dbcRepository <NotifModel, Integer>{
    Mono<NotifModel> findByOrderId(Integer orderId);
}
