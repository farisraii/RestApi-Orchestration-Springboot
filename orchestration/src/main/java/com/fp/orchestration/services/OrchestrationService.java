package com.fp.orchestration.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrchestrationService {

    final JmsTemplate jmsTemplate;

    @Autowired
    public OrchestrationService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage(String queueName, Object object) {
        log.info("send message: {} data: {}", queueName, object);
        jmsTemplate.convertAndSend(queueName, object);
    }
}
