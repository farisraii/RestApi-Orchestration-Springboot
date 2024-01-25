package com.fp.notif.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.fp.notif.model.DataModel;
import com.fp.notif.model.NotifModel;
import com.fp.notif.model.ResponseNotifModel;
import com.fp.notif.model.enums.NotifStatusEnum;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotifService {

    final JmsTemplate jmsTemplate;
    final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Autowired
    public NotifService(JmsTemplate jmsTemplate, R2dbcEntityTemplate r2dbcEntityTemplate) {

        this.jmsTemplate = jmsTemplate;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    }

    public void sendNotif(DataModel data) {
        NotifModel notifModel = new NotifModel();
        notifModel.setOrderId(data.getOrderId());
        notifModel.setStatus("NOTIF_COMPLETED");
        notifModel.setCreatedDate(LocalDate.now());
        log.info("notif datanya :{}", notifModel);
        r2dbcEntityTemplate.insert(notifModel)
                .doOnSuccess(registered -> {
                    ResponseNotifModel responseNotifModel = new ResponseNotifModel();
                    responseNotifModel.setOrderId(data.getOrderId());
                    responseNotifModel.setPriority(data.getPriority());
                    responseNotifModel.setData(data.getData());
                    responseNotifModel.setNotifStatus("NOTIF_COMPLETED");
                    jmsTemplate.convertAndSend("queue.notif.status", responseNotifModel);
                    log.info("Message sent to orchestrator that notif successfully registered: {}", responseNotifModel);
                })
                .doOnError(error -> {
                    log.error("notif failed because : {}", error.getMessage());
                    ResponseNotifModel responseNotifModel = new ResponseNotifModel();
                    responseNotifModel.setOrderId(data.getOrderId());
                    responseNotifModel.setPriority(data.getPriority());
                    responseNotifModel.setData(data.getData());
                    responseNotifModel.setNotifStatus("NOTIF_REJECTED");
                    jmsTemplate.convertAndSend("queue.notif.status", responseNotifModel);
                    log.info("message sent to orchestrator that notif rejected: {}", NotifStatusEnum.NOTIF_REJECTED);
                }).subscribe();
    }
}