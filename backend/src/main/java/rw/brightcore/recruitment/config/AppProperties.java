package rw.brightcore.recruitment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed view of the {@code app.*} settings in application.yml. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String timezone,
        String uploadsDir,
        long cvMaxBytes,
        Jwt jwt,
        Cookie cookie,
        Admin admin,
        boolean seedSampleJobs) {

    public record Jwt(String secret, long ttlMinutes) {}

    public record Cookie(boolean secure) {}

    public record Admin(String email, String password) {}
}
