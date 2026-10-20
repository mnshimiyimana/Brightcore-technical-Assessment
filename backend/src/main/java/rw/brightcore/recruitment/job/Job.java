package rw.brightcore.recruitment.job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private String description;

    /** One item per line; the UI renders each line as a bullet. */
    @Column(nullable = false)
    private String responsibilities;

    /** One item per line; the UI renders each line as a bullet. */
    @Column(nullable = false)
    private String requirements;

    @Column(name = "closing_date", nullable = false)
    private LocalDate closingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * The single rule for "can people apply?": the admin has not closed it and
     * the closing date (inclusive) has not passed.
     */
    public boolean isAcceptingApplications(LocalDate today) {
        return status == JobStatus.OPEN && !closingDate.isBefore(today);
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public EmploymentType getEmploymentType() { return employmentType; }

    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public String getSummary() { return summary; }

    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getResponsibilities() { return responsibilities; }

    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }

    public String getRequirements() { return requirements; }

    public void setRequirements(String requirements) { this.requirements = requirements; }

    public LocalDate getClosingDate() { return closingDate; }

    public void setClosingDate(LocalDate closingDate) { this.closingDate = closingDate; }

    public JobStatus getStatus() { return status; }

    public void setStatus(JobStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
