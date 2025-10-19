package vn.host.service;

import vn.host.entity.Address;
import vn.host.entity.User;
import vn.host.model.request.AddressRequest;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    <S extends Address> S save(S entity);

    Optional<Address> findById(Long aLong);

    void deleteById(Long aLong);

    long count();

    List<Address> findAll();

    Optional<Address> findDefaultAddressByUserId(Long userId);

    Address findAddressByAddressDetail(AddressRequest addressRequest, User user);
}
