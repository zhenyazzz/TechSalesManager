package org.com.techsalesmanagerclient.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Product {

    private Long id;

    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;

    public Product(Long aLong, String name, String description, Double aDouble, Integer integer, Long aLong1) {
        this.id = aLong;
        this.name = name;
        this.description = description;
        this.price = aDouble;
        this.stock = integer;
        this.category = aLong1.toString();

    }
    public Product(Map<String, Object> data) {
        // Извлекаем id и приводим к Long, по умолчанию 0L
        Object idObj = data.get("id");
        this.id = idObj != null ? Long.parseLong(idObj.toString()) : 0L;

        // Извлекаем name как String, по умолчанию пустая строка
        Object nameObj = data.get("name");
        this.name = nameObj != null ? nameObj.toString() : "";

        // Извлекаем description как String, по умолчанию пустая строка
        Object descriptionObj = data.get("description");
        this.description = descriptionObj != null ? descriptionObj.toString() : "";

        // Извлекаем price и приводим к Double, по умолчанию 0.0
        Object priceObj = data.get("price");
        this.price = priceObj != null ? Double.parseDouble(priceObj.toString()) : 0.0;

        // Извлекаем stock и приводим к Integer, по умолчанию 0
        Object stockObj = data.get("stock");
        this.stock = stockObj != null ? Integer.parseInt(stockObj.toString()) : 0;

        // Извлекаем categoryId и приводим к Long, по умолчанию 0L
        Object categoryObj = data.get("category");
        this.category = categoryObj != null ? categoryObj.toString() : "";
    }


    //private Supplier supplier;
  /*  private List<ProductImage> images = new ArrayList<>();
    private List<OrderItem> orderItems;
    private List<Sale> sales;*/
}
