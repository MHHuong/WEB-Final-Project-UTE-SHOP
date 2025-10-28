package vn.host.dto.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class LocationVM {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Province {
        private Integer code;
        private String name;
        private List<District> districts;
        public Integer getCode() { return code; }
        public void setCode(Integer code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<District> getDistricts() { return districts; }
        public void setDistricts(List<District> districts) { this.districts = districts; }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class District {
        private Integer code;
        private String name;
        private List<Ward> wards;
        public Integer getCode() { return code; }
        public void setCode(Integer code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Ward> getWards() { return wards; }
        public void setWards(List<Ward> wards) { this.wards = wards; }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ward {
        private Integer code;
        private String name;
        public Integer getCode() { return code; }
        public void setCode(Integer code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class SimpleItem {
        private Integer code;
        private String name;
        public SimpleItem() {}
        public SimpleItem(Integer code, String name) { this.code = code; this.name = name; }
        public Integer getCode() { return code; }
        public String getName() { return name; }
        public void setCode(Integer code) { this.code = code; }
        public void setName(String name) { this.name = name; }
    }
}
