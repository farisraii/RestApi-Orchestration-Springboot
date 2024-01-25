package com.fp.orchestration.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.fp.orchestration.model.TrxModel;

import reactor.core.publisher.Mono;

@Repository
public interface TrxRepository extends R2dbcRepository<TrxModel, Integer> {
    @Query("SELECT * FROM trx_steps WHERE order_id =:orderId AND status ='INIT' " +
            "AND priority = (select min(priority) from trx_steps where order_id=:orderId and status = 'INIT')")
    Mono<TrxModel> findStatus(Integer orderId);

    @Query("SELECT * FROM trx_steps WHERE order_id =:orderId AND status ='COMPLETED' " +
            "AND priority = (select max(priority) from trx_steps where order_id=:orderId and status = 'COMPLETED')")
    Mono<TrxModel> statusRollback(Integer orderId);

    Mono<TrxModel> orderIdAndPriority(Integer orderId, Integer priority);
}
