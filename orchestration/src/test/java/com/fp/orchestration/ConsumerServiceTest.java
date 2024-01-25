package com.fp.orchestration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import com.fp.orchestration.model.DataModel;
import com.fp.orchestration.model.OrderModel;
import com.fp.orchestration.model.ResponseCrmModel;
import com.fp.orchestration.model.ResponseNotifModel;
import com.fp.orchestration.model.TrxModel;
import com.fp.orchestration.repository.TrxRepository;
import com.fp.orchestration.services.ConsumerService;
import com.fp.orchestration.services.OrchestrationService;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class ConsumerServiceTest {
    @Mock
    private TrxRepository trxRepository;

    @Mock
    private OrchestrationService orchestrationService;

    @InjectMocks
    private ConsumerService consumerService;

    @Test
    void testProcessOrder() {
        OrderModel order = new OrderModel();
        order.setOrderId(1);
        order.setAction("REGISTER");
        order.setData("data");

        Message<OrderModel> message = mock(Message.class);
        when(message.getPayload()).thenReturn(order);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(1);
        trxModel.setOrderId(1);
        trxModel.setActionId(1);
        trxModel.setStep("queue.crm.register");
        trxModel.setPriority(0);
        trxModel.setStatus("INIT");

        when(trxRepository.findStatus(order.getOrderId())).thenReturn(Mono.just(trxModel));
        doNothing().when(orchestrationService).sendMessage(anyString(), any(DataModel.class));

        consumerService.processOrder(message);

        verify(trxRepository).findStatus(order.getOrderId());
        verify(orchestrationService).sendMessage(anyString(), any(DataModel.class));
    }

    @Test
    void testStatusRegisterUserApproved() {
        ResponseCrmModel responseCrmModel = new ResponseCrmModel();
        responseCrmModel.setOrderId(1);
        responseCrmModel.setPriority(0);
        responseCrmModel.setId("abc123");
        responseCrmModel.setName("crm");
        responseCrmModel.setUsername("crm");
        responseCrmModel.setPassword("crm");
        responseCrmModel.setBalance(50000);
        responseCrmModel.setStatus(true);
        responseCrmModel.setUserStatus("USER_APPROVED");

        Message<ResponseCrmModel> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseCrmModel);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(1);
        trxModel.setActionId(1);
        trxModel.setOrderId(1);
        trxModel.setStep("queue.crm.register");
        trxModel.setPriority(0);
        trxModel.setStatus("INIT");

        TrxModel transaksiUpdateStep = new TrxModel();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.crm.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("COMPLETED");

        TrxModel transaksiNextStep = new TrxModel();
        transaksiNextStep.setId(1);
        transaksiNextStep.setActionId(1);
        transaksiNextStep.setOrderId(1);
        transaksiNextStep.setStep("queue.notif");
        transaksiNextStep.setPriority(1);
        transaksiNextStep.setStatus("INIT");

        when(trxRepository.findStatus(any())).thenReturn(Mono.just(trxModel));
        when(trxRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        doNothing().when(orchestrationService).sendMessage(anyString(), any(DataModel.class));

        consumerService.statusRegister(message);

        verify(trxRepository, times(2)).findStatus(any());
        verify(trxRepository).save(transaksiUpdateStep);
        verify(orchestrationService).sendMessage(anyString(), any(DataModel.class));
    }

    @Test
    void testStatusRegisterUserRejected() {
        ResponseCrmModel responseCrmModel = new ResponseCrmModel();
        responseCrmModel.setUserStatus("USER_REJECTED");
        responseCrmModel.setOrderId(1);
        responseCrmModel.setPriority(0);

        Message<ResponseCrmModel> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseCrmModel);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(1);
        trxModel.setActionId(1);
        trxModel.setOrderId(1);
        trxModel.setStep("queue.crm.register");
        trxModel.setPriority(0);
        trxModel.setStatus("INIT");

        TrxModel transaksiUpdateStep = new TrxModel();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.crm.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("CANCELED");

        TrxModel transaksiRollbackStep = new TrxModel();
        transaksiRollbackStep.setId(1);
        transaksiRollbackStep.setActionId(1);
        transaksiRollbackStep.setOrderId(1);
        transaksiRollbackStep.setStep("queue.crm.register");
        transaksiRollbackStep.setPriority(0);
        transaksiRollbackStep.setStatus("CANCELED");

        when(trxRepository.orderIdAndPriority(1, 0)).thenReturn(Mono.just(trxModel));
        when(trxRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(trxRepository.statusRollback(1)).thenReturn(Mono.just(transaksiRollbackStep));
        doNothing().when(orchestrationService).sendMessage(anyString(), any(DataModel.class));

        consumerService.statusRegister(message);

        verify(trxRepository).orderIdAndPriority(1, 0);
        verify(trxRepository).save(transaksiUpdateStep);
        verify(trxRepository).statusRollback(1);
        verify(orchestrationService).sendMessage(anyString(), any(DataModel.class));
    }

    @Test
    void testStatusRegisterUserRejectedNull() {
        ResponseCrmModel responseCrmModel = new ResponseCrmModel();
        responseCrmModel.setUserStatus("USER_REJECTED");
        responseCrmModel.setOrderId(1);
        responseCrmModel.setPriority(0);

        Message<ResponseCrmModel> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseCrmModel);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(1);
        trxModel.setActionId(1);
        trxModel.setOrderId(1);
        trxModel.setStep("queue.crm.register");
        trxModel.setPriority(0);
        trxModel.setStatus("INIT");

        TrxModel transaksiUpdateStep = new TrxModel();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.crm.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("CANCELED");

        TrxModel transaksiRollbackStep = new TrxModel();
        transaksiRollbackStep.setId(1);
        transaksiRollbackStep.setActionId(1);
        transaksiRollbackStep.setOrderId(1);
        transaksiRollbackStep.setStep("queue.crm.register");
        transaksiRollbackStep.setPriority(0);
        transaksiRollbackStep.setStatus("CANCELED");

        when(trxRepository.orderIdAndPriority(1, 0)).thenReturn(Mono.just(trxModel));
        when(trxRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(trxRepository.statusRollback(1)).thenReturn(Mono.empty());

        consumerService.statusRegister(message);

        verify(trxRepository).orderIdAndPriority(1, 0);
        verify(trxRepository).save(transaksiUpdateStep);
        verify(trxRepository).statusRollback(1);
    }

    @Test
    void testRollbackRegister() {
        ResponseCrmModel responseCrmModel = new ResponseCrmModel();
        responseCrmModel.setUserStatus("ROLLBACKED");
        responseCrmModel.setOrderId(1);
        responseCrmModel.setPriority(0);

        Message<ResponseCrmModel> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseCrmModel);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(1);
        trxModel.setActionId(1);
        trxModel.setOrderId(1);
        trxModel.setStep("queue.crm.register");
        trxModel.setPriority(0);
        trxModel.setStatus("COMPLETED");

        TrxModel transaksiUpdateStep = new TrxModel();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.crm.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("CANCELED");

        TrxModel transaksiNextRollbackStep = new TrxModel();
        transaksiNextRollbackStep.setId(1);
        transaksiNextRollbackStep.setActionId(1);
        transaksiNextRollbackStep.setOrderId(1);
        transaksiNextRollbackStep.setStep("queue.crm.register");
        transaksiNextRollbackStep.setPriority(0);
        transaksiNextRollbackStep.setStatus("COMPLETED");

        when(trxRepository.orderIdAndPriority(1, 0)).thenReturn(Mono.just(trxModel));
        when(trxRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(trxRepository.statusRollback(1)).thenReturn(Mono.just(transaksiNextRollbackStep));
        doNothing().when(orchestrationService).sendMessage(anyString(), any(DataModel.class));

        consumerService.rollbackRegister(message);

        verify(trxRepository).orderIdAndPriority(1, 0);
        verify(trxRepository).save(transaksiUpdateStep);
        verify(trxRepository).statusRollback(1);
        verify(orchestrationService).sendMessage(anyString(), any(DataModel.class));
    }

    @Test
    void testRollbackRegisterNextNull() {
        ResponseCrmModel responseCrmModel = new ResponseCrmModel();
        responseCrmModel.setUserStatus("ROLLBACKED");
        responseCrmModel.setOrderId(1);
        responseCrmModel.setPriority(0);

        Message<ResponseCrmModel> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseCrmModel);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(1);
        trxModel.setActionId(1);
        trxModel.setOrderId(1);
        trxModel.setStep("queue.crm.register");
        trxModel.setPriority(0);
        trxModel.setStatus("COMPLETED");

        TrxModel transaksiUpdateStep = new TrxModel();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.crm.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("CANCELED");

        TrxModel transaksiNextRollbackStep = new TrxModel();
        transaksiNextRollbackStep.setId(1);
        transaksiNextRollbackStep.setActionId(1);
        transaksiNextRollbackStep.setOrderId(1);
        transaksiNextRollbackStep.setStep("queue.crm.register");
        transaksiNextRollbackStep.setPriority(0);
        transaksiNextRollbackStep.setStatus("COMPLETED");

        when(trxRepository.orderIdAndPriority(1, 0)).thenReturn(Mono.just(trxModel));
        when(trxRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(trxRepository.statusRollback(1)).thenReturn(Mono.empty());

        consumerService.rollbackRegister(message);

        verify(trxRepository).orderIdAndPriority(1, 0);
        verify(trxRepository).save(transaksiUpdateStep);
        verify(trxRepository).statusRollback(1);
    }

    @Test
    void testStatusNotifCompleted() {
        ResponseNotifModel responseNotifModel = new ResponseNotifModel();
        responseNotifModel.setNotifStatus("NOTIF_COMPLETED");
        responseNotifModel.setOrderId(1);
        responseNotifModel.setPriority(1);
        responseNotifModel.setData("data");

        Message<ResponseNotifModel> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseNotifModel);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(2);
        trxModel.setActionId(1);
        trxModel.setOrderId(1);
        trxModel.setStep("queue.notif");
        trxModel.setPriority(1);
        trxModel.setStatus("INIT");

        TrxModel transaksiUpdateStep = new TrxModel();
        transaksiUpdateStep.setId(2);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.notif");
        transaksiUpdateStep.setPriority(1);
        transaksiUpdateStep.setStatus("COMPLETED");

        TrxModel transaksiNextStep = new TrxModel();
        transaksiNextStep.setId(2);
        transaksiNextStep.setActionId(1);
        transaksiNextStep.setOrderId(1);
        transaksiNextStep.setStep("queue.complete");
        transaksiNextStep.setPriority(1);
        transaksiNextStep.setStatus("INIT");

        when(trxRepository.findStatus(1)).thenReturn(Mono.just(trxModel));
        when(trxRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        doNothing().when(orchestrationService).sendMessage(anyString(), any(DataModel.class));

        consumerService.statusNotif(message);

        verify(trxRepository, times(2)).findStatus(1);
        verify(trxRepository).save(transaksiUpdateStep);
        verify(orchestrationService).sendMessage(anyString(), any(DataModel.class));
    }

    @Test
    void testStatusNotifRejected() {
        ResponseNotifModel responseNotifModel = new ResponseNotifModel();
        responseNotifModel.setNotifStatus("NOTIF_REJECTED");
        responseNotifModel.setOrderId(1);
        responseNotifModel.setPriority(1);
        responseNotifModel.setData("data");

        Message<ResponseNotifModel> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseNotifModel);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(2);
        trxModel.setActionId(1);
        trxModel.setOrderId(1);
        trxModel.setStep("queue.notif");
        trxModel.setPriority(1);
        trxModel.setStatus("INIT");

        TrxModel transaksiUpdateStep = new TrxModel();
        transaksiUpdateStep.setId(2);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.notif");
        transaksiUpdateStep.setPriority(1);
        transaksiUpdateStep.setStatus("CANCELED");

        TrxModel transaksiRollbackStep = new TrxModel();
        transaksiRollbackStep.setId(1);
        transaksiRollbackStep.setActionId(1);
        transaksiRollbackStep.setOrderId(1);
        transaksiRollbackStep.setStep("queue.complete");
        transaksiRollbackStep.setPriority(0);
        transaksiRollbackStep.setStatus("COMPLETED");

        when(trxRepository.orderIdAndPriority(1, 1)).thenReturn(Mono.just(trxModel));
        when(trxRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(trxRepository.statusRollback(1)).thenReturn(Mono.just(transaksiRollbackStep));
        doNothing().when(orchestrationService).sendMessage(anyString(), any(DataModel.class));

        consumerService.statusNotif(message);

        verify(trxRepository).orderIdAndPriority(1, 1);
        verify(trxRepository).save(transaksiUpdateStep);
        verify(trxRepository).statusRollback(1);
        verify(orchestrationService).sendMessage(anyString(), any(DataModel.class));
    }

    @Test
    void testStatusNotifRejectedNextRollbackNull() {
        ResponseNotifModel responseNotifModel = new ResponseNotifModel();
        responseNotifModel.setNotifStatus("NOTIF_REJECTED");
        responseNotifModel.setOrderId(1);
        responseNotifModel.setPriority(1);
        responseNotifModel.setData("data");

        Message<ResponseNotifModel> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseNotifModel);

        TrxModel trxModel = new TrxModel();
        trxModel.setId(2);
        trxModel.setActionId(1);
        trxModel.setOrderId(1);
        trxModel.setStep("queue.notif");
        trxModel.setPriority(1);
        trxModel.setStatus("INIT");

        TrxModel transaksiUpdateStep = new TrxModel();
        transaksiUpdateStep.setId(2);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.notif");
        transaksiUpdateStep.setPriority(1);
        transaksiUpdateStep.setStatus("CANCELED");

        TrxModel transaksiRollbackStep = new TrxModel();
        transaksiRollbackStep.setId(1);
        transaksiRollbackStep.setActionId(1);
        transaksiRollbackStep.setOrderId(1);
        transaksiRollbackStep.setStep("queue.complete");
        transaksiRollbackStep.setPriority(0);
        transaksiRollbackStep.setStatus("COMPLETED");

        when(trxRepository.orderIdAndPriority(1, 1)).thenReturn(Mono.just(trxModel));
        when(trxRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(trxRepository.statusRollback(1)).thenReturn(Mono.empty());

        consumerService.statusNotif(message);

        verify(trxRepository).orderIdAndPriority(1, 1);
        verify(trxRepository).save(transaksiUpdateStep);
        verify(trxRepository).statusRollback(1);
    }
}
