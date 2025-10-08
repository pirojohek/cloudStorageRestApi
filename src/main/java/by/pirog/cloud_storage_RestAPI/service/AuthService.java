package by.pirog.cloud_storage_RestAPI.service;

import by.pirog.cloud_storage_RestAPI.storage.entity.UserEntity;
import by.pirog.cloud_storage_RestAPI.storage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserEntity signUp(String username, String password){
        if (userRepository.existsByUsername(username)){
            throw new UsernameNotFoundException(username);
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(password));
        userRepository.save(userEntity);
        return userEntity;
    }

    public Optional<UserEntity> findUserByUsername(String username){
        return userRepository.findByUsername(username);
    }


}
