package rw.brightcore.recruitment.auth;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import rw.brightcore.recruitment.config.AppProperties;

/**
 * The token lives in an HttpOnly cookie, so page scripts (and any XSS) cannot read it,
 * unlike localStorage. SameSite=Strict stops browsers sending it on cross-site requests,
 * which is our CSRF protection.
 */
@Component
public class AuthCookies {

    public static final String NAME = "admin_token";

    private final boolean secure;

    public AuthCookies(AppProperties props) {
        this.secure = props.cookie().secure();
    }

    public ResponseCookie create(String token, Duration maxAge) {
        return base(token).maxAge(maxAge).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/");
    }
}
