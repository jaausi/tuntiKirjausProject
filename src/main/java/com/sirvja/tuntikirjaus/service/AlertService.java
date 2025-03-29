package com.sirvja.tuntikirjaus.service;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    public void showFieldNotFilledAlert(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Pakollisia kenttiä täyttämättä");
        alert.setContentText("Punaisella korostettuihin kenttiin tulee syöttää" +
                " arvo ennen taulukkoon lisäämistä.");
        alert.showAndWait();
    }

    public void showNotCorrectTimeAlert(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Syötetty aika on pienempi kuin viimeisin aika");
        alert.setContentText("Syötä aika, joka on listan viimeisimmän ajan jälkeen.");
        alert.showAndWait();
    }

    public void showNotCorrectTimeAlert(boolean isTooLarge){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        if(isTooLarge){
            alert.setHeaderText("Syötetty aika on suurempi kuin seuraava syötetty aika");
            alert.setContentText("Syötä aika, joka on ennen ajanhetkeä joka on seuraavan listalla.");
        } else {
            alert.setHeaderText("Syötetty aika on pienempi kuin edellinen aika");
            alert.setContentText("Syötä aika, joka on edellisen syötetyn ajanhetken jälkeen.");
        }
        alert.showAndWait();
    }

    public void showTimeInWrongFormatAlert(String problem){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Syötetty aika on väärässä formaatissa");
        alert.setContentText("Virhe: "+problem);
        alert.showAndWait();
    }

    public boolean showConfirmationAlert(String confirmationHeader, String confirmationText){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, confirmationText, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle("Vahvista valinta!");
        alert.setHeaderText(confirmationHeader);
        alert.showAndWait();

        return alert.getResult() == ButtonType.YES;
    }

    public void showSomethingWentWrongAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Error");
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
}
