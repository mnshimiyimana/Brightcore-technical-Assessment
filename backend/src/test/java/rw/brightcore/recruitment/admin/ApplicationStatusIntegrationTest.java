package rw.brightcore.recruitment.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import rw.brightcore.recruitment.IntegrationTestSupport;
import rw.brightcore.recruitment.job.Job;

class ApplicationStatusIntegrationTest extends IntegrationTestSupport {

    private static final RequestPostProcessor ADMIN = user("admin@test.rw").roles("ADMIN");

    private Job job;
    private long applicationId;

    @BeforeEach
    void apply() throws Exception {
        job = saveOpenJob();
        String body = mvc.perform(applyRequest(job.getId(), "aline@example.com", pdf()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        applicationId = ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void newApplicationStartsAsPending() throws Exception {
        mvc.perform(get("/api/admin/applications/{id}", applicationId).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.jobTitle").value("Developer"));
    }

    @Test
    void adminCanAcceptWithANote() throws Exception {
        mvc.perform(patch("/api/admin/applications/{id}/status", applicationId).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\",\"note\":\"  Strong portfolio  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.decisionNote").value("Strong portfolio"));
    }

    @Test
    void adminCanRejectWithoutANote() throws Exception {
        mvc.perform(patch("/api/admin/applications/{id}/status", applicationId).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.decisionNote").doesNotExist());
    }

    @Test
    void applicationCannotBeMovedBackToPending() throws Exception {
        mvc.perform(patch("/api/admin/applications/{id}/status", applicationId).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDING\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());
    }

    @Test
    void applicantsCanBeFilteredByStatus() throws Exception {
        mvc.perform(applyRequest(job.getId(), "eric@example.com", pdf())).andExpect(status().isCreated());
        mvc.perform(patch("/api/admin/applications/{id}/status", applicationId).with(ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACCEPTED\"}"));

        mvc.perform(get("/api/admin/jobs/{id}/applications", job.getId()).param("status", "PENDING").with(ADMIN))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("eric@example.com"));
        mvc.perform(get("/api/admin/jobs/{id}/applications", job.getId()).with(ADMIN))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void overviewCountsApplicationsPerJobAndStatus() throws Exception {
        mvc.perform(applyRequest(job.getId(), "eric@example.com", pdf())).andExpect(status().isCreated());
        mvc.perform(patch("/api/admin/applications/{id}/status", applicationId).with(ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"REJECTED\"}"));

        mvc.perform(get("/api/admin/overview").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.total").value(2))
                .andExpect(jsonPath("$.totals.pending").value(1))
                .andExpect(jsonPath("$.totals.rejected").value(1))
                .andExpect(jsonPath("$.jobs[0].applications.total").value(2));
    }

    @Test
    void adminCanDownloadTheCv() throws Exception {
        mvc.perform(get("/api/admin/applications/{id}/cv", applicationId).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"My CV.pdf\""))
                .andExpect(content().bytes(PDF_BYTES));
    }
}
