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
    requires java.mail;
    requires java.net.http;
    requires jdk.httpserver;
    requires java.dotenv;
    requires java.prefs;

    opens smallbusinessbuddycrm to javafx.fxml;
    exports smallbusinessbuddycrm;


    opens smallbusinessbuddycrm.model to javafx.base;
    exports smallbusinessbuddycrm.controllers.contact;
    opens smallbusinessbuddycrm.controllers.contact to javafx.fxml;
    exports smallbusinessbuddycrm.controllers.list;
    opens smallbusinessbuddycrm.controllers.list to javafx.fxml;
    exports smallbusinessbuddycrm.controllers.workshop;
    opens smallbusinessbuddycrm.controllers.workshop to javafx.fxml;
    exports smallbusinessbuddycrm.controllers.utilities;
    opens smallbusinessbuddycrm.controllers.utilities to javafx.fxml;
    exports smallbusinessbuddycrm.controllers.teacher;
    opens smallbusinessbuddycrm.controllers.teacher to javafx.fxml;
    exports smallbusinessbuddycrm.controllers.organization;
    opens smallbusinessbuddycrm.controllers.organization to javafx.fxml;
    exports smallbusinessbuddycrm.controllers.underaged;
    opens smallbusinessbuddycrm.controllers.underaged to javafx.fxml;
}