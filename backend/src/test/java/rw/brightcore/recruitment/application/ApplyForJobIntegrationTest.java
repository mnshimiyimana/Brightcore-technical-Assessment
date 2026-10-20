package rw.brightcore.recruitment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import rw.brightcore.recruitment.IntegrationTestSupport;
import rw.brightcore.recruitment.TestClockConfig;
import rw.brightcore.recruitment.job.Job;
import rw.brightcore.recruitment.job.JobStatus;

class ApplyForJobIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JobApplicationRepository applications;

    @Test
    void validApplicationIsSavedAsPending() throws Exception {
        Job job = saveOpenJob();

        mvc.perform(applyRequest(job.getId(), "Aline@Example.com", pdf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobTitle").value("Developer"));

        var saved = applications.findByJobIdOrderByCreatedAtDesc(job.getId());
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(saved.get(0).getEmail()).isEqualTo("aline@example.com");
        assertThat(saved.get(0).getPhone()).isEqualTo("+250788123456");
        assertThat(saved.get(0).getCvOriginalName()).isEqualTo("My CV.pdf");
    }

    @Test
    void sameEmailCannotApplyTwiceToTheSameJobRegardlessOfCase() throws Exception {
        Job job = saveOpenJob();
        mvc.perform(applyRequest(job.getId(), "aline@example.com", pdf())).andExpect(status().isCreated());

        mvc.perform(applyRequest(job.getId(), "ALINE@Example.com", pdf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ApplicationService.DUPLICATE_MESSAGE));
    }

    @Test
    void sameEmailMayApplyToDifferentJobs() throws Exception {
        Job first = saveOpenJob();
        Job second = saveOpenJob();
        mvc.perform(applyRequest(first.getId(), "aline@example.com", pdf())).andExpect(status().isCreated());
        mvc.perform(applyRequest(second.getId(), "aline@example.com", pdf())).andExpect(status().isCreated());
    }

    @Test
    void cannotApplyToAClosedJob() throws Exception {
        Job job = saveJob("Closed", TestClockConfig.TODAY.plusDays(10), JobStatus.CLOSED);

        mvc.perform(applyRequest(job.getId(), "aline@example.com", pdf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ApplicationService.CLOSED_MESSAGE));
    }

    @Test
    void cannotApplyAfterTheClosingDate() throws Exception {
        Job job = saveJob("Expired", TestClockConfig.TODAY.minusDays(1), JobStatus.OPEN);

        mvc.perform(applyRequest(job.getId(), "aline@example.com", pdf()))
                .andExpect(status().isConflict());
        assertThat(applications.count()).isZero();
    }

    @Test
    void canStillApplyOnTheClosingDate() throws Exception {
        Job job = saveJob("Last day", TestClockConfig.TODAY, JobStatus.OPEN);
        mvc.perform(applyRequest(job.getId(), "aline@example.com", pdf())).andExpect(status().isCreated());
    }

    @Test
    void invalidFieldsReturnFieldErrors() throws Exception {
        Job job = saveOpenJob();

        mvc.perform(multipart("/api/jobs/{id}/applications", job.getId())
                        .file(pdf())
                        .param("fullName", "")
                        .param("email", "not-an-email")
                        .param("phone", "abc")
                        .param("coverNote", "Hello"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Enter a valid email address."))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    void missingCvIsRejected() throws Exception {
        Job job = saveOpenJob();
        mvc.perform(applyRequest(job.getId(), "aline@example.com", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cv").value("Please attach your CV as a PDF."));
    }

    @Test
    void nonPdfCvIsRejected() throws Exception {
        Job job = saveOpenJob();
        var word = new MockMultipartFile("cv", "cv.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "PK..".getBytes());

        mvc.perform(applyRequest(job.getId(), "aline@example.com", word))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cv").value("The CV must be a PDF file."));
    }

    @Test
    void applyingToAnUnknownJobReturns404() throws Exception {
        mvc.perform(applyRequest(999_999L, "aline@example.com", pdf())).andExpect(status().isNotFound());
    }

    @Test
    void publicListShowsOnlyJobsAcceptingApplications() throws Exception {
        saveOpenJob();
        saveJob("Closed", TestClockConfig.TODAY.plusDays(10), JobStatus.CLOSED);
        saveJob("Expired", TestClockConfig.TODAY.minusDays(1), JobStatus.OPEN);

        mvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].title").value("Developer"));
    }

    @Test
    void publicSearchTreatsWildcardsLiterally() throws Exception {
        saveOpenJob();
        mvc.perform(get("/api/jobs").param("search", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
        mvc.perform(get("/api/jobs").param("search", "develop"))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }
}
