package com.ecommerce.ecomapi.serviceImpl;

import com.ecommerce.ecomapi.entity.Address;
import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.exception.AddressNotFoundException;
import com.ecommerce.ecomapi.repository.AddressRepo;
import com.ecommerce.ecomapi.service.AddressService;
import com.ecommerce.ecomapi.service.UserService;
import com.ecommerce.ecomapi.utility.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private UserService userService;

    @Override
    public boolean saveAddress(Address address) {
        Long userId = securityUtil.getCurrentUserId();
        User user = userService.getUserById(userId);

        if (user == null) {
            return false; // User not found
        }

        address.setStreet(address.getStreet());
        address.setCity(address.getCity());
        address.setState(address.getState());
        address.setCountry(address.getCountry());
        address.setZip(address.getZip());
        address.setSaved(true);
        address.setAddressType(address.getAddressType());
        address.setUser(user);

        addressRepo.save(address);

        return true;
    }

    @Override
    public Address findById(Long id) {
        return addressRepo.findById(id)
                .orElseThrow( () -> new AddressNotFoundException("Address not found with given ID: " + id));
    }

    @Override
    public List<Address> findAllAddressWithUserId(Long userId) {
        List<Address> addresses = addressRepo.findByUserIdAndIsSaved(userId, true);

        if (addresses.isEmpty()) {
            throw new AddressNotFoundException("Address not found with given id: " + userId);
        }

        return addresses;
    }

    @Override
    public boolean editSavedAddress(Long id,Address updatedAddress) {

        Long userId = securityUtil.getCurrentUserId();
        User user = userService.getUserById(userId);

        Optional<Address> optionalAddress = addressRepo.findById(id);

        if (optionalAddress.isPresent()) {
            Address existingAddress = optionalAddress.get();

            // Check if the address belongs to the current user
            if (!existingAddress.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("You are not authorized to edit this address.");
            }

            // Only allow editing if the address is marked as saved
            if (!existingAddress.isSaved()) {
                throw new IllegalStateException("Only saved addresses can be edited.");
            }

            // Partial update: update only if field is non-null and non-empty (for strings)
            if (updatedAddress.getStreet() != null && !updatedAddress.getStreet().isBlank()) {
                existingAddress.setStreet(updatedAddress.getStreet());
            }
            if (updatedAddress.getCity() != null && !updatedAddress.getCity().isBlank()) {
                existingAddress.setCity(updatedAddress.getCity());
            }
            if (updatedAddress.getState() != null && !updatedAddress.getState().isBlank()) {
                existingAddress.setState(updatedAddress.getState());
            }
            if (updatedAddress.getZip() != null && !updatedAddress.getZip().isBlank()) {
                existingAddress.setZip(updatedAddress.getZip());
            }
            if (updatedAddress.getCountry() != null && !updatedAddress.getCountry().isBlank()) {
                existingAddress.setCountry(updatedAddress.getCountry());
            }
            if (updatedAddress.getAddressType() != null && !updatedAddress.getAddressType().isBlank()) {
                existingAddress.setAddressType(updatedAddress.getAddressType());
            }

            addressRepo.save(existingAddress);
            return true;
        } else {
            throw new AddressNotFoundException("Address not found with ID: " + id);
        }
    }

    @Override
    public boolean deleteAddress(Long id) {
        Long userId = securityUtil.getCurrentUserId();
        Optional<Address> optionalAddress = addressRepo.findById(id);

        if (optionalAddress.isPresent()) {
            Address address = optionalAddress.get();

            // Check ownership
            if (!address.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("You are not authorized to delete this address.");
            }

            // Only allow editing if the address is marked as saved
            if (!address.isSaved()) {
                throw new IllegalStateException("Only saved addresses can be edited.");
            }

            addressRepo.delete(address);
            return true;
        } else {
            throw new AddressNotFoundException("Address not found with ID: " + id);
        }
    }


}
