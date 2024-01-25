package com.fp.orchestration.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.fp.orchestration.model.DataModel;
import com.fp.orchestration.model.OrderModel;
import com.fp.orchestration.model.ResponseCrmModel;
import com.fp.orchestration.model.ResponseNotifModel;
import com.fp.orchestration.model.TrxModel;
import com.fp.orchestration.repository.TrxRepository;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
@Service
@Slf4j
public class ConsumerService {

    final OrchestrationService orchestrationService;
    final TrxRepository trxRepository;

    @Autowired
    public ConsumerService(OrchestrationService orchestrationService,
            TrxRepository trxRepository) {
        this.orchestrationService = orchestrationService;
        this.trxRepository = trxRepository;
    }

    @JmsListener(destination = "queue.orders")
    public void processOrder(Message<OrderModel> message) {
        OrderModel order = new OrderModel();
        order.setOrderId(message.getPayload().getOrderId());
        order.setAction(message.getPayload().getAction());
        order.setData(message.getPayload().getData());
        log.info("order masuk dengan data payload : {} {} {}", order.getOrderId(), order.getAction(), order.getData());
        trxRepository.findStatus(order.getOrderId())
                .doOnSuccess(stepFound -> {
                    DataModel dataModel = new DataModel();
                    dataModel.setOrderId(stepFound.getOrderId());
                    dataModel.setPriority(stepFound.getPriority());
                    dataModel.setData(message.getPayload().getData());
                    log.info("(First Step) Data before send message : {}", dataModel.getData());
                    orchestrationService.sendMessage(stepFound.getStep(), dataModel);
                }).subscribe();
    }

    @JmsListener(destination = "queue.crm.register.status")
    public void statusRegister(Message<ResponseCrmModel> message) {
        if (message.getPayload().getUserStatus().equals("USER_APPROVED")) {
            trxRepository.findStatus(message.getPayload().getOrderId())
                    .doOnSuccess(found -> {
                        TrxModel trxModel = new TrxModel();
                        trxModel.setId(found.getId());
                        trxModel.setActionId(found.getActionId());
                        trxModel.setOrderId(found.getOrderId());
                        trxModel.setStep(found.getStep());
                        trxModel.setPriority(found.getPriority());
                        trxModel.setStatus("COMPLETED");
                        trxRepository.save(trxModel)
                                .doOnSuccess(updatedStatusTransaksi -> {
                                    trxRepository.findStatus(updatedStatusTransaksi.getOrderId())
                                            .doOnSuccess(nextStepTransaksi -> {

                                                Map<String, Object> data = new HashMap<>();
                                                data.put("id", message.getPayload().getId());
                                                data.put("name", message.getPayload().getName());
                                                data.put("crmname", message.getPayload().getUsername());
                                                data.put("password", message.getPayload().getPassword());
                                                data.put("balance", message.getPayload().getBalance());
                                                data.put("status", message.getPayload().isStatus());

                                                DataModel dataModel = new DataModel();
                                                dataModel.setOrderId(nextStepTransaksi.getOrderId());
                                                dataModel.setPriority(nextStepTransaksi.getPriority());
                                                dataModel.setData(data);
                                                log.info("(Status Register) Data before send message : {}",
                                                        dataModel.getData());
                                                orchestrationService.sendMessage(nextStepTransaksi.getStep(),
                                                        dataModel);
                                            }).subscribe();
                                }).subscribe();
                    }).subscribe();
        } else {
            ResponseCrmModel responseUser = message.getPayload();
            log.info("User rejected : {}", message.getPayload().getUserStatus());
            trxRepository.orderIdAndPriority(responseUser.getOrderId(), responseUser.getPriority())
                    .doOnSuccess(found -> {
                        TrxModel trxModel = new TrxModel();
                        trxModel.setId(found.getId());
                        trxModel.setActionId(found.getActionId());
                        trxModel.setOrderId(found.getOrderId());
                        trxModel.setStep(found.getStep());
                        trxModel.setPriority(found.getPriority());
                        trxModel.setStatus("CANCELED");
                        trxRepository.save(trxModel)
                                .doOnSuccess(updatedStatus -> {
                                    trxRepository.statusRollback(message.getPayload().getOrderId())
                                            .doOnSuccess(nextRollbackStep -> {
                                                if (nextRollbackStep == null) {
                                                    log.info("Rollback completed");
                                                } else {
                                                    DataModel dataModel = new DataModel();
                                                    dataModel.setOrderId(nextRollbackStep.getOrderId());
                                                    dataModel.setPriority(nextRollbackStep.getPriority());
                                                    dataModel.setData(message.getPayload().getId());
                                                    log.info("(Status Notif) Data rollback before send message : {}",
                                                            dataModel.getData());
                                                    orchestrationService.sendMessage(
                                                            nextRollbackStep.getStep() + ".rollback", dataModel);
                                                }
                                            }).subscribe();
                                }).subscribe();
                    }).subscribe();
        }
    }

