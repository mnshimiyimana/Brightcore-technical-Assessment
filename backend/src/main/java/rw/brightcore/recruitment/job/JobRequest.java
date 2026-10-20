package rw.brightcore.recruitment.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Body for creating or editing a job (admin). */
public record JobRequest(
        @NotBlank(message = "Title is required.")
        @Size(max = 150, message = "Title must be at most 150 characters.")
        String title,

        @NotBlank(message = "Location is required.")
        @Size(max = 120, message = "Location must be at most 120 characters.")
        String location,

        @NotNull(message = "Employment type is required.")
        EmploymentType employmentType,

        @NotBlank(message = "Summary is required.")
        @Size(max = 300, message = "Summary must be at most 300 characters.")
        String summary,

        @NotBlank(message = "Description is required.")
        @Size(max = 10000, message = "Description must be at most 10,000 characters.")
        String description,

        @NotBlank(message = "Responsibilities are required.")
        @Size(max = 10000, message = "Responsibilities must be at most 10,000 characters.")
        String responsibilities,

        @NotBlank(message = "Requirements are required.")
        @Size(max = 10000, message = "Requirements must be at most 10,000 characters.")
        String requirements,

        @NotNull(message = "Closing date is required.")
        LocalDate closingDate) {
}
