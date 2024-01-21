package com.amigoscode.testing.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query(
            value = "SELECT id, name, phone_number FROM customer WHERE phone_number = :phone_number",
            nativeQuery = true
    )
    Optional<Customer> selectCustomerByPhoneNumber(@Param("phone_number") String phoneNumber);

    @Query(
            value = "SELECT phone_number FROM customer WHERE id = :id",
            nativeQuery = true
    )
    String selectPhoneNumberById(UUID id);
}
