package com.fp.crm.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.fp.crm.model.CrmModel;
import com.fp.crm.model.DataModel;
import com.fp.crm.model.ResponseCrmModel;
import com.fp.crm.model.enums.NotifStatusEnum;
import com.fp.crm.repository.CrmRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CrmService {

    @Autowired
    CrmRepository crmRepository;
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    R2dbcEntityTemplate r2dbcEntityTemplate;

    public void register(DataModel data) {
        CrmModel crmModel = new CrmModel();
        crmModel.setId(UUID.randomUUID().toString());
        crmModel.setName((String) ((Map<?, ?>) data.getData()).get("name"));
        crmModel.setUsername((String) ((Map<?, ?>) data.getData()).get("crmname"));
        crmModel.setPassword((String) ((Map<?, ?>) data.getData()).get("password"));
        crmModel.setBalance((Integer) ((Map<?, ?>) data.getData()).get("balance"));
        crmModel.setStatus(true);
        crmModel.setCreatedDate(LocalDate.now());
        log.info("crm datanya :{}", crmModel);
        r2dbcEntityTemplate.insert(crmModel)
                .doOnSuccess(registered -> {
                    ResponseCrmModel responseUser = new ResponseCrmModel();
                    responseUser.setOrderId(data.getOrderId());
                    responseUser.setId(registered.getId());
                    responseUser.setName(registered.getName());
                    responseUser.setUsername(registered.getUsername());
                    responseUser.setPassword(registered.getPassword());
                    responseUser.setBalance(registered.getBalance());
                    responseUser.setStatus(registered.isStatus());
                    responseUser.setPriority(data.getPriority());
                    responseUser.setUserStatus("USER_APPROVED");
                    jmsTemplate.convertAndSend("queue.crm.register.status", responseUser);
                    log.info("Message sent to orchestrator that crm successfully registered: {}", responseUser);
                })
                .doOnError(error -> {
                    log.error("crm register failed because : {}", error.getMessage());
                    ResponseCrmModel responseUser = new ResponseCrmModel();
                    responseUser.setOrderId(data.getOrderId());
                    responseUser.setId(crmModel.getId());
                    responseUser.setName(crmModel.getName());
                    responseUser.setUsername(crmModel.getUsername());
                    responseUser.setPassword(crmModel.getPassword());
                    responseUser.setBalance(crmModel.getBalance());
                    responseUser.setStatus(crmModel.isStatus());
                    responseUser.setPriority(data.getPriority());
                    responseUser.setUserStatus("USER_REJECTED");
                    jmsTemplate.convertAndSend("queue.crm.register.status", responseUser);
                    log.info("message sent to orchestrator that crm register rejected: {}", NotifStatusEnum.CRM_REJECTED);
                }).subscribe();
    }

    public void rollbackRegister(DataModel data) {
        System.out.println((String) ((Map<?, ?>) data.getData()).get("id"));
        CrmModel crmModel = new CrmModel();
        crmModel.setId((String) ((Map<?, ?>) data.getData()).get("id"));
        crmModel.setName((String) ((Map<?, ?>) data.getData()).get("name"));
        crmModel.setUsername((String) ((Map<?, ?>) data.getData()).get("crmname"));
        crmModel.setPassword((String) ((Map<?, ?>) data.getData()).get("password"));
        crmModel.setBalance((Integer) ((Map<?, ?>) data.getData()).get("balance"));
        crmModel.setStatus(true);
        crmModel.setCreatedDate(LocalDate.now());
        System.out.println(crmModel.getId());
        crmRepository.deleteById(crmModel.getId())
                .doOnSuccess(deleted -> {
                    ResponseCrmModel responseUser = new ResponseCrmModel();
                    responseUser.setOrderId(data.getOrderId());
                    responseUser.setId(null);
                    responseUser.setName(null);
                    responseUser.setUsername(null);
                    responseUser.setPassword(null);
                    responseUser.setBalance(null);
                    responseUser.setStatus(false);
                    responseUser.setPriority(data.getPriority());
                    responseUser.setUserStatus("ROLLBACKED");
                    log.info("CrmModel deleted, id : {}", crmModel.getId());
                    jmsTemplate.convertAndSend("queue.crm.register.rollback.status", responseUser);
                })
                .doOnError(err -> {
                    log.error(err.getMessage());
                }).subscribe();
    }
}