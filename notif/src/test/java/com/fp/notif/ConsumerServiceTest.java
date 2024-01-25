package com.fp.notif;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import com.fp.notif.model.DataModel;
import com.fp.notif.service.ConsumerService;
import com.fp.notif.service.NotifService;

public class ConsumerServiceTest {

    @Mock
    private NotifService notifService;

    @Mock
    private Message<DataModel> message;

    @InjectMocks
    private ConsumerService consumerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testReceiveSendNotif() {
        DataModel dataModel = new DataModel();
        when(message.getPayload()).thenReturn(dataModel);

        consumerService.receiveSendNotif(message);

        verify(notifService).sendNotif(dataModel);
    }

    @Test
    void testReceiveSendNotifWithError() {
        when(message.getPayload()).thenThrow(new RuntimeException());

        consumerService.receiveSendNotif(message);

        verifyNoInteractions(notifService);
    }
}