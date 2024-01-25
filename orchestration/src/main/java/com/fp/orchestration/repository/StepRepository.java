package com.fp.orchestration.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.fp.orchestration.model.StepModel;

import reactor.core.publisher.Flux;

public interface StepRepository extends R2dbcRepository <StepModel, Integer> {
    Flux<StepModel> findAllByAction(Integer actionId);
}
