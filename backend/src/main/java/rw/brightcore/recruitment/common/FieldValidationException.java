package rw.brightcore.recruitment.common;

import java.util.Map;

/** Business-rule validation failure tied to specific form fields (HTTP 400). */
public class FieldValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public FieldValidationException(String field, String message) {
        super(message);
        this.errors = Map.of(field, message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
