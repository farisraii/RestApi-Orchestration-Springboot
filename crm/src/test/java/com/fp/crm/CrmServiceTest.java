package com.fp.crm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;

import com.fp.crm.model.CrmModel;
import com.fp.crm.model.DataModel;
import com.fp.crm.repository.CrmRepository;
import com.fp.crm.service.CrmService;

import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
    @Mock
    private CrmRepository crmRepository;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @InjectMocks
    private CrmService crmService;

    @Test
    void testRegister() {
        DataModel dataModel = new DataModel();
        dataModel.setOrderId(1);
        dataModel.setPriority(1);
        Map<String, Object> crmDataModel = new HashMap<>();
        crmDataModel.put("name", "crm");
        crmDataModel.put("username", "crm");
        crmDataModel.put("password", "crm");
        crmDataModel.put("balance", 50000);
        dataModel.setData(crmDataModel);

        CrmModel crm = new CrmModel();
        crm.setId("abc123");
        crm.setName(crmDataModel.get("name").toString());
        crm.setUsername(crmDataModel.get("username").toString());
        crm.setPassword(crmDataModel.get("password").toString());
        crm.setBalance((Integer) crmDataModel.get("balance"));
        crm.setStatus(true);
        crm.setCreatedDate(LocalDate.now());

        lenient().when(r2dbcEntityTemplate.insert(any(CrmModel.class))).thenReturn(Mono.just(crm));
        lenient().doNothing().when(jmsTemplate).convertAndSend(anyString(), (Object) any());
        crmService.register(dataModel);

        verify(r2dbcEntityTemplate).insert(any(CrmModel.class));
        verify(jmsTemplate).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void testRegisterError() {
        DataModel dataModel = new DataModel();
        dataModel.setOrderId(1);
        dataModel.setPriority(1);
        Map<String, Object> crmDataModel = new HashMap<>();
        crmDataModel.put("name", "crm");
        crmDataModel.put("username", "crm");
        crmDataModel.put("password", "crm");
        crmDataModel.put("balance", 50000);
        dataModel.setData(crmDataModel);

        CrmModel crm = new CrmModel();
        crm.setId(UUID.randomUUID().toString());
        crm.setName(crmDataModel.get("name").toString());
        crm.setUsername(crmDataModel.get("username").toString());
        crm.setPassword(crmDataModel.get("password").toString());
        crm.setBalance((Integer) crmDataModel.get("balance"));
        crm.setStatus(true);
        crm.setCreatedDate(LocalDate.now());

        lenient().when(r2dbcEntityTemplate.insert(any(CrmModel.class))).thenReturn(Mono.error(new RuntimeException()));
        lenient().doNothing().when(jmsTemplate).convertAndSend(anyString(), (Object) any());
        crmService.register(dataModel);

        verify(r2dbcEntityTemplate).insert(any(CrmModel.class));
        verify(jmsTemplate).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void testRollbackRegister() {
        DataModel dataModel = new DataModel();
        dataModel.setOrderId(1);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", UUID.randomUUID().toString());
        dataMap.put("name", "crm");
        dataMap.put("username", "crm");
        dataMap.put("password", "crm");
        dataMap.put("balance", 50000);
        dataModel.setData(dataMap);

        CrmModel crm = new CrmModel();
        crm.setId(dataMap.get("id").toString());
        crm.setName(dataMap.get("name").toString());
        crm.setUsername(dataMap.get("username").toString());
        crm.setPassword(dataMap.get("password").toString());
        crm.setBalance((Integer) dataMap.get("balance"));
        crm.setStatus(true);

        lenient().when(crmRepository.deleteById(anyString())).thenReturn(Mono.empty());
        lenient().doNothing().when(jmsTemplate).convertAndSend(anyString(), (Object) any());

        crmService.rollbackRegister(dataModel);

        verify(crmRepository).deleteById(anyString());
        verify(jmsTemplate).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void testRollbackRegisterError() {
        DataModel dataModel = new DataModel();
        dataModel.setOrderId(1);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", UUID.randomUUID().toString());
        dataMap.put("name", "crm");
        dataMap.put("username", "crm");
        dataMap.put("password", "crm");
        dataMap.put("balance", 50000);
        dataModel.setData(dataMap);

        CrmModel crm = new CrmModel();
        crm.setId(dataMap.get("id").toString());
        crm.setName(dataMap.get("name").toString());
        crm.setUsername(dataMap.get("username").toString());
        crm.setPassword(dataMap.get("password").toString());
        crm.setBalance((Integer) dataMap.get("balance"));
        crm.setStatus(true);

        lenient().when(crmRepository.deleteById(anyString())).thenReturn(Mono.error(new RuntimeException()));

        crmService.rollbackRegister(dataModel);

        verify(crmRepository).deleteById(anyString());
    }
}