    @JmsListener(destination = "queue.crm.register.rollback.status")
    public void rollbackRegister(Message<ResponseCrmModel> message) {
        if (message.getPayload().getUserStatus().equals("ROLLBACKED")) {
            ResponseCrmModel responseUser = message.getPayload();
            trxRepository.orderIdAndPriority(responseUser.getOrderId(), responseUser.getPriority())
                    .doOnSuccess(found -> {
                        TrxModel trxModel = new TrxModel();
                        trxModel.setId(found.getId());
                        trxModel.setActionId(found.getActionId());
                        trxModel.setOrderId(found.getOrderId());
                        trxModel.setStep(found.getStep());
                        trxModel.setPriority(found.getPriority());
                        trxModel.setStatus("CANCELED");
                        trxRepository.save(trxModel)
                                .doOnSuccess(updatedStatus -> {
                                    trxRepository.statusRollback(message.getPayload().getOrderId())
                                            .doOnSuccess(nextRollbackStep -> {
                                                if (nextRollbackStep == null) {
                                                    log.info("Rollback completed");
                                                } else {
                                                    DataModel dataModel = new DataModel();
                                                    dataModel.setOrderId(nextRollbackStep.getOrderId());
                                                    dataModel.setPriority(nextRollbackStep.getPriority());
                                                    dataModel.setData(message.getPayload().getId());
                                                    log.info(
                                                            "(Status User Rollback) Data rollback before send message : {}",
                                                            dataModel.getData());
                                                    orchestrationService.sendMessage(
                                                            nextRollbackStep.getStep() + ".rollback", dataModel);
                                                }
                                            }).subscribe();
                                }).subscribe();
                    }).subscribe();
        }
    }

    @JmsListener(destination = "queue.notif.status")
    public void statusNotif(Message<ResponseNotifModel> message) {
        if (message.getPayload().getNotifStatus().equals("NOTIF_COMPLETED")) {
            trxRepository.findStatus(message.getPayload().getOrderId())
                    .doOnSuccess(found -> {
                        TrxModel trxModel = new TrxModel();
                        trxModel.setId(found.getId());
                        trxModel.setActionId(found.getActionId());
                        trxModel.setOrderId(found.getOrderId());
                        trxModel.setStep(found.getStep());
                        trxModel.setPriority(found.getPriority());
                        trxModel.setStatus("COMPLETED");
                        trxRepository.save(trxModel)
                                .doOnSuccess(updatedStatusTransaksi -> trxRepository
                                        .findStatus(message.getPayload().getOrderId())
                                        .doOnSuccess(nextStepTransaksi -> {
                                            DataModel dataModel = new DataModel();
                                            dataModel.setOrderId(nextStepTransaksi.getOrderId());
                                            dataModel.setPriority(nextStepTransaksi.getPriority());
                                            dataModel.setData(message.getPayload().getData());
                                            log.info("(Status Notif) Data before send message : {}",
                                                    dataModel.getData());
                                            orchestrationService.sendMessage(nextStepTransaksi.getStep(),
                                                    dataModel);
                                        }).subscribe())
                                .subscribe();
                    }).subscribe();
            TrxModel trxModel = new TrxModel();
            trxModel.setStatus("COMPLETED");
        } else {
            log.info("Notif rejected : {}", message.getPayload().getNotifStatus());
            ResponseNotifModel responseNotif = new ResponseNotifModel();
            responseNotif.setOrderId(message.getPayload().getOrderId());
            responseNotif.setPriority(message.getPayload().getPriority());
            responseNotif.setData(message.getPayload().getData());
            responseNotif.setNotifStatus(message.getPayload().getNotifStatus());
            trxRepository.orderIdAndPriority(responseNotif.getOrderId(), responseNotif.getPriority())
                    .doOnSuccess(found -> {
                        TrxModel trxModel = new TrxModel();
                        trxModel.setId(found.getId());
                        trxModel.setActionId(found.getActionId());
                        trxModel.setOrderId(found.getOrderId());
                        trxModel.setStep(found.getStep());
                        trxModel.setPriority(found.getPriority());
                        trxModel.setStatus("CANCELED");
                        trxRepository.save(trxModel)
                                .doOnSuccess(updatedStatus -> {
                                    trxRepository.statusRollback(message.getPayload().getOrderId())
                                            .doOnSuccess(nextRollbackStep -> {
                                                if (nextRollbackStep == null) {
                                                    log.info("Rollback completed");
                                                } else {
                                                    DataModel dataModel = new DataModel();
                                                    dataModel.setOrderId(nextRollbackStep.getOrderId());
                                                    dataModel.setPriority(nextRollbackStep.getPriority());
                                                    dataModel.setData(message.getPayload().getData());
                                                    log.info("(Status Notif) Data rollback before send message : {}",
                                                            dataModel.getData());
                                                    orchestrationService.sendMessage(
                                                            nextRollbackStep.getStep() + ".rollback", dataModel);
                                                }
                                            }).subscribe();
                                }).subscribe();
                    }).subscribe();
        }
    }

}