package com.fp.orchestration.model;

public class ResponseCrmModel {
    
    Integer orderId;
    Integer priority;
    String id;
    String name;
    String crmname;
    String password;
    Integer balance;
    boolean status;
    String crmStatus;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return crmname;
    }

    public void setUsername(String crmname) {
        this.crmname = crmname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getUserStatus() {
        return crmStatus;
    }

    public void setUserStatus(String crmStatus) {
        this.crmStatus = crmStatus;
    }

}
