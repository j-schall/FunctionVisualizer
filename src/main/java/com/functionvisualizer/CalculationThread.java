package com.functionvisualizer;

import com.functionvisualizer.functions.LinearFunction;
import com.functionvisualizer.functions.ProportionalFunction;
import com.functionvisualizer.functions.SimpleQuadraticFunction;
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

        LinearFunction lFunc = new LinearFunction();
        ProportionalFunction pFunc = new ProportionalFunction();
        SimpleQuadraticFunction sqf = new SimpleQuadraticFunction();

        // Aktualisieren der UI im JavaFX Application Thread
        Platform.runLater(() -> {
            switch (FunctionVisualizer.funcIndex) {
                case 1:
                    pFunc.create(m, range, coordinateTable, series);
                    break;
                case 2:
                    lFunc.create(m, b, range, coordinateTable, series);
                    break;
                case 3:
                    sqf.create(m, range, coordinateTable, series);
                    break;
            }
        });
    }
}
