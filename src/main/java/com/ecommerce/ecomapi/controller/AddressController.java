package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.dto.AddressRequestDto;
import com.ecommerce.ecomapi.dto.AddressUpdateDTO;
import com.ecommerce.ecomapi.entity.Address;
import com.ecommerce.ecomapi.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/address")
@Tag(name = "08. Address Controller", description = "APIs for managing user addresses including create, update, retrieve, and delete operations")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private ModelMapper modelMapper;

    @Operation(summary = "Save a new address for the current user",
            description = "Creates a new address linked to the authenticated user. " +
                    "Accepts address details in the request body and returns a success or failure message.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/save")
    public ResponseEntity<?> saveAddress(@Valid @RequestBody AddressRequestDto address) {
        boolean isSaved = addressService.saveAddress(modelMapper.map(address, Address.class));
        if (isSaved) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Address saved successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to save address.");
        }
    }

    @Operation(summary = "Get address details by ID",
            description = "Fetches the address details for the specified address ID. " +
                    "User must own the address or have proper authorization.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable Long id) {
        Address address = addressService.findById(id);
        return ResponseEntity.ok(address);
    }

    @Operation(summary = "Get all addresses for a user",
            description = "Returns a list of all save addresses associated with the specified user ID.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getAddressesByUserId(@PathVariable Long userId) {
        List<Address> addresses = addressService.findAllAddressWithUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "Edit a saved address by ID",
            description = "Updates fields of a saved address by address ID. " +
                    "Only non-null and non-empty fields from the request body will be updated.")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editSavedAddress(@PathVariable Long id, @RequestBody @Valid AddressUpdateDTO updatedAddress) {
        boolean isUpdated = addressService.editSavedAddress(id, modelMapper.map(updatedAddress, Address.class));
        if (isUpdated) {
            return ResponseEntity.ok("Address updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update address.");
        }
    }

    @Operation(summary = "Delete a saved address by ID",
            description = "Deletes the address with the specified ID if it belongs to the current user and is marked as saved.")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        boolean deleted = addressService.deleteAddress(id);
        if (deleted) {
            return ResponseEntity.ok("Address deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete address.");
        }
    }

}
