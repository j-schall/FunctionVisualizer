package com.functionvisualizer;

import com.functionvisualizer.attributs.Coordinate;
import com.functionvisualizer.functions.LinearFunction;
import com.functionvisualizer.functions.ProportionalFunction;
import com.functionvisualizer.functions.SimpleQuadraticFunction;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

public class CalculationThread extends Thread {
    @Override
    public void run() {
        double range = FunctionVisualizer.RANGE;
        double m = FunctionVisualizer.M;
        double b = FunctionVisualizer.B;
        MenuItem item = FunctionVisualizer.SELECTED_ITEM;
        Object[] functions = FunctionVisualizer.FUNCTIONS.keySet().toArray();

        TableView<Coordinate> coordinateTable = FunctionVisualizer.COORDINATE_TABLE;
        XYChart.Series<Number, Number> series = FunctionVisualizer.SERIES;

        // Aktualisieren der UI im JavaFX Application Thread
        Platform.runLater(() -> {
            if (item.getText().equals(functions[0])) {
                SimpleQuadraticFunction.create(m, range, coordinateTable, series);
            } else if (item.getText().equals(functions[1])) {
                ProportionalFunction.create(m, range, coordinateTable, series);
            } else if (item.getText().equals(functions[2])) {
                LinearFunction.create(m, b, range, coordinateTable, series);
            }
        });
    }
}
