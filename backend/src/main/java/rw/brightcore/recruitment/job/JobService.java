package rw.brightcore.recruitment.job;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.brightcore.recruitment.common.FieldValidationException;
import rw.brightcore.recruitment.common.NotFoundException;

@Service
public class JobService {

    private final JobRepository jobs;
    private final Clock clock;

    public JobService(JobRepository jobs, Clock clock) {
        this.jobs = jobs;
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    @Transactional(readOnly = true)
    public Page<Job> findOpenJobs(String search, EmploymentType type, int page, int size) {
        Specification<Job> spec = Specification.where(JobSpecifications.acceptingApplications(today()))
                .and(JobSpecifications.hasType(type))
                .and(JobSpecifications.matches(search));
        var pageable = PageRequest.of(page, size, Sort.by("closingDate").ascending().and(Sort.by("id")));
        return jobs.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Job> findAllForAdmin() {
        return jobs.findAll(Sort.by("createdAt").descending());
    }

    @Transactional(readOnly = true)
    public Job get(Long id) {
        return jobs.findById(id).orElseThrow(() -> new NotFoundException("Job not found."));
    }

    @Transactional
    public Job create(JobRequest request) {
        requireClosingDateNotPast(request.closingDate());
        Job job = new Job();
        apply(job, request);
        Instant now = clock.instant();
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        return jobs.save(job);
    }

    @Transactional
    public Job update(Long id, JobRequest request) {
        Job job = get(id);
        // Only check the date if it changed, so typos can still be fixed on old jobs.
        if (!request.closingDate().equals(job.getClosingDate())) {
            requireClosingDateNotPast(request.closingDate());
        }
        apply(job, request);
        job.setUpdatedAt(clock.instant());
        return job;
    }

    @Transactional
    public Job changeStatus(Long id, JobStatus status) {
        Job job = get(id);
        job.setStatus(status);
        job.setUpdatedAt(clock.instant());
        return job;
    }

    private void requireClosingDateNotPast(LocalDate closingDate) {
        if (closingDate.isBefore(today())) {
            throw new FieldValidationException("closingDate", "Closing date cannot be in the past.");
        }
    }

    private static void apply(Job job, JobRequest r) {
        job.setTitle(r.title().trim());
        job.setLocation(r.location().trim());
        job.setEmploymentType(r.employmentType());
        job.setSummary(r.summary().trim());
        job.setDescription(r.description().trim());
        job.setResponsibilities(r.responsibilities().trim());
        job.setRequirements(r.requirements().trim());
        job.setClosingDate(r.closingDate());
    }
}
