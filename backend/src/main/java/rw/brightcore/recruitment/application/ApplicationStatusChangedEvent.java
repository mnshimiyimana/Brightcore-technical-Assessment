package rw.brightcore.recruitment.application;

/** Published when an admin decides on an application; handled after the transaction commits. */
public record ApplicationStatusChangedEvent(
        Long applicationId, String fullName, String email, String jobTitle,
        ApplicationStatus status, String note) {
}
