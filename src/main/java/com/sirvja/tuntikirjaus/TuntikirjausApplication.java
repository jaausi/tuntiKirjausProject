package com.sirvja.tuntikirjaus;

import com.sirvja.tuntikirjaus.controller.MainViewController;
import com.sirvja.tuntikirjaus.utils.*;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
public class TuntikirjausApplication extends Application {

    private ConfigurableApplicationContext context;
    private final Initializer initializer;
    public static Stage stage;

    public TuntikirjausApplication(Initializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public void init() {
        try {
            initializer.initializeApplication();

            ApplicationContextInitializer<GenericApplicationContext> initializer = applicationContext -> {
                applicationContext.registerBean(Application.class, () -> TuntikirjausApplication.this);
                applicationContext.registerBean(Parameters.class, this::getParameters);
                applicationContext.registerBean(HostServices.class, this::getHostServices);
            };

            this.context = new SpringApplicationBuilder()
                    .sources(Launcher.class)
                    .initializers(initializer)
                    .run(getParameters().getRaw().toArray(new String[0]));
        } catch (IOException e) {
            log.error("Couldn't initialize the application, something went wrong in database file creation: {}", e.getMessage());
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.context.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() throws Exception {
        this.context.close();
        Platform.exit();
    }
}

@Log4j2
@Component
class StageInitializer implements ApplicationListener<StageReadyEvent> {
    private final ApplicationContext applicationContext;
    private final MainViewController mainViewController;

    StageInitializer(ApplicationContext applicationContext, MainViewController mainViewController) {
        this.applicationContext = applicationContext;
        this.mainViewController = mainViewController;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
        try {
            Stage stage = stageReadyEvent.getStage();
            FXMLLoader fxmlLoader = new FXMLLoader(TuntikirjausApplication.class.getResource("main-view.fxml"));
            fxmlLoader.setControllerFactory(this.applicationContext::getBean);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(String.valueOf(TuntikirjausApplication.class.getResource("main-view_dark.css")));
            stage.setScene(scene);
            stage.setTitle("Tuntikirjaus App");
            stage.show();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class StageReadyEvent extends ApplicationEvent {

    private final Stage stage;

    StageReadyEvent(Stage stage) {
        super(stage);
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}