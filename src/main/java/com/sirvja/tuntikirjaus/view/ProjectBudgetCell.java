package com.sirvja.tuntikirjaus.view;

import com.sirvja.tuntikirjaus.domain.ProjectBudgetItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.util.List;

public class ProjectBudgetCell extends ListCell<ProjectBudgetItem> {

    private final VBox container;
    private final Label nameLabel;
    private final Label hoursLabel;
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

        container = new VBox(2, nameLabel, progressBar, hoursLabel);
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

            // Color the bar red if over budget
            if (item.budgetMinutes().isPresent() && progress > 1.0) {
                progressBar.setStyle("-fx-accent: #e74c3c;");
            } else if (item.budgetMinutes().isPresent() && progress >= 0.8) {
                progressBar.setStyle("-fx-accent: #e67e22;");
            } else {
                progressBar.setStyle("");
            }

            setGraphic(container);
        }
    }
}

