package com.fp.crm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.fp.crm.model.DataModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConsumerService {

    @Autowired
    CrmService crmService;

    @Autowired
    JmsTemplate jmsTemplate;

    @JmsListener(destination = "queue.crm.register")
    public void receiveRegister(Message<DataModel> message) {
        try {
            log.info("whole data : {}", message.getPayload().getData());
            crmService.register(message.getPayload());
        } catch (Exception e) {
            log.error("Error while register : {}", e.getMessage());
            jmsTemplate.convertAndSend("queue.crm.register.status", "USER_REJECTED");
        }
    }

    @JmsListener(destination = "queue.crm.register.rollback")
    public void rollbackRegister(Message<DataModel> message) {
        try {
            log.info("whole data : {}", message.getPayload().getData());
            crmService.rollbackRegister(message.getPayload());
        } catch (Exception e) {
            log.error("Error while rollback register : {}", e.getMessage());
        }
    }
}