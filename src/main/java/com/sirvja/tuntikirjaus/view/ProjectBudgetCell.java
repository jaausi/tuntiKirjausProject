package com.sirvja.tuntikirjaus.view;

import com.sirvja.tuntikirjaus.domain.ProjectBudgetItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ProjectBudgetCell extends ListCell<ProjectBudgetItem> {

    private final VBox container;
    private final Label nameLabel;
    private final Label hoursLabel;
    private final Label percentLabel;
    private final ProgressBar progressBar;
    private final List<ProjectBudgetItem> allItems;

    public ProjectBudgetCell(List<ProjectBudgetItem> allItems) {
        this.allItems = allItems;

        nameLabel = new Label();
        nameLabel.setStyle("-fx-font-weight: bold;");

        hoursLabel = new Label();
        hoursLabel.setStyle("-fx-font-size: 10;");

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(18);

        percentLabel = new Label();

        StackPane progressStack = new StackPane(progressBar, percentLabel);
        progressStack.setClip(null);

        container = new VBox(2, nameLabel, progressStack, hoursLabel);
        container.setStyle("-fx-padding: 4 6 4 6;");
    }

    @Override
    protected void updateItem(ProjectBudgetItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            long maxSpent = allItems.stream()
                    .mapToLong(ProjectBudgetItem::spentMinutes)
                    .max()
                    .orElse(1L);

            nameLabel.setText(item.projectName());
            hoursLabel.setText(item.hoursLabel());

            double progress = item.progress(maxSpent);
            progressBar.setProgress(Math.min(progress, 1.0));

            int percentage = (int) Math.round(progress * 100);
            percentLabel.setText(percentage + " %");

            percentLabel.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: #FCFCFC;");
            progressBar.setStyle("-fx-accent: " + progressColor(progress) + "; -fx-control-inner-background: #2B2B2B;");
            setGraphic(container);
        }
    }

    private String progressColor(double progress) {
        // green (0%) -> yellow (50%) -> red (100%+)
        double clamped = Math.min(progress, 1.0);
        int r, g;
        if (clamped <= 0.5) {
            // green to yellow: R 0->255, G stays 200
            r = (int) (clamped * 2 * 220);
            g = 200;
        } else {
            // yellow to red: R stays 220, G 200->0
            r = 220;
            g = (int) ((1.0 - clamped) * 2 * 200);
        }
        return String.format("#%02x%02x00", r, g);
    }
}
