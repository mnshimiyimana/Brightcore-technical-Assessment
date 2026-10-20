package rw.brightcore.recruitment.admin;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.brightcore.recruitment.application.ApplicationStatus;
import rw.brightcore.recruitment.application.JobApplicationRepository;
import rw.brightcore.recruitment.job.EmploymentType;
import rw.brightcore.recruitment.job.Job;
import rw.brightcore.recruitment.job.JobService;
import rw.brightcore.recruitment.job.JobStatus;

/** Builds the admin jobs table and the overview counts from one grouped query. */
@Service
public class OverviewService {

    public record Counts(long total, long pending, long accepted, long rejected) {

        static Counts from(Map<ApplicationStatus, Long> byStatus) {
            long pending = byStatus.getOrDefault(ApplicationStatus.PENDING, 0L);
            long accepted = byStatus.getOrDefault(ApplicationStatus.ACCEPTED, 0L);
            long rejected = byStatus.getOrDefault(ApplicationStatus.REJECTED, 0L);
            return new Counts(pending + accepted + rejected, pending, accepted, rejected);
        }
    }

    public record JobRow(
            Long id, String title, String location, EmploymentType employmentType, LocalDate closingDate,
            JobStatus status, boolean acceptingApplications, Counts applications) {}

    public record Overview(Counts totals, List<JobRow> jobs) {}

    private final JobService jobService;
    private final JobApplicationRepository applications;

    public OverviewService(JobService jobService, JobApplicationRepository applications) {
        this.jobService = jobService;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public Overview overview() {
        Map<Long, Map<ApplicationStatus, Long>> countsByJob = new HashMap<>();
        Map<ApplicationStatus, Long> totals = new EnumMap<>(ApplicationStatus.class);
        for (var row : applications.countByJobAndStatus()) {
            countsByJob.computeIfAbsent(row.getJobId(), id -> new EnumMap<>(ApplicationStatus.class))
                    .put(row.getStatus(), row.getTotal());
            totals.merge(row.getStatus(), row.getTotal(), Long::sum);
        }

        LocalDate today = jobService.today();
        List<JobRow> rows = jobService.findAllForAdmin().stream()
                .map(job -> toRow(job, today, Counts.from(countsByJob.getOrDefault(job.getId(), Map.of()))))
                .toList();
        return new Overview(Counts.from(totals), rows);
    }

    private static JobRow toRow(Job job, LocalDate today, Counts counts) {
        return new JobRow(job.getId(), job.getTitle(), job.getLocation(), job.getEmploymentType(),
                job.getClosingDate(), job.getStatus(), job.isAcceptingApplications(today), counts);
    }
}
