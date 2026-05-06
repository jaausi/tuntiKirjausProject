package com.sirvja.tuntikirjaus.domain;

import java.util.OptionalLong;

public record ProjectBudgetItem(String projectName, long spentMinutes, OptionalLong budgetMinutes) {

    public double progress(long maxSpentMinutes) {
        if (budgetMinutes.isPresent() && budgetMinutes.getAsLong() > 0) {
            return (double) spentMinutes / budgetMinutes.getAsLong();
        }
        if (maxSpentMinutes > 0) {
            return (double) spentMinutes / maxSpentMinutes;
        }
        return 0.0;
    }

    public String hoursLabel() {
        long hours = spentMinutes / 60;
        long minutes = spentMinutes % 60;
        String spent = String.format("%d:%02d", hours, minutes);

        if (budgetMinutes.isPresent()) {
            long bh = budgetMinutes.getAsLong() / 60;
            long bm = budgetMinutes.getAsLong() % 60;
            return String.format("%s / %d:%02d h", spent, bh, bm);
        }
        return spent + " h";
    }
}

