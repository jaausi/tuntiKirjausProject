package com.sirvja.tuntikirjaus;

import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.utils.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;


public class TuntikirjausApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(TuntikirjausApplication.class);
    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        Thread.setDefaultUncaughtExceptionHandler(TuntikirjausApplication::handleUncaughtException);
        FXMLLoader fxmlLoader = new FXMLLoader(TuntikirjausApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        scene.getStylesheets().add(String.valueOf(TuntikirjausApplication.class.getResource("main-view_dark.css")));
        stage.setScene(scene);
        stage.setTitle("Tuntikirjaus App");
        stage.show();
    }

    // Shows otherwise unhandled errors (e.g. failed database operations) to the user instead of only logging them
    private static void handleUncaughtException(Thread thread, Throwable throwable) {
        LOGGER.error("Unhandled exception in thread {}", thread.getName(), throwable);
        Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
        String message = throwable.getMessage() + (cause != throwable ? " (" + cause.getMessage() + ")" : "");
        if (Platform.isFxApplicationThread()) {
            new AlertService().showGeneralAlert(message);
        } else {
            Platform.runLater(() -> new AlertService().showGeneralAlert(message));
        }
    }

    public static void main(String[] args) {
        Initializer.initializeApplication();
        launch();
    }
}