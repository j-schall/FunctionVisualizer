package com.functionvisualizer.attributs;

import com.functionvisualizer.functions.LinearFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LineTest {

    @Test
    void INTERSECT() {
        LinearFunction func1 = new LinearFunction(2, 3);
        LinearFunction func2 = new LinearFunction(-2, 9);
        Coordinate intersectPoint = new Coordinate(1.5, 6);

        assertEquals(intersectPoint.getX(), Line.INTERSECT(func1, func2).getX());
        assertEquals(intersectPoint.getY(), Line.INTERSECT(func1, func2).getY());
    }

    @Test
    void POINTTEST() {
        Coordinate coordinate = new Coordinate(0, 5);
        LinearFunction function = new LinearFunction(4, 5);
        assertTrue(Line.POINT_TEST(coordinate, function));
    }
}