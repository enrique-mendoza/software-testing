package com.amigoscode.testing.payment;

import com.twilio.rest.api.v2010.account.Message;

public interface CardPaymentSender {

    Message.Status sendSms(String phoneNumber, String textMessage);
}
