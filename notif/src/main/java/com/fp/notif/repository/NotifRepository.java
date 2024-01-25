package com.fp.notif.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.fp.notif.model.NotifModel;

@Repository
public interface NotifRepository extends R2dbcRepository<NotifModel, String> {

}
