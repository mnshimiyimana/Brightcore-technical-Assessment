package rw.brightcore.recruitment.application;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rw.brightcore.recruitment.application.ApplicationResponses.Receipt;

@RestController
public class PublicApplicationController {

    private final ApplicationService applicationService;

    public PublicApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Accepts multipart/form-data: the text fields of {@link ApplicationForm} plus a {@code cv} file.
     * The CV is optional at the binding level so a missing file gets the same friendly
     * field error as an invalid one.
     */
    @PostMapping(path = "/api/jobs/{jobId}/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Receipt apply(@PathVariable Long jobId,
                         @Valid @ModelAttribute ApplicationForm form,
                         @RequestParam(name = "cv", required = false) MultipartFile cv) {
        JobApplication application = applicationService.apply(jobId, form, cv);
        return new Receipt(application.getId(), application.getJob().getTitle(),
                "Thank you, your application has been received.");
    }
}
