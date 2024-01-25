package com.fp.crm.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.fp.crm.model.CrmModel;

@Repository
public interface CrmRepository extends R2dbcRepository<CrmModel, String> {

}