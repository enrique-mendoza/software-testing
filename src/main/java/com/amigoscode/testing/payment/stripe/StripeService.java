package com.amigoscode.testing.payment.stripe;

import com.amigoscode.testing.payment.CardPaymentCharge;
import com.amigoscode.testing.payment.CardPaymentCharger;
import com.amigoscode.testing.payment.Currency;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import com.stripe.param.ChargeCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@ConditionalOnProperty(
        value = "stripe.enable",
        havingValue = "true"
)
@Service
public class StripeService implements CardPaymentCharger {

    private static final RequestOptions requestOptions = RequestOptions.builder()
            .setApiKey("sk_test_IKYCHOAmUhC7IPTdaoVtO58D")
            .build();

    private final StripeApi stripeApi;

    @Autowired
    public StripeService(StripeApi stripeApi) {
        this.stripeApi = stripeApi;
    }

    @Override
    public CardPaymentCharge chargeCard(String cardSource, BigDecimal amount, Currency currency, String description) {
        ChargeCreateParams params = ChargeCreateParams.builder()
                .setAmount(amount.longValue())
                .setCurrency(currency.name())
                .setSource(cardSource)
                .setDescription(description)
                .build();

        try {
            Charge charge = stripeApi.create(params, requestOptions);
            return new CardPaymentCharge(charge.getPaid());
        } catch (StripeException e) {
            throw new IllegalStateException("Cannot make Stripe charge, ", e);
        }
    }
}
