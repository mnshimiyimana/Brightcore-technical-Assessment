package rw.brightcore.recruitment.common;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Stable JSON shape for paginated results (Spring's Page serialisation is not a stable API). */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
