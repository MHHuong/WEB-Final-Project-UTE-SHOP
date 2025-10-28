package vn.host.dto.review;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewMediaRes {
    private Long id;
    private String url;
    private String type;
}