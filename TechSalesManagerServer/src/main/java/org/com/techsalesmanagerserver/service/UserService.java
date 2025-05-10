package org.com.techsalesmanagerserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.enumeration.ResponseStatus;
import org.com.techsalesmanagerserver.model.Role;
import org.com.techsalesmanagerserver.model.User;
import org.com.techsalesmanagerserver.repository.UserRepository;
import org.com.techsalesmanagerserver.server.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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


}
