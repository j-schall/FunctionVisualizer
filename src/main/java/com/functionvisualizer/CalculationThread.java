package com.functionvisualizer;

import com.functionvisualizer.functions.LineareFunction;
import com.functionvisualizer.functions.ProportionalFunction;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

public class CalculationThread extends Thread {
    @Override
    public void run() {
        double range = FunctionVisualizer.range;
        double m = FunctionVisualizer.m;
        double b = FunctionVisualizer.b;
        TableView<Coordinate> coordinateTable = FunctionVisualizer.coordinateTable;
        XYChart.Series series = FunctionVisualizer.series;

        LineareFunction lFunc = new LineareFunction();
        ProportionalFunction pFunc = new ProportionalFunction();

        // Aktualisieren der UI im JavaFX Application Thread
        Platform.runLater(() -> {
            if (FunctionVisualizer.isPressed) {
                lFunc.create(m, b, range, coordinateTable, series);
            } else {
                pFunc.create(m, range, coordinateTable, series);
            }
        });
    }
}
