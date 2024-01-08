package com.functionvisualizer.functions;

import com.functionvisualizer.attributs.Coordinate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

public class QuadraticFunction {

    private final Coordinate VERTEX = new Coordinate();
    private double a;

    public static void create(double a, double range, TableView<Coordinate> pointTable, XYChart.Series<Number, Number> series) {
        ObservableList<Coordinate> dataPoints = FXCollections.observableArrayList();
        series.getData().clear();

        for (double x = -range; x <= range; x++) {
            /* f(x)=a*x² */
            double y = a * Math.pow(x, 2);
            dataPoints.add(new Coordinate(x, y));
            series.getData().add(new XYChart.Data<>(x, y));
        }
        pointTable.setItems(dataPoints);
    }

    public static void createSPF(double a, double range, double d, double e, TableView<Coordinate> pointTable, XYChart.Series<Number, Number> series) {
        ObservableList<Coordinate> dataPoints = FXCollections.observableArrayList();
        for (double x = -range; x <= range; x++) {
            /* f(x)=a*(x-d)²+e */
            double y = a* Math.pow(x-d, 2) + e;
            dataPoints.add(new Coordinate(x, y));
            series.getData().add(new XYChart.Data<>(x, y));
            System.out.println("x=" + x + " y=" + y);
        }
        pointTable.setItems(dataPoints);
    }
}
