package rw.brightcore.recruitment.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import rw.brightcore.recruitment.application.ApplicationStatus;
import rw.brightcore.recruitment.application.ApplicationStatusChangedEvent;

/**
 * Mock email sender: logs the message the applicant would receive. Runs only after the
 * status change has committed, so nobody is emailed about a decision that was rolled back.
 * Swapping in a real provider (SMTP, SendGrid...) only touches this class.
 */
@Component
public class StatusNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(StatusNotificationListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(ApplicationStatusChangedEvent event) {
        String outcome = event.status() == ApplicationStatus.ACCEPTED
                ? "we are pleased to let you know that your application has been accepted"
                : "after careful consideration, we will not be moving forward with your application";
        StringBuilder body = new StringBuilder()
                .append("Dear ").append(event.fullName()).append(",\n\n")
                .append("Thank you for applying for ").append(event.jobTitle()).append(". ")
                .append(Character.toUpperCase(outcome.charAt(0))).append(outcome.substring(1)).append(".\n");
        if (event.note() != null) {
            body.append("\nNote from the hiring team: ").append(event.note()).append('\n');
        }
        body.append("\nKind regards,\nBrightCore Recruitment");

        log.info("[MOCK EMAIL] to={} subject=\"Update on your application for {}\"\n{}",
                event.email(), event.jobTitle(), body);
    }
}
