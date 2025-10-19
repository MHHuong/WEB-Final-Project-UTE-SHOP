package vn.host.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.host.entity.Address;
import vn.host.entity.User;
import vn.host.model.request.AddressRequest;
import vn.host.repository.AddressRepository;
import vn.host.service.AddressService;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    AddressRepository addressRepository;

    @Override
    public <S extends Address> S save(S entity) {
        return addressRepository.save(entity);
    }

    @Override
    public Optional<Address> findById(Long aLong) {
        return addressRepository.findById(aLong);
    }

    @Override
    public void deleteById(Long aLong) {
        addressRepository.deleteById(aLong);
    }

    @Override
    public long count() {
        return addressRepository.count();
    }

    @Override
    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    @Override
    public Optional<Address> findDefaultAddressByUserId(Long userId) {
        return addressRepository.findDefaultAddressByUserId(userId);
    }

    @Override
    public Address findAddressByAddressDetail(AddressRequest addressRequest, User user) {
        String province = addressRequest.getProvince();
        String district = addressRequest.getDistrict();
        String ward = addressRequest.getWard();
        String detail = addressRequest.getAddressDetail();
        String receiverName = addressRequest.getReceiverName();
        String phone = addressRequest.getPhone();
        return addressRepository.findAddresses(province, district, ward, detail, receiverName, phone, user)
                .orElseGet(() -> {
                    return saveAddress(addressRequest, user);
                });
    }

    public Address saveAddress(AddressRequest addressRequest, User user) {
        Address newAddress = Address.builder()
                .receiverName(addressRequest.getReceiverName())
                .phone(addressRequest.getPhone())
                .province(addressRequest.getProvince())
                .district(addressRequest.getDistrict())
                .ward(addressRequest.getWard())
                .addressDetail(addressRequest.getAddressDetail())
                .user(user)
                .build();
        return addressRepository.save(newAddress);
    }
}
