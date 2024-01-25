package com.fp.order.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.fp.order.model.ActionModel;

import reactor.core.publisher.Mono;

public interface ActionRepository extends R2dbcRepository<ActionModel, Integer>{
    Mono<ActionModel> findByAction(String action);
}
