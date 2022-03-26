module com.sirvja.tuntikirjaus {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.sirvja.tuntikirjaus to javafx.fxml;
    exports com.sirvja.tuntikirjaus;
}