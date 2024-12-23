package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.CustomerRegistrationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void itShouldCreatePaymentSuccessfully() throws Exception {
        // Given a customer
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "James", "+447000000000");
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // .. payment
        long paymentId = 1L;

        // register
        Payment payment = new Payment(paymentId,
                customerId,
                new BigDecimal("100.00"),
                Currency.GBP,
                "x0x0x0",
                "Zakat"
        );
        // ... payment request
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // ... When payment is sent
        ResultActions customerRegResultActions = mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectToJson(request))));

        ResultActions paymentResultActions = mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectToJson(paymentRequest))));

        ResultActions getPaymentByIdResultActions = mockMvc.perform(get("/api/v1/payment/{paymentId}", paymentId)
                .contentType(MediaType.APPLICATION_JSON));

        // Then both customer registration and payment requests are 200 status code
        customerRegResultActions.andExpect(status().isOk());
        paymentResultActions.andExpect(status().isOk());
        // payment is stored in db
        // NOTE: Do not use paymentRepository instead create an endpoint to retrieve payments for customers
        getPaymentByIdResultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is(1)));
    }

    private String objectToJson(Object customer) {
        try {
            return new ObjectMapper().writeValueAsString(customer);
        } catch (JsonProcessingException e) {
            fail("Failed to convert object to JSON");
            return null;
        }
    }
}
