package handler;

import com.vadastore.music_store_api.exceptions.*;
import com.vadastore.music_store_api.record.CustomError;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidCredentialsException.class})
    public ResponseEntity<Object> invalidCredentialsExceptionHandler(InvalidCredentialsException exception) {
        CustomError customError = new CustomError(HttpStatus.UNAUTHORIZED.value(), exception.getLocalizedMessage());

        return ResponseEntity
                .status(customError.statusCode())
                .body(customError);
    }

    @ExceptionHandler({UserAlreadyExistsException.class})
    public ResponseEntity<Object> userAlreadyExistsExceptionHandler(UserAlreadyExistsException exception) {
        CustomError customError = new CustomError(HttpStatus.CONFLICT.value(), exception.getLocalizedMessage());

        return ResponseEntity
                .status(customError.statusCode())
                .body(customError);
    }

    @ExceptionHandler({InsufficientStockException.class})
    public ResponseEntity<Object> insufficientStockException(InsufficientStockException exception) {
        CustomError customError = new CustomError(HttpStatus.CONFLICT.value(), exception.getLocalizedMessage());

        return ResponseEntity
                .status(customError.statusCode())
                .body(customError);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<Object> entityNotFoundException(EntityNotFoundException exception) {
        CustomError customError = new CustomError(HttpStatus.NOT_FOUND.value(), exception.getLocalizedMessage());


        return ResponseEntity
                .status(customError.statusCode())
                .body(customError);
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<Object> resourceNotFoundException(ResourceNotFoundException exception) {
        CustomError customError = new CustomError(HttpStatus.NOT_FOUND.value(), exception.getLocalizedMessage());

        return ResponseEntity.status(customError.statusCode()).body(customError);
    }

    @ExceptionHandler({TokenRefreshException.class})
    public ResponseEntity<Object> tokenRefreshException(TokenRefreshException exception) {
        CustomError customError = new CustomError(HttpStatus.BAD_REQUEST.value(), exception.getLocalizedMessage());

        return ResponseEntity.status(customError.statusCode()).body(customError);
    }

    @ExceptionHandler({IllegalStateException.class})
    public ResponseEntity<Object> illegalStateException(IllegalStateException exception) {
        CustomError customError = new CustomError(HttpStatus.BAD_REQUEST.value(), exception.getLocalizedMessage());

        return ResponseEntity.status(customError.statusCode()).body(customError);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> illegalArgumentException(IllegalArgumentException exception) {
        CustomError customError = new CustomError(HttpStatus.BAD_REQUEST.value(), exception.getLocalizedMessage());

        return ResponseEntity.status(customError.statusCode()).body(customError);
    }







}
