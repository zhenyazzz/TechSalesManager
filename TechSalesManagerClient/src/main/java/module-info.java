module org.com.techsalesmanagerclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires io.netty.transport;
    requires io.netty.codec;
    requires com.fasterxml.jackson.databind;
    requires io.netty.buffer;
    requires static lombok;
    requires io.netty.common;
    requires io.netty.handler;
    requires org.slf4j;
    requires annotations;
    requires java.sql;
    requires javafx.swing;

    opens org.com.techsalesmanagerclient.model to com.fasterxml.jackson.databind;
    opens org.com.techsalesmanagerclient to javafx.fxml;
    exports org.com.techsalesmanagerclient;
    opens org.com.techsalesmanagerclient.controller to javafx.fxml;

    exports org.com.techsalesmanagerclient.controller;
    exports org.com.techsalesmanagerclient.client;
    opens org.com.techsalesmanagerclient.client to javafx.fxml;
    exports org.com.techsalesmanagerclient.enums;
    opens org.com.techsalesmanagerclient.enums to javafx.fxml;
    exports org.com.techsalesmanagerclient.model to com.fasterxml.jackson.databind;
    exports org.com.techsalesmanagerclient.service to com.fasterxml.jackson.databind;
    opens org.com.techsalesmanagerclient.service to com.fasterxml.jackson.databind;

}