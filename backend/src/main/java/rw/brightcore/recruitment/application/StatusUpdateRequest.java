package rw.brightcore.recruitment.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required.")
        ApplicationStatus status,

        @Size(max = 1000, message = "Note must be at most 1,000 characters.")
        String note) {
}
