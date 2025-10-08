package by.pirog.cloud_storage_RestAPI.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserRestController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok().body(Map.of("username", userDetails.getUsername()));
    }
}
