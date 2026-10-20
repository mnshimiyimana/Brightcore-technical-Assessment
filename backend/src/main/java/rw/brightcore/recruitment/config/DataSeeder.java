package rw.brightcore.recruitment.config;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.brightcore.recruitment.auth.AdminUser;
import rw.brightcore.recruitment.auth.AdminUserRepository;
import rw.brightcore.recruitment.job.EmploymentType;
import rw.brightcore.recruitment.job.Job;
import rw.brightcore.recruitment.job.JobRepository;
import rw.brightcore.recruitment.job.JobStatus;

/**
 * Seeds the admin account and sample jobs on start-up. Done in code rather than a SQL migration
 * so the password is hashed properly and closing dates are relative to "today" (they stay open
 * whenever a reviewer runs the app).
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppProperties props;
    private final AdminUserRepository admins;
    private final JobRepository jobs;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public DataSeeder(AppProperties props, AdminUserRepository admins, JobRepository jobs,
                      PasswordEncoder passwordEncoder, Clock clock) {
        this.props = props;
        this.admins = admins;
        this.jobs = jobs;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmin();
        if (props.seedSampleJobs() && jobs.count() == 0) {
            seedJobs();
        }
    }

    private void seedAdmin() {
        String email = props.admin().email().trim().toLowerCase(Locale.ROOT);
        if (admins.findByEmail(email).isEmpty()) {
            admins.save(new AdminUser(email, passwordEncoder.encode(props.admin().password()), clock.instant()));
            log.info("Seeded admin account {}", email);
        }
    }

    private void seedJobs() {
        LocalDate today = LocalDate.now(clock);
        jobs.save(job("Full Stack Software Developer", "Kigali, Rwanda", EmploymentType.FULL_TIME,
                today.plusDays(30),
                "Build and maintain web platforms for SACCOs and financial institutions using Next.js and Spring Boot.",
                "BrightCore is looking for a full stack developer to join the team building digital "
                        + "products for savings groups, SACCOs and microfinance institutions across the region. "
                        + "You will work across the stack, from database design to polished user interfaces.",
                """
                        Design, build and maintain REST APIs with Spring Boot
                        Build responsive front ends with React and Next.js
                        Integrate with mobile money, SMS and core banking systems
                        Write automated tests and take part in code reviews
                        Work directly with clients to understand their requirements""",
                """
                        Degree in Computer Science or equivalent experience
                        2+ years building production web applications
                        Good knowledge of Java, TypeScript and SQL
                        Experience with Git, Docker and CI/CD
                        Clear written and spoken English; Kinyarwanda is a plus"""));

        jobs.save(job("UI/UX Designer", "Kigali, Rwanda (Hybrid)", EmploymentType.CONTRACT,
                today.plusDays(21),
                "Design simple, accessible experiences for web and USSD channels used by thousands of members.",
                "We need a designer who can turn complex financial processes into simple, accessible "
                        + "experiences. You will design for web, mobile and USSD, often for first-time digital users.",
                """
                        Run user research with SACCO members and staff
                        Produce wireframes, prototypes and final designs in Figma
                        Maintain and extend our design system
                        Work closely with developers through implementation""",
                """
                        Portfolio showing web or mobile product work
                        3+ years of product design experience
                        Understanding of accessibility standards
                        Experience designing for low-bandwidth or low-literacy contexts is a plus"""));

        jobs.save(job("Customer Success Intern", "Musanze, Rwanda", EmploymentType.INTERNSHIP,
                today.plusDays(14),
                "Support SACCO clients during onboarding and help them get the most out of BrightCore products.",
                "A six-month paid internship for a recent graduate who enjoys helping people and solving "
                        + "problems. You will be the first point of contact for SACCOs adopting our platform.",
                """
                        Answer client questions by phone, email and WhatsApp
                        Help run training sessions for SACCO staff
                        Log issues and follow them up with the product team
                        Write simple help guides""",
                """
                        Recent graduate in any field
                        Excellent communication in Kinyarwanda and English
                        Comfortable with computers and smartphones
                        Patient and organised"""));

        Job closed = job("Data Analyst", "Remote (Rwanda)", EmploymentType.PART_TIME,
                today.plusDays(10),
                "Turn loan and savings data into reports that help SACCOs make better decisions.",
                "Part-time role analysing portfolio data and building reports for our SACCO partners.",
                """
                        Build dashboards on loan performance
                        Clean and validate data from partner systems""",
                """
                        Strong SQL and spreadsheet skills
                        Experience with a BI tool such as Metabase or Power BI""");
        closed.setStatus(JobStatus.CLOSED);
        jobs.save(closed);

        log.info("Seeded sample jobs");
    }

    private Job job(String title, String location, EmploymentType type, LocalDate closingDate, String summary,
                    String description, String responsibilities, String requirements) {
        Job job = new Job();
        job.setTitle(title);
        job.setLocation(location);
        job.setEmploymentType(type);
        job.setClosingDate(closingDate);
        job.setSummary(summary);
        job.setDescription(description);
        job.setResponsibilities(responsibilities);
        job.setRequirements(requirements);
        job.setCreatedAt(clock.instant());
        job.setUpdatedAt(clock.instant());
        return job;
    }
}
