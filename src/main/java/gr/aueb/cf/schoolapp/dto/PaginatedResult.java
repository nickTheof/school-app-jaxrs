package gr.aueb.cf.schoolapp.dto;

import java.util.List;

public record PaginatedResult<T>(
        List<T> data,
        int currentPage,
        int pageSize,
        int totalPages,
        long totalItems
) {
}
