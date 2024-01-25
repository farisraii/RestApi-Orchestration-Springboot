package com.fp.notif.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.fp.notif.model.DataModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConsumerService {

    final NotifService notifService;

    @Autowired
    public ConsumerService(NotifService notifService) {
        this.notifService = notifService;
    }

    @JmsListener(destination = "queue.notif")
    public void receiveSendNotif(Message<DataModel> message) {
        try {
            notifService.sendNotif(message.getPayload());
        } catch (Exception e) {
            log.error("Error while send notif : {}", e.getMessage());
        }
    }
}
