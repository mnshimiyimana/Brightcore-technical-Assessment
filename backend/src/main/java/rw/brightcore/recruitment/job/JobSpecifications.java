package rw.brightcore.recruitment.job;

import java.time.LocalDate;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Composable, parameterised filters for the public jobs list (no string-built SQL). */
final class JobSpecifications {

    private JobSpecifications() {}

    static Specification<Job> acceptingApplications(LocalDate today) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), JobStatus.OPEN),
                cb.greaterThanOrEqualTo(root.get("closingDate"), today));
    }

    static Specification<Job> hasType(EmploymentType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("employmentType"), type);
    }

    /** Case-insensitive match on title, location or summary. */
    static Specification<Job> matches(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + escapeLike(search.trim().toLowerCase(Locale.ROOT)) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern, '\\'),
                    cb.like(cb.lower(root.get("location")), pattern, '\\'),
                    cb.like(cb.lower(root.get("summary")), pattern, '\\'));
        };
    }

    /** Stops user input such as "%" from acting as a LIKE wildcard. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
