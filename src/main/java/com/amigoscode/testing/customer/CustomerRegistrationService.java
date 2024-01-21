package com.amigoscode.testing.customer;

import com.amigoscode.testing.utils.PhoneNumberValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerRegistrationService {

    private final CustomerRepository customerRepository;

    private final PhoneNumberValidator phoneNumberValidator;

    @Autowired
    public CustomerRegistrationService(CustomerRepository customerRepository,
                                       PhoneNumberValidator phoneNumberValidator) {
        this.customerRepository = customerRepository;
        this.phoneNumberValidator = phoneNumberValidator;
    }

    public void registerNewCustomer(CustomerRegistrationRequest request) {
        String phoneNumber = request.getCustomer().getPhoneNumber();

        if (!phoneNumberValidator.test(phoneNumber)) {
            throw new IllegalStateException("Phone Number " + phoneNumber + " is not valid");
        }

        // 1. PhoneNumber is taken
        Optional<Customer> customerOptional = customerRepository.selectCustomerByPhoneNumber(phoneNumber);

        // 2. If taken let check if belongs to same customer
        if (customerOptional.isPresent()) {
            // 2.1. if yes return
            if (customerOptional.get().getName().equals(request.getCustomer().getName())) {
                return;
            }

            // 2.2. thrown an exception
            throw new IllegalStateException(String.format("phone number [%s] is taken", request.getCustomer().getPhoneNumber()));
        }

        if (request.getCustomer().getId() == null) {
            request.getCustomer().setId(UUID.randomUUID());
        }

        // 3. Save customer
        customerRepository.save(request.getCustomer());
    }
}
