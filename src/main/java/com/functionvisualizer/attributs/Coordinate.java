package com.functionvisualizer.attributs;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;

public class Coordinate {
    private final SimpleDoubleProperty x, y;

    public Coordinate(double x, double y) {
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
    }

    public double getX() {
        return x.get();
    }

    public double getY() {
        return y.get();
    }

    public ObservableValue<Double> xProperty() {
        return x.asObject();
    }

    public ObservableValue<Double> yProperty() {
        return y.asObject();
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
