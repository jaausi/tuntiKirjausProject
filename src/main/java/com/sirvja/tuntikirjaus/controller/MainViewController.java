package com.sirvja.tuntikirjaus.controller;

import com.sirvja.AutoCompleteTextField;
import com.sirvja.BudgetProgressCell;
import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.ProjectBudgetItem;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.exception.EmptyTopicException;
import com.sirvja.tuntikirjaus.exception.MalformatedTimeException;
import com.sirvja.tuntikirjaus.exception.StartTimeNotAfterLastTuntikirjausException;
import com.sirvja.tuntikirjaus.exception.TuntikirjausDatabaseInInconsistentStage;
import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.service.MainViewService;
import com.sirvja.tuntikirjaus.utils.CustomLocalTimeStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MainViewController implements Initializable {

    private MainViewService mainViewService;
    private AlertService alertService;

    private static final Logger log = LoggerFactory.getLogger(MainViewController.class);

    @FXML
    private TableView<TuntiKirjaus> tuntiTaulukko = new TableView<>();
    @FXML
    private TableColumn<TuntiKirjaus, LocalTime> kellonaikaColumn;
    @FXML
    private TableColumn<TuntiKirjaus, String> aiheColumn;
    @FXML
    private TableColumn<TuntiKirjaus, String> tunnitColumn;
    @FXML
    private TableColumn<TuntiKirjaus, Boolean> remoteColumn;
    @FXML
    private AutoCompleteTextField<String> aiheField;
    @FXML
    private CheckBox etatyoCheckbox;
    @FXML
    private ListView<Paiva> daysListView = new ListView<>();
    @FXML
    private MenuItem reportsMenuItem;
    @FXML
    private MenuItem settingsMenuItem;
    @FXML
    private MenuItem undoMenuItem;
    @FXML
    private MenuItem redoMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private TextField kellonAikaField;
    @FXML
    private Button tallennaTaulukkoonButton;
    @FXML
    private Button uusiPaivaButton;
    @FXML
    private Button poistaKirjausButton;
    @FXML
    private Font x3;
    @FXML
    private Color x4;
    @FXML
    private ListView<ProjectBudgetItem> projectBudgetListView;
    private Object valueBeforeEdit;
    private final Set<TuntiKirjaus> remoteListenerRegistered = Collections.newSetFromMap(new WeakHashMap<>());

    public MainViewController() {
        this.mainViewService = new MainViewService();
        this.alertService = new AlertService();
    }

    @Override
    public void initialize (URL url, ResourceBundle rb){
        tuntiTaulukko.setEditable(true);

        kellonaikaColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        aiheColumn.setCellValueFactory(cellData -> cellData.getValue().topicProperty());
        tunnitColumn.setCellValueFactory(cellData -> cellData.getValue().durationStringProperty());
        remoteColumn.setCellValueFactory(cellData -> {
            TuntiKirjaus kirjaus = cellData.getValue();
            if (remoteListenerRegistered.add(kirjaus)) {
                kirjaus.remoteProperty().addListener(
                        mainViewService.getRemoteColumnListener(kirjaus, tuntiTaulukko::refresh));
            }
            return kirjaus.remoteProperty();
        });
        remoteColumn.setCellFactory(CheckBoxTableCell.forTableColumn(remoteColumn));


        kellonaikaColumn.setSortable(false);
        aiheColumn.setSortable(false);
        tunnitColumn.setSortable(false);
        remoteColumn.setSortable(false);

        setEditListenerToKellonaikaColumn();

        setEditListenerToAiheColumn();

        setListenerForDayListView();

        updateDayList();

        daysListView.getSelectionModel().selectFirst();

        initializeAutoCompleteAiheField();
        updateProjectBudgets();
    }

    private void initializeAutoCompleteAiheField() {
        aiheField.getEntries().addAll(mainViewService.getAiheEntries());
        aiheField.getLastSelectedObject().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null){
                aiheField.setText(newValue);
                aiheField.positionCaret(newValue.length());
                aiheField.setLastSelectedItem(null);
            }
        });
    }

    private void setListenerForDayListView() {
        daysListView.getSelectionModel().selectedItemProperty().addListener(mainViewService.getDayListChangeListener(() -> {
            updateTuntiTaulukko();
            updateProjectBudgets();
        }));
    }

    private void setEditListenerToAiheColumn() {
        BiConsumer<String, String> updateAiheFieldEntry = (oldTopic, newTopic) -> {
            aiheField.getEntries().remove(oldTopic);
            aiheField.getEntries().add(newTopic);
        };

        aiheColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        aiheColumn.setOnEditCommit(mainViewService.getAiheColumnEditHandler(updateAiheFieldEntry));
    }

    private void setEditListenerToKellonaikaColumn() {
        kellonaikaColumn.setCellFactory(TextFieldTableCell.forTableColumn(new CustomLocalTimeStringConverter(mainViewService)));
        kellonaikaColumn.setOnEditCommit(mainViewService.getKellonaikaColumnEditHandler(tuntiTaulukko::refresh));
    }

    @FXML
    protected void onChangeAboutMenuItem() {

    }

    @FXML
    protected void onChangeRedoMenuItem() {

    }

    @FXML
    protected void onChangeUndoMenuItem() {

    }

    @FXML
    protected void onKeyPressedToAiheField(){
        aiheField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                log.debug("Enter was pressed");
                onTallennaTaulukkoonButtonClick();
            }
        });
    }

    @FXML
    protected void onKeyPressedToKellonaikaField(){
        kellonAikaField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                log.debug("Enter was pressed");
                onTallennaTaulukkoonButtonClick();
            }
        });
    }

    @FXML
    protected void onOpenReportsMenuItem(){
        log.debug("Open reports clicked!");
        openModalView(new FXMLLoader(TuntikirjausApplication.class.getResource("reports-hub-view.fxml"), ResourceBundle.getBundle("com.sirvja.tuntikirjaus.i18n")), "Raportit");
    }

    @FXML
    protected void onOpenSettings() {
        log.debug("Open settings clicked!");
        Stage settingsStage = openModalView(new FXMLLoader(TuntikirjausApplication.class.getResource("settings-view.fxml"), ResourceBundle.getBundle("com.sirvja.tuntikirjaus.i18n")), "Asetukset");
        // Project budgets may have changed in the settings window
        settingsStage.setOnHidden(event -> updateProjectBudgets());
    }

    private static Stage openModalView(FXMLLoader fxmlLoader, String viewTitle) {
        try {
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(viewTitle);
            Scene scene = new Scene(root1);
            TuntikirjausApplication.applyTheme(scene);
            stage.setScene(scene);
            stage.show();
            return stage;
        } catch (IOException e) {
            throw new RuntimeException("Couldn't open view " + viewTitle, e);
        }
    }

    @FXML
    protected void onTallennaTaulukkoonButtonClick() {
        try {
            log.debug("Save to table button pushed!");
            TuntiKirjaus tuntiKirjaus = mainViewService.addTuntikirjaus(kellonAikaField.getText(), aiheField.getText(), etatyoCheckbox.isSelected());
            log.info("Added kirjaus with id {}", tuntiKirjaus.getId());
            aiheField.getEntries().add(tuntiKirjaus.getTopic());
            initializeView();
        } catch (EmptyTopicException e) {
            // Invalid user input is expected, so these are warnings instead of errors
            log.warn("Kirjaus not saved: {}", e.getMessage());
            aiheField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showFieldNotFilledAlert();
        } catch (MalformatedTimeException e) {
            log.warn("Kirjaus not saved: {}", e.getMessage());
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showTimeInWrongFormatAlert(e.getMessage());
        } catch (StartTimeNotAfterLastTuntikirjausException e) {
            log.warn("Kirjaus not saved: {}", e.getMessage());
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showNotCorrectTimeAlert();
        } catch (TuntikirjausDatabaseInInconsistentStage e) {
            log.error("Kirjaus not saved, database is in inconsistent state", e);
            alertService.showGeneralAlert(e.getMessage());
        }
    }

    @FXML
    protected void onPoistaKirjausButtonClick() {
        log.debug("Poista kirjaus painettu!");
        TuntiKirjaus selectedKirjaus = tuntiTaulukko.getSelectionModel().getSelectedItem();
        if (selectedKirjaus == null) {
            log.debug("No kirjaus selected, nothing to remove");
            return;
        }
        log.debug("Following kirjaus selected: {}", selectedKirjaus.toLogString());
        if(!alertService.showConfirmationAlert("Oletko varma että haluat poistaa kirjauksen",
                String.format("Poistettava kirjaus: \n%s" +
                        "\nKirjauksen poistaminen muokkaa, poistettavaa edeltävän kirjauksen kestoa " +
                "siirtämällä lopetusajan poistettavan kirjauksen lopetusaikaan.", selectedKirjaus))){
            return;
        }
        mainViewService.removeTuntikirjaus(selectedKirjaus);
        log.info("Removed kirjaus with id {}", selectedKirjaus.getId());
        initializeView();
    }

    @FXML
    protected void onUusiPaivaButtonClick() {
        log.debug("Uusi päivä clicked!");
        daysListView.getItems().addFirst(new Paiva(LocalDate.now()));
        daysListView.getSelectionModel().selectFirst();
    }

    @FXML
    protected void onKellonaikaFieldClick(){
        log.debug("Kellonaika field clicked!");
        kellonAikaField.setStyle("-fx-border-color: none ; -fx-border-width: 0px ;");
    }

    @FXML
    protected void onAiheFieldClick(){
        log.debug("Aihe field clicked!");
        aiheField.setStyle("-fx-border-color: none ; -fx-border-width: 0px ;");
    }

    private void initializeView(){
        updateTuntiTaulukko();
        updateDayList();
        updateProjectBudgets();
        clearInputFields();
    }

    private void updateTuntiTaulukko() {
        tuntiTaulukko.setItems(mainViewService.getTuntiDataForTable());
        tuntiTaulukko.refresh();
    }

    private void updateProjectBudgets() {
        ObservableList<ProjectBudgetItem> items = mainViewService.getMonthlyProjectBudgetItems().stream()
                .filter(item -> item.budgetMinutes().isPresent())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        SortedList<ProjectBudgetItem> sorted = new SortedList<>(
                items,
                Comparator.comparingLong(ProjectBudgetItem::spentMinutes).reversed()
        );

        projectBudgetListView.setSelectionModel(null);
        projectBudgetListView.setItems(sorted);
        projectBudgetListView.setCellFactory(lv -> new BudgetProgressCell<>());
    }

    private void clearInputFields() {
        kellonAikaField.clear();
        aiheField.clear();
    }

    private void updateDayList() {
        daysListView.setItems(mainViewService.getPaivaDataForTable());
        daysListView.refresh();
    }
}
