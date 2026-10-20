package rw.brightcore.recruitment.application;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobIdAndEmail(Long jobId, String email);

    List<JobApplication> findByJobIdOrderByCreatedAtDesc(Long jobId);

    List<JobApplication> findByJobIdAndStatusOrderByCreatedAtDesc(Long jobId, ApplicationStatus status);

    /** Loads the job in the same query, avoiding a lazy-loading round trip on the detail page. */
    @EntityGraph(attributePaths = "job")
    Optional<JobApplication> findWithJobById(Long id);

    /** One grouped query for the whole overview, instead of one count query per job (N+1). */
    @Query("select a.job.id as jobId, a.status as status, count(a) as total "
            + "from JobApplication a group by a.job.id, a.status")
    List<StatusCount> countByJobAndStatus();

    interface StatusCount {
        Long getJobId();

        ApplicationStatus getStatus();

        long getTotal();
    }
}
