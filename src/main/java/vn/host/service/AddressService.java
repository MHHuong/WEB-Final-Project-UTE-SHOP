package vn.host.service;

import vn.host.entity.Address;

import java.util.List;

public interface AddressService {
    void save(Address address);
    void delete(long id);
    List<Address> findAll();
    Address findById(long id);
}
