module com.hamhamham.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.desktop;

    opens com.hamhamham.client to javafx.fxml;
    exports com.hamhamham.client;
}