package rw.brightcore.recruitment.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rw.brightcore.recruitment.application.CvStorageService.StoredCv;
import rw.brightcore.recruitment.common.ConflictException;
import rw.brightcore.recruitment.common.FieldValidationException;
import rw.brightcore.recruitment.common.NotFoundException;
import rw.brightcore.recruitment.job.Job;
import rw.brightcore.recruitment.job.JobService;

@Service
public class ApplicationService {

    static final String DUPLICATE_MESSAGE = "You have already applied for this job with this email address.";
    static final String CLOSED_MESSAGE = "This job is no longer accepting applications.";

    private final JobApplicationRepository applications;
    private final JobService jobService;
    private final CvStorageService cvStorage;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public ApplicationService(JobApplicationRepository applications, JobService jobService,
                              CvStorageService cvStorage, ApplicationEventPublisher events, Clock clock) {
        this.applications = applications;
        this.jobService = jobService;
        this.cvStorage = cvStorage;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public JobApplication apply(Long jobId, ApplicationForm form, MultipartFile cv) {
        Job job = jobService.get(jobId);
        if (!job.isAcceptingApplications(jobService.today())) {
            throw new ConflictException(CLOSED_MESSAGE);
        }
        cvStorage.validate(cv);

        String email = form.email().trim().toLowerCase(Locale.ROOT);
        // Friendly early check. The unique constraint below still catches two simultaneous submissions.
        if (applications.existsByJobIdAndEmail(jobId, email)) {
            throw new ConflictException(DUPLICATE_MESSAGE);
        }

        StoredCv stored = cvStorage.store(cv);
        try {
            JobApplication application = new JobApplication();
            application.setJob(job);
            application.setFullName(form.fullName().trim());
            application.setEmail(email);
            application.setPhone(normalisePhone(form.phone()));
            application.setCoverNote(form.coverNote().trim());
            application.setCvStorageKey(stored.key());
            application.setCvOriginalName(stored.originalName());
            application.setCvSizeBytes(stored.size());
            Instant now = clock.instant();
            application.setCreatedAt(now);
            application.setUpdatedAt(now);
            // Flush now so a constraint violation surfaces here, while we can still clean up the file.
            return applications.saveAndFlush(application);
        } catch (DataIntegrityViolationException e) {
            cvStorage.delete(stored.key());
            throw new ConflictException(DUPLICATE_MESSAGE);
        } catch (RuntimeException e) {
            cvStorage.delete(stored.key());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<JobApplication> findForJob(Long jobId, ApplicationStatus status) {
        jobService.get(jobId); // 404 if the job does not exist
        return status == null
                ? applications.findByJobIdOrderByCreatedAtDesc(jobId)
                : applications.findByJobIdAndStatusOrderByCreatedAtDesc(jobId, status);
    }

    @Transactional(readOnly = true)
    public JobApplication get(Long id) {
        return applications.findWithJobById(id)
                .orElseThrow(() -> new NotFoundException("Application not found."));
    }

    /**
     * Every application starts as PENDING and can be moved to ACCEPTED or REJECTED.
     * A decision may be changed later (e.g. to fix a mistake), but never back to PENDING.
     */
    @Transactional
    public JobApplication changeStatus(Long id, StatusUpdateRequest request) {
        if (request.status() == ApplicationStatus.PENDING) {
            throw new FieldValidationException("status", "An application can only be accepted or rejected.");
        }
        JobApplication application = get(id);
        ApplicationStatus previous = application.getStatus();
        String note = request.note() == null || request.note().isBlank() ? null : request.note().trim();

        application.setStatus(request.status());
        application.setDecisionNote(note);
        application.setUpdatedAt(clock.instant());

        if (previous != request.status()) {
            events.publishEvent(new ApplicationStatusChangedEvent(application.getId(),
                    application.getFullName(), application.getEmail(), application.getJob().getTitle(),
                    application.getStatus(), note));
        }
        return application;
    }

    /** Stores phone numbers without spaces or dashes, e.g. "+250 788-123 456" becomes "+250788123456". */
    static String normalisePhone(String phone) {
        return phone.replaceAll("[ -]", "");
    }
}
