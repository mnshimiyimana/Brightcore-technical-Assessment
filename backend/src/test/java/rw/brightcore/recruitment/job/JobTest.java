package rw.brightcore.recruitment.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class JobTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);

    private static Job job(JobStatus status, LocalDate closingDate) {
        Job job = new Job();
        job.setStatus(status);
        job.setClosingDate(closingDate);
        return job;
    }

    @Test
    void openJobBeforeClosingDateAcceptsApplications() {
        assertThat(job(JobStatus.OPEN, TODAY.plusDays(1)).isAcceptingApplications(TODAY)).isTrue();
    }

    @Test
    void closingDateIsInclusive() {
        assertThat(job(JobStatus.OPEN, TODAY).isAcceptingApplications(TODAY)).isTrue();
    }

    @Test
    void jobPastClosingDateDoesNotAcceptApplications() {
        assertThat(job(JobStatus.OPEN, TODAY.minusDays(1)).isAcceptingApplications(TODAY)).isFalse();
    }

    @Test
    void closedJobDoesNotAcceptApplicationsEvenBeforeClosingDate() {
        assertThat(job(JobStatus.CLOSED, TODAY.plusDays(5)).isAcceptingApplications(TODAY)).isFalse();
    }
}
