package rw.brightcore.recruitment.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A single injectable Clock so "today" (used for closing dates) is consistent
 * across the app and can be fixed in tests.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(AppProperties props) {
        return Clock.system(ZoneId.of(props.timezone()));
    }
}
