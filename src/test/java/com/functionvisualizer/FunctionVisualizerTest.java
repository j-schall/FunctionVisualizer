package com.functionvisualizer;

import com.functionvisualizer.attributs.Coordinate;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FunctionVisualizerTest {

    @Test
    void parseToInteger() {
        FunctionVisualizer visualizer = new FunctionVisualizer();
        String input1 = "32;1";
        List<Double> result1 = visualizer.parseToInteger(input1);
        assertEquals(List.of(32, 1), result1);

        String input2 = "String";
        List<Double> result2 = visualizer.parseToInteger(input2);
        assertEquals(List.of(), result2);

        String input3 = "";
        List<Double> result3 = visualizer.parseToInteger(input3);
        assertEquals(List.of(), result3);
    }

    @Test
    void distanceToMouse() {
        Point2D mousePos = new Point2D(1, 2);
        double expectedMousePos = 2.236;

        double xPow = Math.pow(mousePos.getX(), 2);
        double yPow = Math.pow(mousePos.getY(), 2);

        double sum = xPow + yPow;
        double c = Math.sqrt(sum);

        String fC = String.format("%.3f", c);
        fC = fC.replace(",", ".");
        double formattedC = Double.parseDouble(fC);

        assertEquals(expectedMousePos, formattedC);
    }
}