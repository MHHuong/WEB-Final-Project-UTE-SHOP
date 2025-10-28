package vn.host.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Address;
import vn.host.model.request.AddressRequest;
import vn.host.model.response.ApiResponse;
import vn.host.service.AddressService;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("{userId}")
    public ResponseEntity<?> getUserAddresses(@PathVariable Long userId) {
        try {
            List<Address> addresses = addressService.findAllByUserId(userId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Get user addresses successfully",
                            addresses
                ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{userId}/default")
    public ResponseEntity<?> getDefaultAddress(@PathVariable Long userId) {
        try {
            Address address = addressService.findDefaultAddressByUserId(userId).orElse(null);

            if (address == null) {
                return new ResponseEntity<>(
                        new ApiResponse(
                                "Error",
                                "No default address found for user",
                                null
                        ), HttpStatus.NOT_FOUND
                );
            }
            return new ResponseEntity<>(
                    new ApiResponse (
                            "Success",
                            "Get default address successfully",
                            address
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("{userId}")
    public ResponseEntity<?> addAddress(@PathVariable Long userId, @RequestBody AddressRequest address) {
        try {
            addressService.saveUserAddress(address, userId);
            return new ResponseEntity<>(
                    new ApiResponse (
                            "Success",
                            "Add address successfully",
                            null
                    ), HttpStatus.CREATED
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @DeleteMapping("{userId}/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        try {
            addressService.deleteUserAddress(addressId, userId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Delete address successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("{userId}")
    public ResponseEntity<?> updateAddress(@PathVariable Long userId, @RequestBody AddressRequest address) {
        try {
            addressService.updateUserAddress(address, userId);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Update address successfully",
                            null
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{userId}/pagination")
    public ResponseEntity<?> getPaginatedAddresses(@PathVariable Long userId,
                                                   @RequestParam int page,
                                                   @RequestParam int size) {
        try {
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<Address> addresses = addressService.findAllByUserId(userId, pageable);
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Get paginated addresses successfully",
                            addresses
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "failed",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}

