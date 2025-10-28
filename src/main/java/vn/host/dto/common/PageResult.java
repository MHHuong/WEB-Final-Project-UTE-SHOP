package vn.host.dto.common;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResult<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    // Map trực tiếp Page<T> -> PageResult<T>
    public static <T> PageResult<T> of(Page<T> pg) {
        return PageResult.<T>builder()
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalPages(pg.getTotalPages())
                .totalElements(pg.getTotalElements())
                .content(pg.getContent())                 // <-- QUAN TRỌNG: set content
                .build();
    }

    // Map Page<S> -> PageResult<D> với mapper
    public static <D, S> PageResult<D> of(Page<S> pg, Function<S, D> mapper) {
        return PageResult.<D>builder()
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalPages(pg.getTotalPages())
                .totalElements(pg.getTotalElements())
                .content(pg.map(mapper).getContent())     // <-- QUAN TRỌNG: set content đã map
                .build();
    }
}