package com.demoing.app.core.math;

import com.demoing.app.core.utils.Utils;

/**
 * {@link MathUtils} is a mathematics utilities class to provide some basic operations
 *
 * @author Frédéric Delorme
 * @since 1.0.5
 */
public class MathUtils {
    /**
     * toolbox to define and fix ceil value
     *
     * @param x    the value to "ceilled"
     * @param ceil the level of ceil to apply to x value.
     * @return value with ceil applied.
     */
    public static double ceilValue(double x, double ceil) {
        return Math.copySign((Math.abs(x) < ceil ? 0 : x), x);
    }

    /**
     * min-max-range to apply to a x value.
     *
     * @param x   the value to be constrained between min and max.
     * @param min minimum for the x value.
     * @param max maximum for the x value.
     * @return
     */
    public static double ceilMinMaxValue(double x, double min, double max) {
        return ceilValue(Math.copySign((Math.abs(x) > max ? max : x), x), min);
    }
}
