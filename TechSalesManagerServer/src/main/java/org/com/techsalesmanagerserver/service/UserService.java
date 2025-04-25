package org.com.techsalesmanagerserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.model.User;
import org.com.techsalesmanagerserver.repository.UserRepository;
import org.com.techsalesmanagerserver.server.init.JsonMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public JsonMessage findAll() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        JsonMessage response = new JsonMessage();
        response.setCommand("success");
        response.getData().put("users", users);
        return response;
    }

    public JsonMessage findById(Long id) {
        log.info("Fetching user with id: {}", id);
        Optional<User> userOptional = userRepository.findById(id);

        JsonMessage response = new JsonMessage();

        if (userOptional.isPresent()) {
            response.setCommand("success");
            response.getData().put("user", userOptional.get());
        } else {
            response.setCommand("error");
            response.getData().put("message", "User not found");
        }

        return response;
    }

    public JsonMessage save(User user) {
        log.info("Saving user: {}", user);

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return createErrorResponse("User already exists");
        }

        User savedUser = userRepository.save(user);
        return createSuccessResponse("User saved successfully", savedUser);
    }

    public JsonMessage deleteById(Long id) {
        log.info("Deleting user with id: {}", id);

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return createSuccessResponse("User deleted successfully", null);
        } else {
            return createErrorResponse("User not found");
        }
    }

    public JsonMessage register(User user) {
        log.info("Registering user: {}", user);

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return createErrorResponse("User already exists");
        }

        User savedUser = userRepository.save(user);
        return createSuccessResponse("User registered successfully", savedUser);
    }

    public JsonMessage authenticate(String username, String password) {
        log.info("Authenticating user: {}", username);
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return createSuccessResponse("Authentication successful", user.get());
        }

        return createErrorResponse("Invalid username or password");
    }

    private JsonMessage createSuccessResponse(String message, User user) {
        JsonMessage response = new JsonMessage();
        response.setCommand("success");
        response.getData().put("message", message);
        response.getData().put("data", user);
        return response;
    }

    private JsonMessage createErrorResponse(String errorMessage) {
        JsonMessage response = new JsonMessage();
        response.setCommand("error");
        response.getData().put("message", errorMessage);
        return response;
    }
}
