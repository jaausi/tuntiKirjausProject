module com.sirvja.tuntikirjaus {
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.sql.rowset;
    requires org.slf4j;
    requires org.apache.commons.lang3;
    requires javafx.graphics;
    requires javafx.controls;

    opens com.sirvja.tuntikirjaus to javafx.fxml;
    exports com.sirvja.tuntikirjaus;
    exports com.sirvja.tuntikirjaus.domain;
    opens com.sirvja.tuntikirjaus.domain to javafx.fxml;
    exports com.sirvja.tuntikirjaus.utils;
    opens com.sirvja.tuntikirjaus.utils to javafx.fxml;
    exports com.sirvja.tuntikirjaus.controller;
    opens com.sirvja.tuntikirjaus.controller to javafx.fxml;
    exports com.sirvja.tuntikirjaus.customFields;
    opens com.sirvja.tuntikirjaus.customFields to javafx.fxml;
}