package com.functionvisualizer.attributs;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

public class Coordinate extends Node {
    private SimpleDoubleProperty x, y;

    public Coordinate(double x, double y) {
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
    }

    public void setX(double x) {
        this.x = new SimpleDoubleProperty();
        this.x.set(x);
    }

    public void setY(double y) {
        this.y = new SimpleDoubleProperty();
        this.y.set(y);
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

    @Override
    public Node getStyleableNode() {
        return super.getStyleableNode();
    }
}

