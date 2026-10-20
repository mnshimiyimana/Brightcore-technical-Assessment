package rw.brightcore.recruitment.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.brightcore.recruitment.admin.OverviewService.Overview;

@RestController
public class AdminOverviewController {

    private final OverviewService overviewService;

    public AdminOverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    /** Application counts per job and per status, plus overall totals. */
    @GetMapping("/api/admin/overview")
    public Overview overview() {
        return overviewService.overview();
    }
}
