package oculus.aperture.spi.store;

/**
 * Stub exception class to replace missing ApertureJS dependency
 */
public class ConflictException extends Exception {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}