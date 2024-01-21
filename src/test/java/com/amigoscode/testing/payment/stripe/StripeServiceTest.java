package com.amigoscode.testing.payment.stripe;

import com.amigoscode.testing.payment.CardPaymentCharge;
import com.amigoscode.testing.payment.Currency;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import com.stripe.param.ChargeCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class StripeServiceTest {

    @Mock
    private StripeApi stripeApi;

    private StripeService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new StripeService(stripeApi);
    }

    @Test
    void itShouldChargeCard() throws StripeException {
        // Given
        String cardSource = "0x0x0x";
        BigDecimal amount = new BigDecimal("10.00");
        Currency currency = Currency.USD;
        String description = "Zakat";

        Charge charge = new Charge();
        charge.setPaid(true);
        given(stripeApi.create(any(), any())).willReturn(charge);

        // When
        CardPaymentCharge cardPaymentCharge = underTest.chargeCard(cardSource, amount, currency, description);

        // Then
        ArgumentCaptor<ChargeCreateParams> paramsArgumentCaptor = ArgumentCaptor.forClass(ChargeCreateParams.class);
        ArgumentCaptor<RequestOptions> optionsArgumentCaptor = ArgumentCaptor.forClass(RequestOptions.class);

        then(stripeApi).should().create(paramsArgumentCaptor.capture(), optionsArgumentCaptor.capture());

        ChargeCreateParams paramsValue = paramsArgumentCaptor.getValue();

        assertThat(paramsValue.getSource()).isEqualTo(cardSource);
        assertThat(paramsValue.getAmount()).isEqualTo(amount.longValue());
        assertThat(paramsValue.getCurrency()).isEqualTo(currency.name());
        assertThat(paramsValue.getDescription()).isEqualTo(description);

        RequestOptions optionsValue = optionsArgumentCaptor.getValue();

        assertThat(optionsValue).isNotNull();

        assertThat(cardPaymentCharge.isCardDebited()).isTrue();
    }

    @Test
    void itShouldThrowWhenStripeFails() throws StripeException {
        // Given
        String cardSource = "0x0x0x";
        BigDecimal amount = new BigDecimal("10.00");
        Currency currency = Currency.USD;
        String description = "Zakat";

        Charge charge = new Charge();
        charge.setPaid(true);

        given(stripeApi.create(any(), any())).willThrow(ApiConnectionException.class);

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(cardSource, amount, currency, description))
                .isInstanceOf(IllegalStateException.class);
    }
}