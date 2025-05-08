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

    public Response authenticate(Request authenticateRequest) throws JsonProcessingException {
        LoginForm authorizationForm = JsonUtils.fromJson(authenticateRequest.getBody(), LoginForm.class);
        log.info("Attempting to find user by username: {}", authorizationForm.getLogin());

        Optional<User> userOptional = userRepository.findByUsername(authorizationForm.getLogin());
        Response response = new Response();
        // еще проверку пароля наверное можно сюда добавить в условие
        if (userOptional.isPresent() && passwordMatches(userOptional.get().getPassword(), authorizationForm.getPassword())) {
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

    public Response findAll() throws JsonProcessingException {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
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

    public Response register(Request registerRequest) throws JsonProcessingException {
        System.out.println("igeoge");
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
        user.setPassword(singUpForm.getPassword());
        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);


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


    public Response updateUser(Request updateRequest) throws JsonProcessingException {
        User user = JsonUtils.fromJson(updateRequest.getBody(), User.class);
        log.info("Updating user: {}", user);
        if (userRepository.findById(user.getId()).isPresent()) {
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
        User user = JsonUtils.fromJson(createRequest.getBody(),User.class);
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return new Response(ResponseStatus.ERROR, "Пользователь уже существует");
        }

        return new Response(ResponseStatus.Ok,JsonUtils.toJson(userRepository.save(user)));
    }


}
