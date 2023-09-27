package com.functionvisualizer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

public class CalculationThread extends Thread {

    @Override
    public void start() {
        double range = FunctionVisualizer.range;
        double m = FunctionVisualizer.m;
        double b = FunctionVisualizer.b;
        TableView<Coordinate> coordinateTable = FunctionVisualizer.coordinateTable;
        XYChart.Series series = FunctionVisualizer.series;

        if (FunctionVisualizer.isPressed) {
            createLinearFunction(m, b, range, coordinateTable, series);
        } else {
            createProportionaleFunction(m, range, coordinateTable, series);
        }
    }


    public static void createLinearFunction(double m, double b, double range, TableView<Coordinate> pointTable, XYChart.Series series) {
        ObservableList<Coordinate> dataPoints = FXCollections.observableArrayList();
        series.getData().clear();
        for (double x = -range; x <= range; x++) { //Variable range definiert die Länge des Graphen
            double y = m * x + b;
            dataPoints.add(new Coordinate(x, y));
            series.getData().add(new XYChart.Data<>(x, y));
        }
        pointTable.setItems(dataPoints);
    }

    public String calculateLineareFunction(Coordinate one, Coordinate two) {
        //1. Steigung m berechnen:
        double y2 = two.getY();
        double x2 = two.getX();
        double y1 = one.getY();
        double x1 = one.getX();

        double m = (y2 - y1) / (x2 - x1);
        // y-Achsenabschnitt b berechnen, indem man den Rest der Subtraktion, von y1
        // und dem Ergebnis der Multipplikation von m und x, berechnet
        double b = y1 - m * x1;
        // Wenn b kleiner als 0 ist, dann soll das Vorzeichen von b angezeigt werden, statt dem +
        if (b < 0) return "f(x)=" + m + "*" + "x" + b;

        return "f(x)=" + m + "*" + "x" + "+" + b;
    }


    public static void createProportionaleFunction(double m, double range, TableView<Coordinate> pointTable, XYChart.Series series) {
        ObservableList<Coordinate> dataPoints = FXCollections.observableArrayList();
        series.getData().clear();
        for (double x = -range; x <= range; x++) {
            double y = m * x;
            dataPoints.add(new Coordinate(x, y));
            series.getData().add(new XYChart.Data<>(x, y));
        }
        pointTable.setItems(dataPoints);
    }
}
