package com.functionvisualizer.functions;

import com.functionvisualizer.attributs.Coordinate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

public class LinearFunction {

    private double b, m;

    public LinearFunction() {

    }

    public LinearFunction(double m, double b) {
        this.m = m;
        this.b = b;
    }

    public static void create(double m, double b, double range, TableView<Coordinate> pointTable, XYChart.Series<Number, Number> series) {
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

        this.m = (y2 - y1) / (x2 - x1);
        // y-Achsenabschnitt b berechnen, indem man den Rest der Subtraktion, von y1
        // und dem Ergebnis der Multipplikation von m und x, berechnet
        this.b = y1 - m * x1;

        // Wenn b kleiner als 0 ist, dann soll das Vorzeichen von b angezeigt werden, statt dem +
        return b < 0 ? "f(x)=" + m + "*" + "x" + b : "f(x)=" + m + "*" + "x" + "+" + b;
    }

    public double getM() {
        return m;
    }

    public double getB() {
        return b;
    }
}
