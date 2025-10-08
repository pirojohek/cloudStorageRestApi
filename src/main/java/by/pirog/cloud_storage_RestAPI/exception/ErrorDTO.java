package by.pirog.cloud_storage_RestAPI.exception;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class ErrorDTO {
    int status;

    String error;

    String message;

    String path;

    LocalDateTime timestamp = LocalDateTime.now();
}
