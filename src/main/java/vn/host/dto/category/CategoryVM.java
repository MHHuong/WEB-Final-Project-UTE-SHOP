package vn.host.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CategoryVM {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SimpleItem {
        private Long id;
        private String name;
        private Long parentId;
    }
}
