package rw.brightcore.recruitment.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/** The single error shape returned by every endpoint. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(int status, String message, Map<String, String> errors) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, Map.of());
    }
}
