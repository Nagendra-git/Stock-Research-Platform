package com.nagendra.user_service.service.impl;

import com.nagendra.user_service.dto.RegisterRequestDto;
import com.nagendra.user_service.dto.RegisterResponseDto;
import com.nagendra.user_service.model.Role;
import com.nagendra.user_service.model.User;
import com.nagendra.user_service.repository.UserRepository;
import com.nagendra.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponseDto register(RegisterRequestDto request) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already registered"
            );
        }

        // 2. Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username already taken"
            );
        }

        // 3. Encode password
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        // 4. Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();

        // 5. Save user
        User savedUser = userRepository.save(user);

    // 6. Return response
    return new RegisterResponseDto(
        savedUser.getId(),
        savedUser.getUsername(),
        savedUser.getEmail(),
        "User registered successfully");
    }
}
