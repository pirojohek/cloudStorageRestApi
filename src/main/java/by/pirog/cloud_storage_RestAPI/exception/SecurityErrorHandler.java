package by.pirog.cloud_storage_RestAPI.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityErrorHandler {

    private final ObjectMapper objectMapper;

    public void handleUnauthorized(HttpServletRequest request,
                                   HttpServletResponse response,
                                   AuthenticationException exception) throws IOException{
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value());
        problemDetail.setTitle("Unauthorized");
        problemDetail.setDetail(exception.getMessage());
        problemDetail.setProperty("path", request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        sendErrorMessage(problemDetail, response);
    }

    public void handleAccessDenied(HttpServletRequest request,
                                   HttpServletResponse response,
                                   AccessDeniedException exception) throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN.value());
        problemDetail.setTitle("Forbidden");
        problemDetail.setDetail(exception.getMessage());
        problemDetail.setProperty("path", request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        sendErrorMessage(problemDetail, response);
    }

    private void sendErrorMessage(ProblemDetail problemDetail, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
