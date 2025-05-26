package com.ecommerce.ecomapi.service;

import com.ecommerce.ecomapi.entity.Address;

import java.util.List;

public interface AddressService {
    public boolean saveAddress(Address address);

    public Address findById(Long id);

    public List<Address> findAllAddressWithUserId(Long id);

    public boolean editSavedAddress(Long id,Address address);

    public boolean deleteAddress(Long id);
}
