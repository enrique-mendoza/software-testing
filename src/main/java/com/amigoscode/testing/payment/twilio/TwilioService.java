package com.amigoscode.testing.payment.twilio;

import com.amigoscode.testing.payment.CardPaymentSender;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(
        value = "twilio.enable",
        havingValue = "true"
)
@Service
public class TwilioService implements CardPaymentSender {

    public static final String ACCOUNT_SID = "ACa8e3a8be7cd829bde049a78020277beb";

    public static final String AUTH_TOKEN = "51f7ea2345fec30a971394b650e60b72";

    public static final String TWILIO_PHONE_NUMBER = "+14432806845";

    private final TwilioApi twilioApi;

    @Autowired
    public TwilioService(TwilioApi twilioApi) {
        this.twilioApi = twilioApi;
    }

    @Override
    public Message.Status sendSms(String phoneNumber, String textMessage) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        return twilioApi.create(TWILIO_PHONE_NUMBER, phoneNumber, textMessage);
    }
}
