package org.com.techsalesmanagerserver.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.model.User;
import org.com.techsalesmanagerserver.repository.UserRepository;
import org.com.techsalesmanagerserver.server.init.JsonMessage;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public JsonMessage findByUsernameAndPassword(String username, String password) {
        log.info("Attempting to find user by username: {}", username);

        // Ищем пользователя по имени
        Optional<User> userOptional = userRepository.findByUsername(username);
        log.info("User Found: {}", userOptional.isPresent());
        JsonMessage response = new JsonMessage();

        if (userOptional.isPresent()) {
            User user = new User();
            user.setUsername(userOptional.get().getUsername());
            user.setPassword(userOptional.get().getPassword());
            user.setRole(userOptional.get().getRole());
            user.setEmail(userOptional.get().getEmail());
            user.setName(userOptional.get().getName());
            user.setSurname(userOptional.get().getSurname());
            log.info("Attempting check the password: {}", password);
            // Проверяем совпадение паролей (рекомендуется использовать BCrypt)
            if (passwordMatches(user.getPassword(), password)) {

                response.setCommand("success");
                response.getData().put("user", user);
                log.info("User found and password matches");
            } else {

                response.setCommand("error");
                response.getData().put("message", "Invalid password");
                log.warn("Password mismatch for user: {}", username);
            }
        } else {
            response.setCommand("error");
            response.getData().put("message", "User not found");
            log.warn("User not found with username: {}", username);
        }

        return response;
    }

    // Метод для проверки пароля (адаптируйте под вашу систему хеширования)
    private boolean passwordMatches(String storedHash, String rawPassword) {

        log.info("Attempting to check the password: {}  {}", rawPassword,storedHash);

        if (rawPassword.equals(storedHash)) {
            return true;
        }
        else return false;
        //потом сменить
        //return BCrypt.checkpw(rawPassword, storedHash);
    }

    public JsonMessage findAll() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        JsonMessage response = new JsonMessage();
        response.setCommand("success");
        response.getData().put("users", users);
        return response;
    }

    public List<User> findAll_List() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();

        return users;
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

    @Transactional
    public JsonMessage register(User user) {
        log.info("Registering user: {}", user);

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return createErrorResponse("User already exists");
        }

        User savedUser = userRepository.save(user);
        return createSuccessResponse("User registered successfully", savedUser);
    }

    public JsonMessage authenticate(String username, String password) {

        log.info("Attempting authentication for user: {}", username);

        JsonMessage authResponse = findByUsernameAndPassword(username, password);

        if ("success".equals(authResponse.getCommand())) {
            log.info("Authentication successful for user: {}", username);

            return authResponse; // Уже содержит user и success-статус
        }

        log.warn("Authentication failed for user: {}", username);
        return createErrorResponse("Invalid credentials"); // Используем существующий метод
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
