package com.fp.notif.model;

public class ResponseNotifModel {
    Integer orderId;
    Integer priority;
    Object data;
    String notifStatus;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getNotifStatus() {
        return notifStatus;
    }

    public void setNotifStatus(String notifStatus) {
        this.notifStatus = notifStatus;
    }

}
