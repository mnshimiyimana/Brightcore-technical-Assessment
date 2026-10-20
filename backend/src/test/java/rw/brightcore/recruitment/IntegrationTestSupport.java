package rw.brightcore.recruitment;

import java.time.Instant;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import rw.brightcore.recruitment.job.EmploymentType;
import rw.brightcore.recruitment.job.Job;
import rw.brightcore.recruitment.job.JobRepository;
import rw.brightcore.recruitment.job.JobStatus;

/** Boots the full app against in-memory H2; each test runs in a transaction that is rolled back. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClockConfig.class)
@Transactional
public abstract class IntegrationTestSupport {

    protected static final byte[] PDF_BYTES = "%PDF-1.4\n% test cv\n%%EOF".getBytes();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JobRepository jobRepository;

    protected Job saveJob(String title, LocalDate closingDate, JobStatus status) {
        Job job = new Job();
        job.setTitle(title);
        job.setLocation("Kigali");
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setSummary("Summary of " + title);
        job.setDescription("Description");
        job.setResponsibilities("Do things");
        job.setRequirements("Know things");
        job.setClosingDate(closingDate);
        job.setStatus(status);
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        return jobRepository.save(job);
    }

    protected Job saveOpenJob() {
        return saveJob("Developer", TestClockConfig.TODAY.plusDays(10), JobStatus.OPEN);
    }

    protected static MockMultipartFile pdf() {
        return new MockMultipartFile("cv", "My CV.pdf", "application/pdf", PDF_BYTES);
    }

    /** A valid application request; tests override single fields to check validation. */
    protected static MockMultipartHttpServletRequestBuilder applyRequest(Long jobId, String email,
                                                                         MockMultipartFile cv) {
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .multipart("/api/jobs/{id}/applications", jobId);
        if (cv != null) {
            builder.file(cv);
        }
        builder.param("fullName", "Aline Uwase")
                .param("email", email)
                .param("phone", "+250 788 123 456")
                .param("coverNote", "I would love to join BrightCore.");
        return builder;
    }
}
