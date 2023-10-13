package com.functionvisualizer;

import org.junit.jupiter.api.Test;

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
}