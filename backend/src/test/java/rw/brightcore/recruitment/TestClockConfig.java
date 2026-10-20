package rw.brightcore.recruitment;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Freezes "now" so closing-date rules are deterministic in tests. */
@TestConfiguration
public class TestClockConfig {

    public static final ZoneId ZONE = ZoneId.of("Africa/Kigali");
    public static final LocalDate TODAY = LocalDate.of(2026, 6, 1);

    @Bean
    @Primary
    public Clock fixedClock() {
        return Clock.fixed(TODAY.atTime(10, 0).atZone(ZONE).toInstant(), ZONE);
    }
}
