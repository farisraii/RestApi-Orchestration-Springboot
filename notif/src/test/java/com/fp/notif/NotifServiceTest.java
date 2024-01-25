package com.fp.notif;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;

import com.fp.notif.model.DataModel;
import com.fp.notif.model.NotifModel;
import com.fp.notif.model.ResponseNotifModel;
import com.fp.notif.service.NotifService;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class NotifServiceTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @InjectMocks
    private NotifService notifService;

    @Test
    void sendNotif_Success() {
        DataModel dataModel = new DataModel();
        dataModel.setOrderId(1);
        dataModel.setPriority(1);
        dataModel.setData("Sample Data");

        when(r2dbcEntityTemplate.insert(any(NotifModel.class))).thenReturn(Mono.empty());

        notifService.sendNotif(dataModel);

        verify(r2dbcEntityTemplate).insert(any(NotifModel.class));
        verify(jmsTemplate).convertAndSend(eq("queue.notif.status"), any(ResponseNotifModel.class));
    }

    @Test
    void sendNotif_Failure() {
        DataModel dataModel = new DataModel();
        dataModel.setOrderId(1);
        dataModel.setPriority(1);
        dataModel.setData("Data");

        when(r2dbcEntityTemplate.insert(any(NotifModel.class))).thenReturn(Mono.error(new RuntimeException()));

        notifService.sendNotif(dataModel);

        verify(r2dbcEntityTemplate).insert(any(NotifModel.class));
        verify(jmsTemplate).convertAndSend(eq("queue.notif.status"), any(ResponseNotifModel.class));
    }
}