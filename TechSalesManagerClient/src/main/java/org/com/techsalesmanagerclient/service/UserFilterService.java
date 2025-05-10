package org.com.techsalesmanagerclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.com.techsalesmanagerclient.client.Client;
import org.com.techsalesmanagerclient.client.JsonUtils;
import org.com.techsalesmanagerclient.client.Request;
import org.com.techsalesmanagerclient.client.Response;
import org.com.techsalesmanagerclient.enums.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class UserFilterService {
    private static final Logger log = LoggerFactory.getLogger(UserFilterService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Создаёт запрос и передаёт управление соответствующему методу фильтрации.
     *
     * @param filterType Тип фильтра ("По ID" или "По почте")
     * @param filterValue Значение фильтра (диапазон ID "10-15" или подстрока email)
     * @return Ответ сервера
     * @throws IOException Если произошла ошибка ввода-вывода
     * @throws TimeoutException Если истёк тайм-аут
     * @throws ClassNotFoundException Если класс ответа не найден
     * @throws IllegalArgumentException Если параметры недействительны
     */


    public static Response filter(String filterType, String filterValue) throws IOException, TimeoutException, ClassNotFoundException, IllegalArgumentException {
        log.debug("Processing filter request: type={}, value={}", filterType, filterValue);

        String jsonValue;
        try {
            jsonValue = mapper.writeValueAsString(filterValue);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize filter value: {}", filterValue, e);
            throw new IllegalArgumentException("Ошибка при обработке значения фильтра");
        }

        if ("По ID".equals(filterType)) {
            return filterById(jsonValue);
        } else if ("По почте".equals(filterType)) {
            return filterByEmail(jsonValue);
        } else {
            log.error("Unknown filter type: {}", filterType);
            throw new IllegalArgumentException("Неизвестный тип фильтра: " + filterType);
        }
    }

    /**
     * Фильтрует пользователей по диапазону ID, указанному в запросе.
     *

     * @return Ответ сервера
     * @throws IOException Если произошла ошибка ввода-вывода
     * @throws TimeoutException Если истёк тайм-аут
     * @throws ClassNotFoundException Если класс ответа не найден
     * @throws IllegalArgumentException Если параметры недействительны
     */
    public static Response filterById(String jsonValue) throws IOException, TimeoutException, ClassNotFoundException, IllegalArgumentException {
        Request request =new Request(RequestType.FILTER_USER_BY_ID, jsonValue);

        log.debug("Processing filterByIdRange request: {}", request);

        // Проверка типа запроса
        if (request.getType() != RequestType.FILTER_USER_BY_ID) {
            log.error("Invalid request type for filterByIdRange: {}", request.getType());
            throw new IllegalArgumentException("Ожидался запрос типа FILTER_USER_BY_ID");
        }

        // Валидация тела запроса
        String idRange;
        try {
            idRange = JsonUtils.fromJson(request.getBody(), String.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse ID range: {}", request.getBody(), e);
            throw new IllegalArgumentException("Неверный формат диапазона ID");
        }

        if (idRange == null || idRange.trim().isEmpty()) {
            log.error("ID range is null or empty");
            throw new IllegalArgumentException("Пожалуйста, введите значение для фильтра");
        }

        // Валидация формата диапазона ID
        if (!idRange.matches("\\d+\\s*-\\s*\\d+")) {
            log.error("Invalid ID range format: {}", idRange);
            throw new IllegalArgumentException("Введите диапазон ID в формате 'start-end' (например, 10-15)");
        }

        // Проверка логики диапазона
        String[] range = idRange.split("-");
        Long startId, endId;
        try {
            startId = Long.parseLong(range[0].trim());
            endId = Long.parseLong(range[1].trim());
        } catch (NumberFormatException e) {
            log.error("Invalid number format in ID range: {}", idRange);
            throw new IllegalArgumentException("Неверный формат чисел в диапазоне ID: " + idRange);
        }

        if (startId > endId) {
            log.error("Start ID {} is greater than end ID {}", startId, endId);
            throw new IllegalArgumentException("Начальный ID должен быть меньше или равен конечному");
        }

        // Отправка запроса
        log.debug("Sending filter request for ID range: {}", request);
        Response response = Client.send(request);
        log.info("Filter response for ID range: {}", response);
        return response;
    }

    /**
     * Фильтрует пользователей по подстроке в email, указанной в запросе.
     *

     * @return Ответ сервера
     * @throws IOException Если произошла ошибка ввода-вывода
     * @throws TimeoutException Если истёк тайм-аут
     * @throws ClassNotFoundException Если класс ответа не найден
     * @throws IllegalArgumentException Если параметры недействительны
     */
    public static Response filterByEmail(String jsonValue) throws IOException, TimeoutException, ClassNotFoundException, IllegalArgumentException {
        Request request =new Request(RequestType.FILTER_USER_BY_EMAIL, jsonValue);

        log.debug("Processing filterByEmail request: {}", request);

        // Проверка типа запроса
        if (request.getType() != RequestType.FILTER_USER_BY_EMAIL) {
            log.error("Invalid request type for filterByEmail: {}", request.getType());
            throw new IllegalArgumentException("Ожидался запрос типа FILTER_USER_BY_EMAIL");
        }

        // Валидация тела запроса
        String substring;
        try {
            substring = JsonUtils.fromJson(request.getBody(), String.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse email substring: {}", request.getBody(), e);
            throw new IllegalArgumentException("Неверный формат подстроки email");
        }

        if (substring == null || substring.trim().isEmpty()) {
            log.error("Email substring is null or empty");
            throw new IllegalArgumentException("Пожалуйста, введите значение для фильтра");
        }

        // Валидация подстроки (только буквы, цифры, точки, дефисы)
        if (!substring.trim().matches("[A-Za-z0-9._-]+")) {
            log.error("Invalid email substring format: {}", substring);
            throw new IllegalArgumentException("Подстрока email должна содержать только буквы, цифры, точки или дефисы");
        }

        // Отправка запроса
        log.debug("Sending filter request for email substring: {}", request);
        Response response = Client.send(request);
        log.info("Filter response for email substring: {}", response);
        return response;
    }
}