package org.com.techsalesmanagerclient.service;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerclient.controller.WorkWithScenes;
import org.com.techsalesmanagerclient.model.Product;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ProductReportService {

    private final WorkWithScenes workWithScenes = new WorkWithScenes();

    public void showReport(List<Product> products, String reportFileName, Button callerButton) throws Exception {
        log.info("Generating report");
        // Подготовка временных файлов для графиков
        List<String> imagePaths = new ArrayList<>();

        // График 1: Круговая диаграмма доли товаров по категориям
        log.info("Generating pieChart");
        String pieChartPath = "pie_chart.png";
        PieChart pieChart = generatePieChart(products, pieChartPath);
        pieChart.setPrefWidth(400);
        pieChart.setPrefHeight(300);
        imagePaths.add(pieChartPath);

        // График 2: Гистограмма средних цен по категориям
        log.info("Generating barChart");
        String barChartPath = "bar_chart.png";
        BarChart<String, Number> barChart = generateBarChart(products, barChartPath);
        barChart.setPrefWidth(400);
        barChart.setPrefHeight(300);
        imagePaths.add(barChartPath);

        // График 3: Линейный график количества товаров по категориям
        log.info("Generating lineChart");
        String lineChartPath = "line_chart.png";
        LineChart<String, Number> lineChart = generateLineChart(products, lineChartPath);
        lineChart.setPrefWidth(400);
        lineChart.setPrefHeight(300);
        imagePaths.add(lineChartPath);

        // График 4: Столбчатая диаграмма топ-5 самых дорогих товаров
        log.info("Generating topChart");
        String topProductsChartPath = "top_products_chart.png";
        BarChart<String, Number> topProductsChart = generateTopProductsChart(products, topProductsChartPath);
        topProductsChart.setPrefWidth(400);
        topProductsChart.setPrefHeight(300);
        imagePaths.add(topProductsChartPath);

        // Создание PDF
        log.info("Generating pdf");
        createPdfReport(imagePaths, reportFileName);

        // Создаем контейнер для графиков
        log.info("Preparing report UI");
        HBox hbox = new HBox(10, pieChart, barChart, lineChart, topProductsChart);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(10));

        // Создаем контейнер для графиков (без добавления callerButton)
        VBox chartContainer = new VBox(10, hbox);
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.setPadding(new Insets(10));

        // Получаем текущую сцену через вызывающую кнопку
        Scene currentScene = callerButton.getScene();
        if (currentScene != null) {
            // Получаем текущий корневой узел сцены (AnchorPane)
            AnchorPane root = (AnchorPane) currentScene.getRoot();

            // Добавляем графики в AnchorPane
            root.getChildren().add(chartContainer);

            // Устанавливаем позицию графиков (например, сверху)
            AnchorPane.setTopAnchor(chartContainer, 10.0);
            AnchorPane.setLeftAnchor(chartContainer, 10.0);
            AnchorPane.setRightAnchor(chartContainer, 10.0);

            // Устанавливаем ширину окна
            currentScene.getWindow().setWidth(1600);
            currentScene.getWindow().sizeToScene();
        } else {
            log.warn("Current scene is null, cannot display report");
        }

        // Удаление временных файлов
        for (String path : imagePaths) {
            new File(path).delete();
        }
    }

    private PieChart generatePieChart(List<Product> products, String filePath) throws Exception {
        if (products == null || products.isEmpty()) {
            log.warn("No products available for pie chart");
            return new PieChart(); // Возвращаем пустой график
        }

        // Подсчет количества товаров по категориям
        Map<String, Long> categoryCount = products.stream()
                .filter(p -> p.getCategory() != null && !p.getCategory().isEmpty())
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        // Создание круговой диаграммы
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Доля товаров по категориям");
        categoryCount.forEach((category, count) -> {
            PieChart.Data data = new PieChart.Data(category, count);
            pieChart.getData().add(data);
        });

        // Сохранение графика как изображения
        saveChartAsImage(pieChart, filePath);
        return pieChart;
    }

    private BarChart<String, Number> generateBarChart(List<Product> products, String filePath) throws Exception {
        // Подсчет средних цен по категориям
        Map<String, Double> averagePrices = products.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory(),
                        Collectors.averagingDouble(Product::getPrice)));

        // Создание гистограммы
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Средние цены по категориям");
        xAxis.setLabel("Категория");
        yAxis.setLabel("Средняя цена");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Средняя цена");
        averagePrices.forEach((category, avgPrice) ->
                series.getData().add(new XYChart.Data<>(category, avgPrice)));
        barChart.getData().add(series);

        // Сохранение графика как изображения
        saveChartAsImage(barChart, filePath);
        return barChart;
    }

    private LineChart<String, Number> generateLineChart(List<Product> products, String filePath) throws Exception {
        // Подсчет количества товаров по категориям
        Map<String, Integer> stockByCategory = products.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory(),
                        Collectors.summingInt(Product::getStock)));

        // Создание линейного графика
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Количество товаров на складе по категориям");
        xAxis.setLabel("Категория");
        yAxis.setLabel("Количество");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Количество товаров");
        stockByCategory.forEach((category, stock) ->
                series.getData().add(new XYChart.Data<>(category, stock)));
        lineChart.getData().add(series);

        // Сохранение графика как изображения
        saveChartAsImage(lineChart, filePath);
        return lineChart;
    }

    private BarChart<String, Number> generateTopProductsChart(List<Product> products, String filePath) throws Exception {
        // Получение топ-5 самых дорогих товаров
        List<Product> topProducts = products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Создание столбчатой диаграммы
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Топ-5 самых дорогих товаров");
        xAxis.setLabel("Товар");
        yAxis.setLabel("Цена");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Цена");
        topProducts.forEach(product ->
                series.getData().add(new XYChart.Data<>(product.getName(), product.getPrice())));
        barChart.getData().add(series);

        // Сохранение графика как изображения
        saveChartAsImage(barChart, filePath);
        return barChart;
    }

    private void saveChartAsImage(Chart chart, String filePath) throws Exception {
        Scene scene = new Scene(chart, 600, 400);
        WritableImage image = scene.snapshot(null);
        File file = new File(filePath);
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    }

    private void createPdfReport(List<String> imagePaths, String reportFileName) throws Exception {
        /*PdfWriter writer = new PdfWriter(new FileOutputStream(reportFileName));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        for (String imagePath : imagePaths) {
            Image img = new Image(ImageDataFactory.create(imagePath));
            img.setAutoScale(true);
            document.add(img);
        }

        document.close();*/
    }
}


