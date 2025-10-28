package vn.host.dto;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CategoryNodeDTO {
    private Long id;
    private String name;
    private List<CategoryNodeDTO> children = new ArrayList<>();
}