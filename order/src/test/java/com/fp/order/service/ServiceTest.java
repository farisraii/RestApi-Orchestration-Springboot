package com.fp.order.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;

import com.fp.order.model.ActionModel;
import com.fp.order.model.ApiModel;
import com.fp.order.model.NotifModel;
import com.fp.order.model.OrderModel;
import com.fp.order.model.StepModel;
import com.fp.order.model.TrxModel;
import com.fp.order.repository.ActionRepository;
import com.fp.order.repository.NotifRepository;
import com.fp.order.repository.StepRepository;
import com.fp.order.services.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class ServiceTest {
    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private NotifRepository notifRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void testSendMessage() {
        OrderModel order = new OrderModel();
        order.setAction("REGISTER");

        ApiModel apiModel = new ApiModel();
        apiModel.setId(1);
        apiModel.setAction(order.getAction());

        ActionModel actionModel = new ActionModel();
        actionModel.setId(1);
        actionModel.setAction(order.getAction());

        StepModel stepModel1 = new StepModel();
        stepModel1.setId(1);
        stepModel1.setActionId(1);
        stepModel1.setStep("queue.crm.register");
        stepModel1.setPriority(0);

        StepModel stepModel2 = new StepModel();
        stepModel2.setId(2);
        stepModel2.setActionId(1);
        stepModel2.setStep("queue.notif");
        stepModel2.setPriority(1);

        StepModel stepModel3 = new StepModel();
        stepModel3.setId(1);
        stepModel3.setActionId(1);
        stepModel3.setStep("queue.complete");
        stepModel3.setPriority(0);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(1);
        trxModel.setOrder_id(1);
        trxModel.setAction_id(1);
        trxModel.setStep("queue.crm.register");
        trxModel.setPriority(0);
        trxModel.setStatus("INIT");

        when(r2dbcEntityTemplate.insert(Mockito.any(ApiModel.class))).thenReturn(Mono.just(apiModel));
        when(actionRepository.findByAction(order.getAction())).thenReturn(Mono.just(actionModel));
        when(stepRepository.findAllByAction(actionModel.getId())).thenReturn(Flux.just(stepModel1,stepModel2, stepModel3));
        when(r2dbcEntityTemplate.insert(trxModel)).thenReturn(Mono.just(trxModel));

        StepVerifier.create(orderService.sendMessage(order))
                .expectNextCount(1)
                .verifyComplete();

        Mockito.verify(jmsTemplate, times(1)).convertAndSend(eq("queue.orders"), eq(order));
    }

    @Test
    public void testSendMessageGetActionError() {
        OrderModel order = new OrderModel();
        order.setAction("REGISTER");

        ApiModel apiModel = new ApiModel();
        apiModel.setId(1);
        apiModel.setAction(order.getAction());

        ActionModel actionModel = new ActionModel();
        actionModel.setId(1);
        actionModel.setAction(order.getAction());

        StepModel stepModel1 = new StepModel();
        stepModel1.setId(1);
        stepModel1.setActionId(1);
        stepModel1.setStep("queue.crm.register");
        stepModel1.setPriority(0);

        StepModel stepModel2 = new StepModel();
        stepModel2.setId(2);
        stepModel2.setActionId(1);
        stepModel2.setStep("queue.notif");
        stepModel2.setPriority(1);

        StepModel stepModel3 = new StepModel();
        stepModel3.setId(1);
        stepModel3.setActionId(1);
        stepModel3.setStep("queue.complete");
        stepModel3.setPriority(0);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(1);
        trxModel.setOrder_id(1);
        trxModel.setAction_id(1);
        trxModel.setStep("queue.crm.register");
        trxModel.setPriority(0);
        trxModel.setStatus("INIT");

        when(r2dbcEntityTemplate.insert(Mockito.any(ApiModel.class))).thenReturn(Mono.just(apiModel));
        when(actionRepository.findByAction(order.getAction())).thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(orderService.sendMessage(order))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void testSendMessageWithError() {
        OrderModel order = new OrderModel();
        order.setAction("someAction");

        Mockito.when(r2dbcEntityTemplate.insert(order))
                .thenThrow(new RuntimeException());
        orderService.sendMessage(order).subscribe();

        Mockito.verify(orderService, Mockito.times(1)).sendMessage(order);
    }

    @Test
    public void testGetStatus() {
        int orderId = 1;

        NotifModel notif = new NotifModel();
        notif.setOrderId(orderId);

        when(notifRepository.findByOrderId(orderId))
                .thenReturn(Mono.just(notif));

        StepVerifier.create(orderService.getStatus(orderId))
                .expectNext(notif)
                .verifyComplete();
    }
}
