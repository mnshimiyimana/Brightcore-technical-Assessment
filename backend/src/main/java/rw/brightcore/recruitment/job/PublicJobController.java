package rw.brightcore.recruitment.job;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rw.brightcore.recruitment.common.PageResponse;
import rw.brightcore.recruitment.job.JobResponses.JobDetail;
import rw.brightcore.recruitment.job.JobResponses.JobSummary;

@RestController
@RequestMapping("/api/jobs")
public class PublicJobController {

    private static final int MAX_PAGE_SIZE = 50;

    private final JobService jobService;

    public PublicJobController(JobService jobService) {
        this.jobService = jobService;
    }

    /** Open jobs only, soonest closing first. {@code page} is zero-based. */
    @GetMapping
    public PageResponse<JobSummary> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EmploymentType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageResponse.from(jobService.findOpenJobs(search, type, safePage, safeSize), JobSummary::from);
    }

    /**
     * Closed jobs are still viewable (old links keep working) but report
     * {@code acceptingApplications: false} so the form is hidden.
     */
    @GetMapping("/{id}")
    public JobDetail get(@PathVariable Long id) {
        return JobDetail.from(jobService.get(id), jobService.today());
    }
}
