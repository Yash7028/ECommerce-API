package com.ecommerce.ecomapi.repository;

import com.ecommerce.ecomapi.entity.Address;
import com.ecommerce.ecomapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepo extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByStreetAndCityAndStateAndZipAndCountryAndUser(
            String street,
            String city,
            String state,
            String zip,
            String country,
            User user
    );

    Optional<Address> findByStreetAndCityAndStateAndZipAndCountryAndUserAndIsSaved(
            String street, String city, String state, String zip,
            String country, User user, boolean isSaved);

    List<Address> findByUserIdAndIsSaved(Long userId, boolean isSaved);

}
