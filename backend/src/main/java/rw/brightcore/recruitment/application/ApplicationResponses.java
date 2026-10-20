package rw.brightcore.recruitment.application;

import java.time.Instant;

public final class ApplicationResponses {

    private ApplicationResponses() {}

    /** What the candidate gets back: a confirmation, nothing more. */
    public record Receipt(Long id, String jobTitle, String message) {}

    /** A row in the admin applicants table. */
    public record ApplicationSummary(
            Long id, Long jobId, String fullName, String email, String phone,
            ApplicationStatus status, Instant createdAt) {

        public static ApplicationSummary from(JobApplication a) {
            return new ApplicationSummary(a.getId(), a.getJob().getId(), a.getFullName(),
                    a.getEmail(), a.getPhone(), a.getStatus(), a.getCreatedAt());
        }
    }

    /** The admin application page. */
    public record ApplicationDetail(
            Long id, Long jobId, String jobTitle, String fullName, String email, String phone,
            String coverNote, String cvFileName, long cvSizeBytes, ApplicationStatus status,
            String decisionNote, Instant createdAt, Instant updatedAt) {

        public static ApplicationDetail from(JobApplication a) {
            return new ApplicationDetail(a.getId(), a.getJob().getId(), a.getJob().getTitle(),
                    a.getFullName(), a.getEmail(), a.getPhone(), a.getCoverNote(),
                    a.getCvOriginalName(), a.getCvSizeBytes(), a.getStatus(),
                    a.getDecisionNote(), a.getCreatedAt(), a.getUpdatedAt());
        }
    }
}
