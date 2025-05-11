package org.com.techsalesmanagerclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.com.techsalesmanagerclient.client.Client;
import org.com.techsalesmanagerclient.client.JsonUtils;
import org.com.techsalesmanagerclient.client.Request;
import org.com.techsalesmanagerclient.client.Response;
import org.com.techsalesmanagerclient.controller.WorkWithScenes;
import org.com.techsalesmanagerclient.enums.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ProductFilterService {
    private static final Logger log = LoggerFactory.getLogger(ProductFilterService.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final WorkWithScenes workWithScenes = new WorkWithScenes();
    /**
     * Создаёт запрос и передаёт управление соответствующему методу фильтрации.
     *
     * @param filterType Тип фильтра ("По ID" или "По названию")
     * @param filterValue Значение фильтра (диапазон ID "10-15" или подстрока названия)
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
        } else if ("По названию".equals(filterType)) {
            return filterByName(jsonValue);
        } else {
            log.error("Unknown filter type: {}", filterType);
            throw new IllegalArgumentException("Неизвестный тип фильтра: " + filterType);
        }
    }

    /**
     * Фильтрует товары по диапазону ID, указанному в запросе.
     *
     * @param jsonValue JSON-строка с диапазоном ID (например, "10-15")
     * @return Ответ сервера
     * @throws IOException Если произошла ошибка ввода-вывода
     * @throws TimeoutException Если истёк тайм-аут
     * @throws ClassNotFoundException Если класс ответа не найден
     * @throws IllegalArgumentException Если параметры недействительны
     */
    public static Response filterById(String jsonValue) throws IOException, TimeoutException, ClassNotFoundException, IllegalArgumentException {
        Request request = new Request(RequestType.FILTER_PRODUCT_BY_ID, jsonValue);

        log.debug("Processing filterById request: {}", request);

        // Проверка типа запроса
        if (request.getType() != RequestType.FILTER_PRODUCT_BY_ID) {
            log.error("Invalid request type for filterById: {}", request.getType());
            throw new IllegalArgumentException("Ожидался запрос типа FILTER_PRODUCT_BY_ID");
        }

        // Валидация тела запроса
        String idRange;
        try {
            idRange = JsonUtils.fromJson(request.getBody(), String.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse ID range: {}", request.getBody(), e);
            throw new IllegalArgumentException("Неверный формат диапазона ID");
        }

//        if (idRange == null || idRange.trim().isEmpty()) {
//            log.error("ID range is null or empty");
//
//            //throw new IllegalArgumentException("Пожалуйста, введите значение для фильтра");
//        }

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
     * Фильтрует товары по подстроке в названии, указанной в запросе.
     *
     * @param jsonValue JSON-строка с подстрокой названия
     * @return Ответ сервера
     * @throws IOException Если произошла ошибка ввода-вывода
     * @throws TimeoutException Если истёк тайм-аут
     * @throws ClassNotFoundException Если класс ответа не найден
     * @throws IllegalArgumentException Если параметры недействительны
     */
    public static Response filterByName(String jsonValue) throws IOException, TimeoutException, ClassNotFoundException, IllegalArgumentException {
        Request request = new Request(RequestType.FILTER_PRODUCT_BY_NAME, jsonValue);

        log.debug("Processing filterByName request: {}", request);

        // Проверка типа запроса
        if (request.getType() != RequestType.FILTER_PRODUCT_BY_NAME) {
            log.error("Invalid request type for filterByName: {}", request.getType());
            throw new IllegalArgumentException("Ожидался запрос типа FILTER_PRODUCT_BY_NAME");
        }

        // Валидация тела запроса
        String substring;
        try {
            substring = JsonUtils.fromJson(request.getBody(), String.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse name substring: {}", request.getBody(), e);
            throw new IllegalArgumentException("Неверный формат подстроки названия");
        }

        if (substring == null || substring.trim().isEmpty()) {
            //workWithScenes.loadScene("/org/com/techsalesmanagerclient/Product_Work_Menu.fxml", exitButton);
            //log.error("Name substring is null or empty");
           // throw new IllegalArgumentException("Пожалуйста, введите значение для фильтра");
        }

        // Валидация подстроки (буквы, цифры, пробелы, дефисы)
       /* if (!substring.trim().matches("[A-Za-zА-Яа-я0-9\\s\\-,.]+")) {
            log.error("Invalid name substring format: {}", substring);
            throw new IllegalArgumentException("Подстрока названия должна содержать только буквы, цифры, пробелы, дефисы, точки или запятые");
        }*/

        // Отправка запроса
        log.debug("Sending filter request for name substring: {}", request);
        Response response = Client.send(request);
        log.info("Filter response for name substring: {}", response);
        return response;
    }
}