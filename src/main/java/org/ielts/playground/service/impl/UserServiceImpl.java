package org.ielts.playground.service.impl;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.ielts.playground.common.constant.ValidationConstants;
import org.ielts.playground.common.enumeration.Subscription;
import org.ielts.playground.common.exception.NotFoundException;
import org.ielts.playground.model.request.UserUpdateRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.ielts.playground.common.exception.ResourceExistedException;
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

    @Nullable
    protected UserInfoResponse userInfoResponse(User user) {
        return Optional.ofNullable(user)
                .map(u -> UserInfoResponse.builder()
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .phoneNumber(u.getPhoneNumber())
                    .subscription(Objects.toString(u.getSubscription()))
                    .build())
                .orElse(null);
    }

    @Override
    public Optional<UserDetails> exists(AuthenticationRequest request) {
        String username = request.getUsername();
        Optional<User> user = this.userRepository.findByUsername(username);
        if (user.isPresent() && this.passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            return Optional.of(new BasicUserDetails(user.get()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserInfoResponse> getUserInfo(String username) {
        return this.userRepository.findByUsername(username)
                .map(this::userInfoResponse);
    }

    @Override
    public void createUser(UserRegistrationRequest userRegistration) {
        if (this.userRepository.findByUsername(userRegistration.getUsername()).isPresent()) {
            throw new ResourceExistedException();
        }

        final User user = User.builder()
                .username(userRegistration.getUsername())
                .password(this.passwordEncoder.encode(userRegistration.getPassword()))
                .email(userRegistration.getEmail())
                .firstName(userRegistration.getFirstName())
                .lastName(userRegistration.getLastName())
                .phoneNumber(userRegistration.getPhoneNumber())
                .roles(Collections.emptySet())
                .subscription(Subscription.of(userRegistration.getSubscription()))
                .build();
        this.userRepository.save(user);
    }

    @Override
    public UserInfoResponse updateUserInfo(UserUpdateRequest userUpdateRequest) {
        final User user = this.userRepository.findByUsername(userUpdateRequest.getUsername())
                .orElseThrow(() -> new NotFoundException(ValidationConstants.USER_NOT_FOUND));
        Optional.ofNullable(userUpdateRequest.getPassword())
                .map(this.passwordEncoder::encode)
                .ifPresent(user::setPassword);
        Optional.ofNullable(userUpdateRequest.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(userUpdateRequest.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(userUpdateRequest.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(userUpdateRequest.getPhoneNumber()).ifPresent(user::setPhoneNumber);
        Optional.ofNullable(userUpdateRequest.getSubscription())
                .map(Subscription::of)
                .ifPresent(user::setSubscription);
        Optional.ofNullable(userUpdateRequest.getActivated()).ifPresent(user::setActivated);
        return this.userInfoResponse(this.userRepository.save(user));
    }
}
