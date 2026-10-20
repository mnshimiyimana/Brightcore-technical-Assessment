package rw.brightcore.recruitment.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.brightcore.recruitment.common.ApiError;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(
            @NotBlank(message = "Email is required.") String email,
            @NotBlank(message = "Password is required.") String password) {}

    public record AdminResponse(String email) {}

    private final AdminUserRepository admins;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthCookies cookies;
    private final LoginAttemptService loginAttempts;
    /** Compared against when the email is unknown, so response time does not reveal which emails exist. */
    private final String dummyHash;

    public AuthController(AdminUserRepository admins, PasswordEncoder passwordEncoder, JwtService jwtService,
                          AuthCookies cookies, LoginAttemptService loginAttempts) {
        this.admins = admins;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.cookies = cookies;
        this.loginAttempts = loginAttempts;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (loginAttempts.isLocked(email)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiError.of(429, "Too many failed attempts. Please try again in 15 minutes."));
        }

        AdminUser admin = admins.findByEmail(email).orElse(null);
        String hash = admin != null ? admin.getPasswordHash() : dummyHash;
        if (!passwordEncoder.matches(request.password(), hash) || admin == null) {
            loginAttempts.recordFailure(email);
            throw new BadCredentialsException("Invalid credentials");
        }

        loginAttempts.recordSuccess(email);
        String token = jwtService.issue(admin.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.create(token, jwtService.ttl()).toString())
                .body(new AdminResponse(admin.getEmail()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
                .build();
    }

    /** Lets the front end check whether the admin session is still valid. */
    @GetMapping("/me")
    public AdminResponse me(Principal principal) {
        return new AdminResponse(principal.getName());
    }
}
