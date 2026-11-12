package kubo.storage.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<String> notFound(NoSuchKeyException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
    }

    // Body @Valid non valido
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> invalidBody(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .collect(Collectors.joining(", ", "Invalid fields: ", ""));
        return ResponseEntity.badRequest().body(message);
    }

    // Body assente, JSON malformato, campo primitivo mancante (es. size, partNumber)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> unreadableBody(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body("Malformed request body");
    }

    // Parametro (es. @PathVariable) non valido
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<String> invalidParameter(HandlerMethodValidationException e) {
        return ResponseEntity.badRequest().body("Invalid request parameter");
    }

    // HEAD su chiave inesistente: l'SDK può lanciare S3Exception generica con status 404
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<String> s3Error(S3Exception e) {
        if (e.statusCode() == HttpStatus.NOT_FOUND.value()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Storage error");
    }

    // Garage non raggiungibile, timeout, ecc.
    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<String> storageUnavailable(SdkClientException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Storage unavailable");
    }
}
