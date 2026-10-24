package kubo.storage.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<String> notFound(NoSuchKeyException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
    }
}
