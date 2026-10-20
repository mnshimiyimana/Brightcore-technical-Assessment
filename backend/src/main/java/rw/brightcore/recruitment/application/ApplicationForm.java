package rw.brightcore.recruitment.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Text fields of the public application form (the CV is a separate multipart part). */
public record ApplicationForm(
        @NotBlank(message = "Full name is required.")
        @Size(max = 120, message = "Full name must be at most 120 characters.")
        String fullName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 255, message = "Email must be at most 255 characters.")
        String email,

        @NotBlank(message = "Phone number is required.")
        @Pattern(regexp = "^\\+?[0-9][0-9 -]{7,18}[0-9]$",
                message = "Enter a valid phone number, e.g. +250 788 123 456.")
        String phone,

        @NotBlank(message = "Cover note is required.")
        @Size(max = 2000, message = "Cover note must be at most 2,000 characters.")
        String coverNote) {
}
