package com.fp.orchestration.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.fp.orchestration.model.ActionModel;

import reactor.core.publisher.Mono;

public interface ActionRepository extends R2dbcRepository<ActionModel, Integer>{
    Mono<ActionModel> findByAction(String action);
}
