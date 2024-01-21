package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.CustomerRepository;
import com.amigoscode.testing.payment.twilio.TwilioService;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final CardPaymentCharger cardPaymentCharger;

    private final CardPaymentSender cardPaymentSender;

    private final CustomerRepository customerRepository;

    private final PaymentRepository paymentRepository;

    private static final List<Currency> ACCEPTED_CURRENCIES = List.of(Currency.USD, Currency.GBP);

    @Autowired
    public PaymentService(CardPaymentCharger cardPaymentCharger,
                          CardPaymentSender cardPaymentSender,
                          CustomerRepository customerRepository,
                          PaymentRepository paymentRepository) {
        this.cardPaymentSender = cardPaymentSender;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.cardPaymentCharger = cardPaymentCharger;
    }

    public void chargeCard(UUID customerId, PaymentRequest request) {
        // 1. Does customer exist if not throw
        boolean isCustomerFound = customerRepository.findById(customerId).isPresent();

        if (!isCustomerFound) {
            throw new IllegalStateException(String.format("Customer with id [%s] not found", customerId));
        }

        // 2. Do we support the currency if not throw
        boolean isCurrencySupported = ACCEPTED_CURRENCIES.contains(request.getPayment().getCurrency());

        if (!isCurrencySupported) {
            throw new IllegalStateException(String.format("currency [%s] not support", request.getPayment().getCurrency()));
        }

        // 3. Charge card
        CardPaymentCharge charge = cardPaymentCharger.chargeCard(
                request.getPayment().getSource(),
                request.getPayment().getAmount(),
                request.getPayment().getCurrency(),
                request.getPayment().getDescription()
        );

        // 4. If not debited throw
        if (!charge.isCardDebited()) {
            throw new IllegalStateException(String.format("Card not debited for customer %s", customerId));
        }

        // 5. Insert payment
        request.getPayment().setCustomerId(customerId);
        paymentRepository.save(request.getPayment());

        // 6. Send sms
        String paymentNotification = "Card change was successfully completed!";
        Message.Status status = cardPaymentSender.sendSms(customerRepository.selectPhoneNumberById(customerId), paymentNotification);

        // If failed throw
        if (Message.Status.FAILED.equals(status)) {
            throw new IllegalStateException(String.format("SMS not delivered for customer %s", customerId));
        }
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }
}
