package com.amigoscode.testing.payment.twilio;

import com.amigoscode.testing.payment.CardPaymentSender;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(
        value = "twilio.enable",
        havingValue = "false"
)
@Service
public class MockTwilioService implements CardPaymentSender {

    @Override
    public Message.Status sendSms(String phoneNumber, String textMessage) {
        return Message.Status.DELIVERED;
    }
}
