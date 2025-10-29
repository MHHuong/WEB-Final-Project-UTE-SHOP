package vn.host.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import vn.host.dto.location.LocationVM;
import vn.host.service.LocationService;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final ObjectMapper mapper = new ObjectMapper();

    private List<LocationVM.Province> provinces = Collections.emptyList();
    private Map<Integer, LocationVM.Province> provinceByCode = new HashMap<>();
    private Map<Integer, LocationVM.District> districtByCode = new HashMap<>();
    @Override
    @PostConstruct
    public void load() {
        try {
            ClassPathResource cpr = new ClassPathResource("data/addresses.json");
            try (InputStream is = cpr.getInputStream()) {
                provinces = mapper.readValue(is, new TypeReference<List<LocationVM.Province>>() {});
            }
            provinceByCode = provinces.stream()
                    .filter(p -> p.getCode()!=null)
                    .collect(Collectors.toMap(LocationVM.Province::getCode, p -> p));

            districtByCode.clear();
            for (LocationVM.Province p : provinces) {
                if (p.getDistricts()!=null) {
                    for (LocationVM.District d : p.getDistricts()) {
                        if (d.getCode()!=null) {
                            districtByCode.put(d.getCode(), d);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể load data provinces.json. Hãy đảm bảo file tồn tại ở src/main/resources/data/addresses.json", e);
        }
    }

    @Override
    public List<LocationVM.SimpleItem> listProvinces() {
        return provinces.stream()
                .map(p -> new LocationVM.SimpleItem(p.getCode(), p.getName()))
                .sorted(Comparator.comparing(LocationVM.SimpleItem::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<LocationVM.SimpleItem> listDistrictsByProvince(Integer provinceCode) {
        LocationVM.Province p = provinceByCode.get(provinceCode);
        if (p == null || p.getDistricts()==null) return List.of();
        return p.getDistricts().stream()
                .map(d -> new LocationVM.SimpleItem(d.getCode(), d.getName()))
                .sorted(Comparator.comparing(LocationVM.SimpleItem::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<LocationVM.SimpleItem> listWardsByDistrict(Integer districtCode) {
        LocationVM.District d = districtByCode.get(districtCode);
        if (d == null || d.getWards()==null) return List.of();
        return d.getWards().stream()
                .map(w -> new LocationVM.SimpleItem(w.getCode(), w.getName()))
                .sorted(Comparator.comparing(LocationVM.SimpleItem::getName))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> resolveNames(Integer provinceCode, Integer districtCode, Integer wardCode) {
        LocationVM.Province p = provinceByCode.get(provinceCode);
        LocationVM.District d = districtByCode.get(districtCode);
        String provinceName = (p != null) ? p.getName() : null;
        String districtName = (d != null) ? d.getName() : null;
        String wardName = null;
        if (d != null && d.getWards()!=null) {
            for (LocationVM.Ward w : d.getWards()) {
                if (Objects.equals(w.getCode(), wardCode)) { wardName = w.getName(); break; }
            }
        }
        if (provinceName==null || districtName==null || wardName==null) return Optional.empty();
        return Optional.of(String.join(", ", wardName, districtName, provinceName));
    }
}
