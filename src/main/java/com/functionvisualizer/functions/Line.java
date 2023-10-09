package com.functionvisualizer.functions;

import com.functionvisualizer.Coordinate;

public class Line {
    public static Coordinate INTERSECT(LinearFunction func1, LinearFunction func2) {
        double b1 = func1.getB();
        double b2 = func2.getB();
        double m1 = func1.getM();
        double m2 = func2.getM();
        double cacheB;
        double cacheM;
        double x;

        /* Wird geprüft, ob die zweite Steigung bzw. der y-Achsenabschnitt größer ist, um nicht in den negativen
           Zahlenbereich zu kommen -> spart einen Zwischenschritt

           mathematische Herleitung:
           f(x) = 2x + 3
           g(x) = -3x + 9

           2x + 3 = -3x + 9 |+3x
           2x + 3x + 3 = 9  |-3
           2x + 3x = 9 - 3 => m1 - m2 = b1 - b2
         */
        if (b1 < b2) cacheB = b2 - b1;
        else cacheB = b1 - b2;
        if (m1 < m2) cacheM = m2 - m1;
        else cacheM = m1 - m2;

        double temp = cacheM;
        cacheM /= cacheM;
        cacheB /= temp;

        x = cacheB;
        double y = m1 * x + b1;
        return new Coordinate(x, y);
    }

    public static boolean POINTTEST(Coordinate point, LinearFunction func) {
        double m = func.getM();
        double b = func.getB();
        double x = point.getX();
        double assertY = point.getY();

        double y = m * x + b;
        return assertY == y;
    }
}
