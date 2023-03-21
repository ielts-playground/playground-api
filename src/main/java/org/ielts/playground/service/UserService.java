package org.ielts.playground.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;

import org.ielts.playground.model.request.AuthenticationRequest;
import org.ielts.playground.model.request.UserRegistrationRequest;
import org.ielts.playground.model.response.UserInfoResponse;

public interface UserService {
    /**
     * Checks if a user with given username and password exists or not.
     * @param request contains the username and password of the user.
     * @return the {@link Optional} of {@link UserDetails} of the existed user.
     */
    Optional<UserDetails> exists(AuthenticationRequest request);

    /**
     * Retrieves the information of a user.
     * @param username the user's name.
     */
    Optional<UserInfoResponse> getUserInfo(String username);

    /**
     * Registers new user.
     * @param userRegistration the user's information.
     */
    void createUser(UserRegistrationRequest userRegistration);
}
