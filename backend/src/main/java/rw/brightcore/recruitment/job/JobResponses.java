package rw.brightcore.recruitment.job;

import java.time.Instant;
import java.time.LocalDate;

/** Read models returned by the job endpoints. Entities are never serialised directly. */
public final class JobResponses {

    private JobResponses() {}

    /** A card on the public jobs page. */
    public record JobSummary(
            Long id, String title, String location, EmploymentType employmentType,
            LocalDate closingDate, String summary) {

        public static JobSummary from(Job job) {
            return new JobSummary(job.getId(), job.getTitle(), job.getLocation(),
                    job.getEmploymentType(), job.getClosingDate(), job.getSummary());
        }
    }

    /** Full job details, used by both the public page and the admin edit form. */
    public record JobDetail(
            Long id, String title, String location, EmploymentType employmentType,
            LocalDate closingDate, String summary, String description,
            String responsibilities, String requirements, JobStatus status,
            boolean acceptingApplications, Instant createdAt, Instant updatedAt) {

        public static JobDetail from(Job job, LocalDate today) {
            return new JobDetail(job.getId(), job.getTitle(), job.getLocation(),
                    job.getEmploymentType(), job.getClosingDate(), job.getSummary(),
                    job.getDescription(), job.getResponsibilities(), job.getRequirements(),
                    job.getStatus(), job.isAcceptingApplications(today),
                    job.getCreatedAt(), job.getUpdatedAt());
        }
    }
}
