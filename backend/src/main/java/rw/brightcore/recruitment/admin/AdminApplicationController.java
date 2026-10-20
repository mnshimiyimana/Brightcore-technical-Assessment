package rw.brightcore.recruitment.admin;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.brightcore.recruitment.application.ApplicationResponses.ApplicationDetail;
import rw.brightcore.recruitment.application.ApplicationService;
import rw.brightcore.recruitment.application.CvStorageService;
import rw.brightcore.recruitment.application.JobApplication;
import rw.brightcore.recruitment.application.StatusUpdateRequest;

@RestController
@RequestMapping("/api/admin/applications")
public class AdminApplicationController {

    private final ApplicationService applicationService;
    private final CvStorageService cvStorage;

    public AdminApplicationController(ApplicationService applicationService, CvStorageService cvStorage) {
        this.applicationService = applicationService;
        this.cvStorage = cvStorage;
    }

    @GetMapping("/{id}")
    public ApplicationDetail get(@PathVariable Long id) {
        return ApplicationDetail.from(applicationService.get(id));
    }

    @PatchMapping("/{id}/status")
    public ApplicationDetail changeStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return ApplicationDetail.from(applicationService.changeStatus(id, request));
    }

    /**
     * Streams the CV as a download. "attachment" plus the nosniff header (added by Spring
     * Security) stops the browser rendering anything unexpected inline.
     */
    @GetMapping("/{id}/cv")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long id) {
        JobApplication application = applicationService.get(id);
        Resource file = cvStorage.load(application.getCvStorageKey());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(application.getCvSizeBytes())
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(application.getCvOriginalName())
                        .build()
                        .toString())
                .body(file);
    }
}
