package com.functionvisualizer;

import com.functionvisualizer.attributs.Coordinate;
import com.functionvisualizer.functions.LinearFunction;
import com.functionvisualizer.functions.ProportionalFunction;
import com.functionvisualizer.functions.QuadraticFunction;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

import java.util.Arrays;

public class CalculationThread extends Thread {
    private final TableView<Coordinate> coordinateTable = FunctionVisualizer.COORDINATE_TABLE;
    @Override
    public void run() {
        double range = FunctionVisualizer.RANGE;
        double m = FunctionVisualizer.M;
        double b = FunctionVisualizer.B;
        String function = FunctionVisualizer.SELECTED_FUNC;
        Object[] functions = FunctionVisualizer.FUNCTIONS.keySet().toArray();
        System.out.println(Arrays.toString(functions));
        XYChart.Series<Number, Number> series = FunctionVisualizer.SERIES;

        // Aktualisieren der UI im JavaFX Application Thread
        Platform.runLater(() -> {
            if (function.equals(functions[0])) {
                QuadraticFunction.create(m, range, coordinateTable, series);
                scrollEvent();
            } else if (function.equals(functions[1])) {
                ProportionalFunction.create(m, range, coordinateTable, series);
                scrollEvent();
            } else if (function.equals(functions[2])) {
                LinearFunction.create(m, b, range, coordinateTable, series);
                scrollEvent();
            }
        });
    }

    // Scroll to the intersection point where the graph intersect with the y-axis
    private void scrollEvent() {
        Platform.runLater(() -> {
            for (Coordinate coor : coordinateTable.getItems()) {
                if (coor.getX() == 0) {
                    int index = coordinateTable.getItems().indexOf(coor);
                    coordinateTable.requestFocus();
                    coordinateTable.getSelectionModel().select(index);
                    coordinateTable.getFocusModel().focus(index);
                    coordinateTable.scrollTo(index);
                }
            }
        });
    }
}
