package org.ielts.playground.service.impl;

import java.util.HashSet;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.ielts.playground.model.dto.BasicUserDetails;
import org.ielts.playground.model.entity.User;
import org.ielts.playground.model.request.AuthenticationRequest;
import org.ielts.playground.model.request.UserRegistrationRequest;
import org.ielts.playground.model.response.UserInfoResponse;
import org.ielts.playground.repository.UserRepository;
import org.ielts.playground.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<UserDetails> exists(AuthenticationRequest request) {
        String username = request.getUsername();
        Optional<User> user = this.userRepository.findByUsername(username);
        if (user.isPresent() && this.passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            return Optional.of(new BasicUserDetails(user.get()));
        }
        return Optional.ofNullable(null);
    }

    @Override
    public Optional<UserInfoResponse> getUserInfo(String username) {
        return this.userRepository.findByUsername(username)
                .map(user -> UserInfoResponse.builder()
                        .username(username)
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build());
    }

    @Override
    public void createUser(UserRegistrationRequest userRegistration) {
        this.userRepository.findByUsername(userRegistration.getUsername())
                .ifPresentOrElse(null, () -> {
                    User user = User.builder()
                            .username(userRegistration.getUsername())
                            .password(this.passwordEncoder.encode(userRegistration.getPassword()))
                            .email(userRegistration.getEmail())
                            .firstName(userRegistration.getFirstName())
                            .lastName(userRegistration.getLastName())
                            .roles(new HashSet<>())
                            .build();
                    this.userRepository.save(user);
                });
    }
}
