package com.fp.crm;

import com.fp.crm.model.DataModel;
import com.fp.crm.service.ConsumerService;
import com.fp.crm.service.CrmService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConsumerServiceTest {

    @InjectMocks
    private ConsumerService consumerService;

    @Mock
    private CrmService crmService;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Message<DataModel> message;

    @Test
    void testReceiveRegister() {
        DataModel dataModel = new DataModel();
        when(message.getPayload()).thenReturn(dataModel);

        consumerService.receiveRegister(message);

        verify(crmService).register(dataModel);
    }

    @Test
    void testReceiveRegisterException() {
        DataModel dataModel = new DataModel();
        when(message.getPayload()).thenReturn(dataModel);

        doThrow(new RuntimeException()).when(crmService).register(any(DataModel.class));

        consumerService.receiveRegister(message);

        verify(jmsTemplate).convertAndSend("queue.user.register.status", "USER_REJECTED");
        verify(crmService).register(any(DataModel.class));
    }

    @Test
    void testRollbackRegister() {
        DataModel dataModel = new DataModel();
        when(message.getPayload()).thenReturn(dataModel);

        consumerService.rollbackRegister(message);

        verify(crmService).rollbackRegister(dataModel);
    }

    @Test
    void testRollbackRegisterException() {
        when(message.getPayload()).thenThrow(new RuntimeException());

        consumerService.rollbackRegister(message);

        verifyNoInteractions(crmService);
        verifyNoInteractions(jmsTemplate);
    }
}