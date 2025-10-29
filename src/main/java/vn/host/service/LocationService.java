package vn.host.service;

import vn.host.dto.location.LocationVM;

import java.util.List;
import java.util.Optional;

public interface LocationService {
    void load();
    List<LocationVM.SimpleItem> listProvinces();
    List<LocationVM.SimpleItem> listDistrictsByProvince(Integer provinceCode);
    List<LocationVM.SimpleItem> listWardsByDistrict(Integer districtCode);
    Optional<String> resolveNames(Integer provinceCode, Integer districtCode, Integer wardCode);
}
