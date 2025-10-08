package by.pirog.cloud_storage_RestAPI.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("Username " + username + " already exists!");
    }
}
