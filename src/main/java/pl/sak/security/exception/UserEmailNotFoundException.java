package pl.sak.security.exception;

public class UserEmailNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "User with given email: %s not found!";

    public UserEmailNotFoundException(String username) {
        super(String.format(ERROR_MESSAGE, username));
    }
}
