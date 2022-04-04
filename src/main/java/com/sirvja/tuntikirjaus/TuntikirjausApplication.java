package com.sirvja.tuntikirjaus;

import com.sirvja.tuntikirjaus.utils.DBUtil;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class TuntikirjausApplication extends Application {
    public static Stage stage;
    private static final Logger LOGGER = LoggerFactory.getLogger(TuntikirjausApplication.class);


    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(TuntikirjausApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        scene.getStylesheets().add(String.valueOf(TuntikirjausApplication.class.getResource("main-view_dark.css")));
        stage.setScene(scene);
        stage.setTitle("Tuntikirjaus App");
        stage.show();
    }

    public static void main(String[] args) {
        DBUtil.checkOrCreateDatabaseFile();

        System.setProperty("prism.lcdtext", "false");
        assert DBUtil.checkDrivers();
        //TuntiKirjausDao.dropTable();
        LOGGER.debug("Initializing Tuntikirjaus table...");
        TuntiKirjausDao.initializeTable();
        LOGGER.debug("Tuntikirjaus table initialized.");

        launch();
    }
}