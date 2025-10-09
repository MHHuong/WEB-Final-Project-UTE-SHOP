package vn.host.service;

import vn.host.entity.Shipper;

import java.util.List;

public interface ShipperService {
    void save(Shipper shipper);
    void delete(long id);
    List<Shipper> findAll();
    Shipper findById(long id);
    List<Shipper> findByShippingProviderId(long shippingProviderId);
}
