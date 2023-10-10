package com.functionvisualizer;

import com.functionvisualizer.attributs.Coordinate;
import com.functionvisualizer.functions.LinearFunction;
import com.functionvisualizer.functions.ProportionalFunction;
import com.functionvisualizer.functions.SimpleQuadraticFunction;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

public class CalculationThread extends Thread {
    @Override
    public void run() {
        double range = FunctionVisualizer.RANGE;
        double m = FunctionVisualizer.M;
        double b = FunctionVisualizer.B;
        TableView<Coordinate> coordinateTable = FunctionVisualizer.COORDINATE_TABLE;
        XYChart.Series<Number, Number> series = FunctionVisualizer.SERIES;

        LinearFunction lFunc = new LinearFunction();
        ProportionalFunction pFunc = new ProportionalFunction();
        SimpleQuadraticFunction sqf = new SimpleQuadraticFunction();

        // Aktualisieren der UI im JavaFX Application Thread
        Platform.runLater(() -> {
            switch (FunctionVisualizer.FUNCTION_INDEX) {
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
