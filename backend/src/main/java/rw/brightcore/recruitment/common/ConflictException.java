package rw.brightcore.recruitment.common;

/** A request that is valid on its own but clashes with the current state (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
