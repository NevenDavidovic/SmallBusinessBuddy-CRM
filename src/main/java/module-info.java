module smallbusinessbuddycrm {
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
    requires java.sql;
    requires annotations;
    requires javafx.swing;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires kernel;
    requires html2pdf;
    requires layout;
    requires io;

    opens smallbusinessbuddycrm to javafx.fxml;
    exports smallbusinessbuddycrm;

    opens smallbusinessbuddycrm.controllers to javafx.fxml;
    exports smallbusinessbuddycrm.controllers;
    opens smallbusinessbuddycrm.model to javafx.base;
}