package vn.host.dto.product;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchSuggestionDTO {
    private Long productId;
    private String name;
    private String imageUrl;
    private String url;
}