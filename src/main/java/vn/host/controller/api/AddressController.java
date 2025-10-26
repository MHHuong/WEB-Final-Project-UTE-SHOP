package vn.host.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Address;
import vn.host.model.ResponseModel;
import vn.host.model.request.AddressRequest;
import vn.host.model.response.AddressResponse;
import vn.host.service.AddressService;

import java.util.List;
import java.util.stream.Collectors;

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
                    new ResponseModel (
                            "true",
                            "Get user addresses successfully",
                            addresses
                ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "false",
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
                        new ResponseModel(
                                "false",
                                "No default address found for user",
                                null
                        ), HttpStatus.NOT_FOUND
                );
            }
            return new ResponseEntity<>(
                    new ResponseModel (
                            "true",
                            "Get default address successfully",
                            address
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "false",
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
                    new ResponseModel (
                            "true",
                            "Add address successfully",
                            null
                    ), HttpStatus.CREATED
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "false",
                            "Error: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}

