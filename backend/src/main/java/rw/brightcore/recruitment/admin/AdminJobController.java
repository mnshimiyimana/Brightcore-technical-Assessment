package rw.brightcore.recruitment.admin;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rw.brightcore.recruitment.admin.OverviewService.JobRow;
import rw.brightcore.recruitment.application.ApplicationResponses.ApplicationSummary;
import rw.brightcore.recruitment.application.ApplicationService;
import rw.brightcore.recruitment.application.ApplicationStatus;
import rw.brightcore.recruitment.job.Job;
import rw.brightcore.recruitment.job.JobRequest;
import rw.brightcore.recruitment.job.JobResponses.JobDetail;
import rw.brightcore.recruitment.job.JobService;
import rw.brightcore.recruitment.job.JobStatus;

@RestController
@RequestMapping("/api/admin/jobs")
public class AdminJobController {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final OverviewService overviewService;

    public AdminJobController(JobService jobService, ApplicationService applicationService,
                              OverviewService overviewService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.overviewService = overviewService;
    }

    /** All jobs (open and closed) with their application counts. */
    @GetMapping
    public List<JobRow> list() {
        return overviewService.overview().jobs();
    }

    @GetMapping("/{id}")
    public JobDetail get(@PathVariable Long id) {
        return detail(jobService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobDetail create(@Valid @RequestBody JobRequest request) {
        return detail(jobService.create(request));
    }

    @PutMapping("/{id}")
    public JobDetail update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return detail(jobService.update(id, request));
    }

    @PostMapping("/{id}/close")
    public JobDetail close(@PathVariable Long id) {
        return detail(jobService.changeStatus(id, JobStatus.CLOSED));
    }

    @PostMapping("/{id}/reopen")
    public JobDetail reopen(@PathVariable Long id) {
        return detail(jobService.changeStatus(id, JobStatus.OPEN));
    }

    @GetMapping("/{id}/applications")
    public List<ApplicationSummary> applications(@PathVariable Long id,
                                                 @RequestParam(required = false) ApplicationStatus status) {
        return applicationService.findForJob(id, status).stream().map(ApplicationSummary::from).toList();
    }

    private JobDetail detail(Job job) {
        return JobDetail.from(job, jobService.today());
    }
}
