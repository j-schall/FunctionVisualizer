package com.functionvisualizer.functions;

import com.functionvisualizer.attributs.Coordinate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

public class ProportionalFunction {
    public void create(double m, double range, TableView<Coordinate> pointTable, XYChart.Series<Number, Number> series) {
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
