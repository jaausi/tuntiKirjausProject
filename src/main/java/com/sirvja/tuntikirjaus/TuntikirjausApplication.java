package com.sirvja.tuntikirjaus;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.DBUtil;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TuntikirjausApplication extends Application {
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
        System.setProperty("prism.lcdtext", "false");
        assert DBUtil.checkDrivers();
        //TuntiKirjausDao.dropTable();
        TuntiKirjausDao.initializeTable();

        ObservableList<TuntiKirjaus> kirjaukset = DBUtil.getAllTuntikirjaus();
        System.out.println("KIRJAUKSET: "+kirjaukset);
        launch();
    }
}