package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.Address;
import vn.host.entity.User;
import vn.host.model.request.AddressRequest;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    <S extends Address> S save(S entity);

    void deleteUserAddress(Long addressId, Long userId);

    void updateUserAddress(AddressRequest address, Long userId);

    Optional<Address> findById(Long aLong);

    void deleteById(Long aLong);

    long count();

    List<Address> findAll();

    Optional<Address> findDefaultAddressByUserId(Long userId);

    Address findAddressByAddressDetail(AddressRequest addressRequest, User user);

    void saveUserAddress(AddressRequest address, Long userId);

    List<Address> findAllByUserId(Long userId);

    Page<Address> findAllByUserId(Long userId, Pageable pageable);
}
