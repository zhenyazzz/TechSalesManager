package org.com.techsalesmanagerserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.com.techsalesmanagerserver.dto.LoginForm;
import org.com.techsalesmanagerserver.dto.LoginResult;
import org.com.techsalesmanagerserver.dto.Request;
import org.com.techsalesmanagerserver.dto.Response;
import org.com.techsalesmanagerserver.dto.SingUpForm;
import org.com.techsalesmanagerserver.dto.SingUpResult;
import org.com.techsalesmanagerserver.enumeration.ResponseStatus;
import org.com.techsalesmanagerserver.model.Role;
import org.com.techsalesmanagerserver.model.User;
import org.com.techsalesmanagerserver.repository.UserRepository;
import org.com.techsalesmanagerserver.server.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public Response authenticate(Request authenticateRequest) throws JsonProcessingException {
        LoginForm authorizationForm = JsonUtils.fromJson(authenticateRequest.getBody(), LoginForm.class);
        log.info("Attempting to find user by username: {}", authorizationForm.getLogin());

        Optional<User> userOptional = userRepository.findByUsername(authorizationForm.getLogin());
        Response response = new Response();

        if (userOptional.isPresent() && passwordService.matches(authorizationForm.getPassword(), userOptional.get().getPassword())) {
            response.setStatus(ResponseStatus.Ok);
            response.setBody(JsonUtils.toJson(new LoginResult(userOptional.get().getId(), userOptional.get().getRole())));
            log.info("User found: {}", userOptional.get().getUsername());
        } else {
            response.setStatus(ResponseStatus.ERROR);
            response.setBody("Пользователь не найден");
            log.warn("User not found with username: {}", authorizationForm.getLogin());
        }

        return response;
    }

    public Response findAll() throws JsonProcessingException {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        for(User user : users) {
            user.setOrders(null);
        }
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(users));
    }

    public List<User> findAll_List() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        for(User user : users) {
            user.setOrders(null);
        }
        return users;
    }

    public Response findById(Long id) throws JsonProcessingException {
        System.out.println("igopseigesopug");
        log.info("Fetching user with id: {}", id);
        Optional<User> userOptional = userRepository.findById(id);

        Response response = new Response();
        if (userOptional.isPresent()) {
            userOptional.get().setOrders(null);
            response.setStatus(ResponseStatus.Ok);
            response.setBody(JsonUtils.toJson(userOptional.get()));
        } else {
            response.setStatus(ResponseStatus.ERROR);
            response.setBody("Пользователь не найден");
        }

        return response;
    }

    @Transactional
    public Response register(Request registerRequest) throws JsonProcessingException {
        SingUpForm singUpForm = JsonUtils.fromJson(registerRequest.getBody(), SingUpForm.class);

        log.info("Saving user: {}", singUpForm.getNickname());

        if (userRepository.findByUsername(singUpForm.getNickname()).isPresent()) {
            return new Response(ResponseStatus.ERROR, "Пользователь уже существует");
        }

        User user =  new User();
        user.setName(singUpForm.getName());
        user.setSurname(singUpForm.getSurname());
        user.setUsername(singUpForm.getNickname());
        user.setEmail(singUpForm.getEmail());
        user.setPassword(passwordService.encode(singUpForm.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setOrders(null);

        System.out.println("user1");
        System.out.println(user.toString());

        User savedUser = userRepository.save(user);
        System.out.println("user2");
        System.out.println(savedUser);

        SingUpResult singUpResult = new SingUpResult(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getSurname(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getPassword(),
                savedUser.getRole());
        return new Response(ResponseStatus.Ok,JsonUtils.toJson(singUpResult));
    }

    @Transactional
    public Response deleteById(Request deleteRequest) throws JsonProcessingException {
        Long id = JsonUtils.fromJson(deleteRequest.getBody(), Long.class);
        log.info("Deleting user with id: {}", id);

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return new Response(ResponseStatus.Ok, "Пользователь успешно удален");
        } else {
            return new Response(ResponseStatus.ERROR, "Пользователь не найден");
        }
    }

    @Transactional
    public Response updateUser(Request updateRequest) throws JsonProcessingException {
        User user = JsonUtils.fromJson(updateRequest.getBody(), User.class);
        log.info("Updating user: {}", user);
        if (userRepository.findById(user.getId()).isPresent()) {
            user.setPassword(passwordService.encode(user.getPassword()));
            User updatedUser = userRepository.save(user);
            log.info("user updated: {}", updatedUser);
            return new Response(ResponseStatus.Ok, JsonUtils.toJson(updatedUser));
        }
        else{
            log.info("user not found");
            return new Response(ResponseStatus.ERROR, "user not found");
        }

    }
    public Response createUser(Request createRequest) throws JsonProcessingException {
        System.out.println("createUser");
        System.out.println(createRequest.getBody());
        User user = JsonUtils.fromJson(createRequest.getBody(),User.class);
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return new Response(ResponseStatus.ERROR, "Пользователь уже существует");
        }
        user.setPassword(passwordService.encode(user.getPassword()));
        return new Response(ResponseStatus.Ok,JsonUtils.toJson(userRepository.save(user)));
    }


    public Response filterById(Request request) throws JsonProcessingException {
        log.info("Filtering users by ID range: {}", request.getBody());
        try {
            // Десериализация строки диапазона ID
            String idRange = JsonUtils.fromJson(request.getBody(), String.class);
            String[] range = idRange.split("-");
            if (range.length != 2) {
                log.error("Invalid ID range format: {}", idRange);
                return new Response(ResponseStatus.ERROR, "Неверный формат диапазона ID: ожидается 'start-end'");
            }

            Long startId, endId;
            try {
                startId = Long.parseLong(range[0].trim());
                endId = Long.parseLong(range[1].trim());
            } catch (NumberFormatException e) {
                log.error("Invalid number format in ID range: {}", idRange);
                return new Response(ResponseStatus.ERROR, "Неверный формат чисел в диапазоне ID");
            }

            if (startId > endId) {
                log.error("Start ID {} is greater than end ID {}", startId, endId);
                return new Response(ResponseStatus.ERROR, "Начальный ID должен быть меньше или равен конечному");
            }

            List<User> users = userRepository.findByIdBetween(startId, endId);
            for (User user : users) {
                user.setOrders(null);
            }
            if (!users.isEmpty()) {
                log.info("Found {} users in ID range: {}", users.size(), idRange);
                return new Response(ResponseStatus.Ok, JsonUtils.toJson(users));
            } else {
                log.warn("No users found in ID range: {}", idRange);
                return new Response(ResponseStatus.ERROR, "Пользователи не найдены");
            }
        } catch (Exception e) {
            log.error("Failed to filter by ID range: {}", request.getBody(), e);
            return new Response(ResponseStatus.ERROR, "Ошибка при фильтрации по диапазону ID: " + e.getMessage());
        }
    }

    /**
     * Фильтрует пользователя по email, указанному в запросе.
     *
     * @param request Запрос с типом FILTER_USER_BY_EMAIL и телом в формате email
     * @return Ответ с пользователем или ошибкой
     * @throws JsonProcessingException Если произошла ошибка сериализации/десериализации
     */
    public Response filterByEmail(Request request) throws JsonProcessingException {
        log.info("Filtering users by email substring: {}", request.getBody());
        try {
            // Десериализация подстроки email
            String substring = JsonUtils.fromJson(request.getBody(), String.class);
            if (substring == null || substring.trim().isEmpty()) {
                log.error("Email substring is null or empty");
                return new Response(ResponseStatus.ERROR, "Подстрока email не может быть пустой");
            }

            List<User> users = userRepository.findByEmailContainingIgnoreCase(substring.trim());
            for (User user : users) {
                user.setOrders(null);
            }
            if (!users.isEmpty()) {
                log.info("Found {} users with email containing: {}", users.size(), substring);
                return new Response(ResponseStatus.Ok, JsonUtils.toJson(users));
            } else {
                log.warn("No users found with email containing: {}", substring);
                return new Response(ResponseStatus.ERROR, "Пользователи не найдены");
            }
        } catch (Exception e) {
            log.error("Failed to filter by email substring: {}", request.getBody(), e);
            return new Response(ResponseStatus.ERROR, "Ошибка при фильтрации по email: " + e.getMessage());
        }
    }
}
