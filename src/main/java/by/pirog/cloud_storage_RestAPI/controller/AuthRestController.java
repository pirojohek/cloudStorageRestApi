package by.pirog.cloud_storage_RestAPI.controller;


import by.pirog.cloud_storage_RestAPI.dto.UserSignInDTO;
import by.pirog.cloud_storage_RestAPI.dto.UserSignUpDTO;
import by.pirog.cloud_storage_RestAPI.service.AuthService;
import by.pirog.cloud_storage_RestAPI.storage.entity.UserEntity;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthRestController {

    private final AuthService authService;

    private final MinioClient minioClient;

    private final AuthenticationManager authenticationManager;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserSignUpDTO user, HttpServletRequest request,
                                    BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        }

        authService.signUp(user.username(), user.password());
        authenticateAndSetSession(user.username(), user.password(),  request);


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("username", user.username()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody UserSignInDTO user, HttpServletRequest request,
                                    BindingResult bindingResult) throws BindException {

        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception){
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        }

        authenticateAndSetSession(user.username(), user.password(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(HttpServletRequest request) throws AuthenticationException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            return ResponseEntity.noContent().build();
        }

        throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
    }


    private void authenticateAndSetSession(String username, String password, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", context);
    }

}
