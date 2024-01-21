package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.CustomerRepository;
import com.amigoscode.testing.payment.twilio.TwilioService;
import com.twilio.rest.api.v2010.account.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class PaymentServiceTest {

    @Mock
    private CardPaymentCharger cardPaymentCharger;

    @Mock
    private CardPaymentSender cardPaymentSender;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new PaymentService(cardPaymentCharger, cardPaymentSender, customerRepository, paymentRepository);
    }

    @Test
    void itShouldChargeCardSuccessfully() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... customer exists
        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(mock(Customer.class)));

        // ... payment request
        PaymentRequest request = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        Currency.USD,
                        "card123xx",
                        "Donation"
                )
        );

        // ... card is charge successfully
        given(cardPaymentCharger.chargeCard(
                request.getPayment().getSource(),
                request.getPayment().getAmount(),
                request.getPayment().getCurrency(),
                request.getPayment().getDescription()
        )).willReturn(new CardPaymentCharge(true));

        // ... sms is sent successfully
        String phoneNumber = "0000";
        given(customerRepository.selectPhoneNumberById(customerId)).willReturn(phoneNumber);

        Message.Status deliveredStatus = Message.Status.DELIVERED;

        given(cardPaymentSender.sendSms(anyString(), anyString())).willReturn(deliveredStatus);

        // When
        underTest.chargeCard(customerId, request);

        // Then
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(Payment.class);

        then(paymentRepository).should().save(paymentArgumentCaptor.capture());

        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue();
        assertThat(paymentArgumentCaptorValue).isEqualToIgnoringGivenFields(
                request.getPayment(), "customerId"
        );
        assertThat(paymentArgumentCaptorValue.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void itShouldThrowWhenCardIsNotCharge() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... customer exists
        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(mock(Customer.class)));

        // ... payment request
        PaymentRequest request = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        Currency.USD,
                        "card123xx",
                        "Donation"
                )
        );

        // ... card is not charge successfully
        given(cardPaymentCharger.chargeCard(
                request.getPayment().getSource(),
                request.getPayment().getAmount(),
                request.getPayment().getCurrency(),
                request.getPayment().getDescription()
        )).willReturn(new CardPaymentCharge(false));

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card not debited for customer " + customerId);
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldNotChargeCardAndThrowWhenCurrencyNotSupported() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... customer exists
        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(mock(Customer.class)));

        // ... Euros
        Currency currency = Currency.EUR;

        // ... payment request
        PaymentRequest request = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        currency,
                        "card123xx",
                        "Donation"
                )
        );

        // When
        assertThatThrownBy(() -> underTest.chargeCard(customerId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("currency [" + currency + "] not support");// When

        // Then
        // ... no interaction with cardPaymentCharger
        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldNotChargeAndThrowWhenCustomerIsNotFound() {
        // Given
        UUID customerId = UUID.randomUUID();

        // customer not found in db
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, new PaymentRequest(new Payment())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Customer with id [" + customerId + "] not found");

        // ... no interactions with PaymentCharger nor PaymentRepository
        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldNotChargeCardAndThrowWhenSmsIsNotDelivered() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... customer exists
        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(mock(Customer.class)));

        // ... payment request
        PaymentRequest request = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("100.00"),
                        Currency.USD,
                        "card123xx",
                        "Donation"
                )
        );

        // ... card is charge successfully
        given(cardPaymentCharger.chargeCard(
                request.getPayment().getSource(),
                request.getPayment().getAmount(),
                request.getPayment().getCurrency(),
                request.getPayment().getDescription()
        )).willReturn(new CardPaymentCharge(true));

        // ... sms is not sent successfully
        String phoneNumber = "0000";
        given(customerRepository.selectPhoneNumberById(customerId)).willReturn(phoneNumber);

        Message.Status failedStatus = Message.Status.FAILED;

        given(cardPaymentSender.sendSms(anyString(), anyString())).willReturn(failedStatus);

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SMS not delivered for customer " + customerId);
    }
}