package com.fp.order.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.fp.order.model.ApiModel;
import com.fp.order.model.NotifModel;
import com.fp.order.model.OrderModel;
import com.fp.order.model.TrxModel;
import com.fp.order.repository.ActionRepository;
import com.fp.order.repository.NotifRepository;
import com.fp.order.repository.StepRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OrderService {
    final JmsTemplate jmsTemplate;
    final R2dbcEntityTemplate r2dbcEntityTemplate;
    final StepRepository stepRepository;
    final ActionRepository actionRepository;
    final NotifRepository notifRepository;

    @Autowired
    public OrderService(JmsTemplate jmsTemplate, R2dbcEntityTemplate r2dbcEntityTemplate, StepRepository stepRepository, ActionRepository actionRepository, NotifRepository notifRepository) {
        this.jmsTemplate = jmsTemplate;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.stepRepository = stepRepository;
        this.actionRepository = actionRepository;
        this.notifRepository = notifRepository;
    }

    public Mono<ApiModel> sendMessage(OrderModel order) {
        try {
            ApiModel orderApi = new ApiModel();
            orderApi.setAction(order.getAction());
            return r2dbcEntityTemplate.insert(orderApi)
                .doOnSuccess(orderSaved -> {
                    log.info(" Order: {}", orderSaved);
                    log.info(" OrderId: {}", orderSaved.getId());
                    log.info(" Action: {}", orderSaved.getAction());
                    actionRepository.findByAction(orderSaved.getAction())
                        .doOnSuccess(action -> {
                            log.info("asdasd : {}", action.getAction());
                            stepRepository.findAllByAction(action.getId())
                                .subscribe(steps -> {
                                    TrxModel trx = new TrxModel();
                                    trx.setOrder_id(orderSaved.getId());
                                    trx.setAction_id(action.getId());
                                    trx.setStep(steps.getStep());
                                    trx.setPriority(steps.getPriority());
                                    trx.setStatus("INIT");
                                    r2dbcEntityTemplate.insert(trx).subscribe();
                                });
                            order.setOrderId(orderSaved.getId());
                            jmsTemplate.convertAndSend("queue.orders", order);
                        })
                        .doOnError(err -> {
                            log.info("err");
                        }).subscribe();
                });
        } catch (Exception e) {
            log.error("Error sendMessage try catch : {}", e.getMessage());
            return Mono.error(e.getCause());
        }
    }
    public Mono<NotifModel> getStatus(Integer orderId) {
        return notifRepository.findByOrderId(orderId)
                .doOnSuccess(found -> log.info("get status order : {}", found));
    }
}
