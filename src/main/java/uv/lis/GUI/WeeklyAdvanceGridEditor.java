package uv.lis.GUI;

import java.util.Optional;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import uv.lis.logic.dto.Activity;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.utils.InputValidator;
import uv.lis.logic.utils.WorkProgressCalculator;

public class WeeklyAdvanceGridEditor {

    private static final String WEEK_LABEL_PREFIX = "Semana ";
    private static final String EMPTY_CELL = "";
    private static final String WEEKLY_FIELD_NAME = "El avance de cada semana";
    private static final String SUM_FIELD_PREFIX = "La suma del avance de ";
    private static final double CELL_WIDTH = 70.0;
    private static final int FIRST_DATA_INDEX = 1;
    private static final int HEADER_INDEX = 0;
    private static final int MAX_ADVANCE = 100;

    private final GridPane gridContainer;
    private TextField[][] cellFields;
    private Activity[] editedActivities;

    public WeeklyAdvanceGridEditor(GridPane gridContainer) {
        this.gridContainer = gridContainer;
        this.cellFields = new TextField[PartialReport.MAX_WEEKS][PartialReport.MAX_ACTIVITIES];
        this.editedActivities = new Activity[PartialReport.MAX_ACTIVITIES];
    }

    public void buildGrid(Activity[] activitiesBySlot, int[][] advances) {
        gridContainer.getChildren().clear();
        cellFields = new TextField[PartialReport.MAX_WEEKS][PartialReport.MAX_ACTIVITIES];
        editedActivities = activitiesBySlot;

        int weekCount = calculateWeekCount(activitiesBySlot);
        addColumnHeaders(activitiesBySlot);
        addWeekRows(activitiesBySlot, advances, weekCount);
    }

    public void applyEditsTo(int[][] advances) {
        for (int weekIndex = 0; weekIndex < PartialReport.MAX_WEEKS; weekIndex++) {
            for (int slot = 0; slot < PartialReport.MAX_ACTIVITIES; slot++) {
                TextField cell = cellFields[weekIndex][slot];

                if (cell != null && !cell.isDisable()) {
                    Optional<Integer> value = parseCellValue(cell.getText());

                    if (value.isPresent()) {
                        advances[weekIndex][slot] = value.get();
                    }
                }
            }
        }
    }

    public Optional<String> validateAdvances() {
        Optional<String> validationError = Optional.empty();
        int slot = 0;

        while (slot < PartialReport.MAX_ACTIVITIES && validationError.isEmpty()) {
            if (editedActivities[slot] != null) {
                validationError = validateColumn(slot);
            }
            slot++;
        }
        return validationError;
    }
    
    private Optional<String> validateColumn(int slot) {
        Optional<String> validationError = Optional.empty();
        int columnSum = 0;

        for (int weekIndex = 0; weekIndex < PartialReport.MAX_WEEKS; weekIndex++) {
            TextField cell = cellFields[weekIndex][slot];

            if (cell != null && !cell.isDisable()) {
                String cellText = cell.getText().trim();

                if (!cellText.isEmpty()) {
                    Optional<String> cellError = InputValidator.validateMaxIntValue(cellText, MAX_ADVANCE, 
                        WEEKLY_FIELD_NAME);

                    if (cellError.isPresent()) {
                        validationError = cellError;
                    } else {
                        columnSum += Integer.parseInt(cellText);
                    }
                }
            }
        }

        if (validationError.isEmpty()) {
            String sumFieldName = SUM_FIELD_PREFIX + editedActivities[slot].getName();
            validationError = InputValidator.validateMaxIntValue(
                String.valueOf(columnSum), MAX_ADVANCE, sumFieldName);
        }
        return validationError;
    }

    private int calculateWeekCount(Activity[] activitiesBySlot) {
        int weekCount = FIRST_DATA_INDEX;

        for (Activity activity : activitiesBySlot) {
            
            if (activity != null) {
                int activityWeeks = resolveActivityWeeks(activity);

                if (activityWeeks > weekCount) {
                    weekCount = activityWeeks;
                }
            }
        }
        return weekCount;
    }

    private int resolveActivityWeeks(Activity activity) {
        int activityWeeks = WorkProgressCalculator.calculateActivityWeeks(activity);
        int totalWeeks = Math.min(activityWeeks, PartialReport.MAX_WEEKS);
        return totalWeeks;
    }

    private void addColumnHeaders(Activity[] activitiesBySlot) {
        for (int slot = 0; slot < PartialReport.MAX_ACTIVITIES; slot++) {
            Activity activity = activitiesBySlot[slot];

            if (activity != null) {
                Label header = new Label(activity.getName());
                gridContainer.add(header, FIRST_DATA_INDEX + slot, HEADER_INDEX);
            }
        }
    }

    private void addWeekRows(Activity[] activitiesBySlot, int[][] advances, int weekCount) {
        for (int weekIndex = 0; weekIndex < weekCount; weekIndex++) {
            int gridRow = weekIndex + FIRST_DATA_INDEX;
            Label weekLabel = new Label(WEEK_LABEL_PREFIX + (weekIndex + FIRST_DATA_INDEX));

            gridContainer.add(weekLabel, HEADER_INDEX, gridRow);
            addWeekCells(activitiesBySlot, advances, weekIndex);
        }
    }

    private void addWeekCells(Activity[] activitiesBySlot, int[][] advances, int weekIndex) {
        int gridRow = weekIndex + FIRST_DATA_INDEX;

        for (int slot = 0; slot < PartialReport.MAX_ACTIVITIES; slot++) {
            Activity activity = activitiesBySlot[slot];

            if (activity != null) {
                TextField cell = createCell(activity, advances[weekIndex][slot], weekIndex);
                cellFields[weekIndex][slot] = cell;
                gridContainer.add(cell, FIRST_DATA_INDEX + slot, gridRow);
            }
        }
    }

    private TextField createCell(Activity activity, int value, int weekIndex) {
        TextField cell = new TextField();
        cell.setPrefWidth(CELL_WIDTH);
        boolean isWeekBelongingToActivity = weekIndex < resolveActivityWeeks(activity);

        if (isWeekBelongingToActivity) {
            cell.setText(String.valueOf(value));
        } else {
            cell.setText(EMPTY_CELL);
            cell.setDisable(true);
        }
        return cell;
    }
    
    private Optional<Integer> parseCellValue(String rawValue) {
        Optional<Integer> value = Optional.empty();

        if (rawValue != null && !rawValue.isBlank()) {
            value = Optional.of(Integer.parseInt(rawValue.trim()));
        }
        return value;
    }
}