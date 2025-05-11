package org.com.techsalesmanagerclient.model;

import javafx.beans.property.*;

public class POJO_Product {
    private final LongProperty id;
    private final StringProperty name;
    private final StringProperty description;
    private final DoubleProperty price;
    private final IntegerProperty stock;
    private final ObjectProperty<Category> category;

    public POJO_Product(Long id, String name, String description, Double price, Integer stock, Category category) {
        this.id = new SimpleLongProperty(id);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.price = new SimpleDoubleProperty(price);
        this.stock = new SimpleIntegerProperty(stock);
        this.category = new SimpleObjectProperty<>(category);
    }

    public LongProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public IntegerProperty stockProperty() {
        return stock;
    }

    public ObjectProperty<Category> categoryProperty() {
        return category;
    }

    public Long getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public String getDescription() {
        return description.get();
    }

    public Double getPrice() {
        return price.get();
    }

    public Integer getStock() {
        return stock.get();
    }

    public Category getCategory() {
        return category.get();
    }
}