package rw.brightcore.recruitment.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import rw.brightcore.recruitment.job.Job;

/** Named JobApplication to avoid confusion with Spring's own "Application" types. */
@Entity
@Table(name = "applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** Always stored lower-cased so the (job_id, email) unique constraint is case-insensitive. */
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "cover_note", nullable = false)
    private String coverNote;

    /** Random file name on disk; never derived from user input. */
    @Column(name = "cv_storage_key", nullable = false)
    private String cvStorageKey;

    @Column(name = "cv_original_name", nullable = false)
    private String cvOriginalName;

    @Column(name = "cv_size_bytes", nullable = false)
    private long cvSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "decision_note")
    private String decisionNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }

    public Job getJob() { return job; }

    public void setJob(Job job) { this.job = job; }

    public String getFullName() { return fullName; }

    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getCoverNote() { return coverNote; }

    public void setCoverNote(String coverNote) { this.coverNote = coverNote; }

    public String getCvStorageKey() { return cvStorageKey; }

    public void setCvStorageKey(String cvStorageKey) { this.cvStorageKey = cvStorageKey; }

    public String getCvOriginalName() { return cvOriginalName; }

    public void setCvOriginalName(String cvOriginalName) { this.cvOriginalName = cvOriginalName; }

    public long getCvSizeBytes() { return cvSizeBytes; }

    public void setCvSizeBytes(long cvSizeBytes) { this.cvSizeBytes = cvSizeBytes; }

    public ApplicationStatus getStatus() { return status; }

    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getDecisionNote() { return decisionNote; }

    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }

    public Instant getCreatedAt() { return createdAt; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
