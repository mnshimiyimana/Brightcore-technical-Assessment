package rw.brightcore.recruitment.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Basic brute-force protection: after 5 failed logins for an email, further attempts are
 * refused for 15 minutes. In-memory is enough for a single instance; a cluster would use Redis.
 */
@Service
public class LoginAttemptService {

    static final int MAX_FAILURES = 5;
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private record Attempts(int failures, Instant lockedUntil) {}

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean isLocked(String email) {
        Attempts a = attempts.get(email);
        return a != null && a.lockedUntil() != null && clock.instant().isBefore(a.lockedUntil());
    }

    public void recordFailure(String email) {
        attempts.compute(email, (k, a) -> {
            int failures = (a == null || a.lockedUntil() != null ? 0 : a.failures()) + 1;
            return failures >= MAX_FAILURES
                    ? new Attempts(0, clock.instant().plus(LOCK_DURATION))
                    : new Attempts(failures, null);
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(email);
    }
}
