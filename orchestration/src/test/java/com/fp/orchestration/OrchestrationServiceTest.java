package com.fp.orchestration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import com.fp.orchestration.services.OrchestrationService;

@ExtendWith(MockitoExtension.class)
public class OrchestrationServiceTest {
    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private OrchestrationService orchestrationService;

    @Test
    void testSendMessage() {
        String queueName = "queue.test";
        Object messageObject = new Object();

        orchestrationService.sendMessage(queueName, messageObject);

        Mockito.verify(jmsTemplate).convertAndSend(queueName, messageObject);
    }
}
