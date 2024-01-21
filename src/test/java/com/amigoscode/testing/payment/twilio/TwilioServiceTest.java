package com.amigoscode.testing.payment.twilio;

import com.twilio.rest.api.v2010.account.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class TwilioServiceTest {

    @Mock
    private TwilioApi twilioApi;

    private TwilioService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new TwilioService(twilioApi);
    }

    @Test
    void itShouldSendSms() {
        // Given
        String to = "0001";
        String textMessage = "Hello from Twilio!";
        Message.Status deliveredStatus = Message.Status.DELIVERED;

        given(twilioApi.create(anyString(), anyString(), anyString())).willReturn(deliveredStatus);

        // When
        Message.Status status = underTest.sendSms(to, textMessage);

        // Then
        then(twilioApi).should().create(TwilioService.TWILIO_PHONE_NUMBER, to, textMessage);

        assertThat(status).isEqualTo(deliveredStatus);
    }

    @Test
    void itShouldNotSendSmsWhenTwilioFails() {
        // Given
        String to = "0001";
        String textMessage = "Hello from Twilio!";
        Message.Status failedStatus = Message.Status.FAILED;

        given(twilioApi.create(anyString(), anyString(), anyString())).willReturn(failedStatus);

        // When
        Message.Status status = underTest.sendSms(to, textMessage);

        // Then
        then(twilioApi).should().create(TwilioService.TWILIO_PHONE_NUMBER, to, textMessage);

        assertThat(status).isEqualTo(failedStatus);
    }
}