package vn.host.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.host.entity.Address;
import vn.host.entity.User;
import vn.host.model.request.AddressRequest;
import vn.host.repository.AddressRepository;
import vn.host.repository.UserRepository;
import vn.host.service.AddressService;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;

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
                    return saveAddress(addressRequest);
                });
    }

    public Address saveAddress(AddressRequest addressRequest) {
        User user = userRepository.findUserByFullName("Default");
        Address newAddress = Address.builder()
                .receiverName(addressRequest.getReceiverName())
                .phone(addressRequest.getPhone())
                .province(addressRequest.getProvince())
                .district(addressRequest.getDistrict())
                .ward(addressRequest.getWard())
                .addressDetail(addressRequest.getAddressDetail())
                .user(user)
                .isDefault(0)
                .build();
        return addressRepository.save(newAddress);
    }

    @Override
    public void saveUserAddress(AddressRequest address, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Integer isDefault = address.getIsDefault();
        if (isDefault != null && isDefault == 1) {
            Optional<Address> currentDefaultAddress = addressRepository.findDefaultAddressByUserId(userId);
            currentDefaultAddress.ifPresent(addr -> {
                addr.setIsDefault(0);
                addressRepository.save(addr);
            });
        }
        Address newAddress = Address.builder()
                .receiverName(address.getReceiverName())
                .phone(address.getPhone())
                .province(address.getProvince())
                .district(address.getDistrict())
                .ward(address.getWard())
                .addressDetail(address.getAddressDetail())
                .user(user)
                .isDefault(isDefault)
                .build();
        addressRepository.save(newAddress);
        user.getAddresses().add(newAddress);
    }

    @Override
    public List<Address> findAllByUserId(Long userId) {
        return addressRepository.findAllByUserId(userId);
    }
}
