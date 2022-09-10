package com.sirvja.tuntikirjaus;

import com.sirvja.tuntikirjaus.service.MainViewService;
import com.sirvja.tuntikirjaus.utils.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

import static com.sirvja.tuntikirjaus.utils.Constants.DROP_TABLE_ON_START;

public class TuntikirjausApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(TuntikirjausApplication.class);
    public static Stage stage;

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
        Initializer.initializeApplication();
        launch();
    }
}