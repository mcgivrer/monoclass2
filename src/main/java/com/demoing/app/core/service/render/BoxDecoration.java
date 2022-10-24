package com.demoing.app.core.service.render;

import java.awt.*;

/**
 * Adding some Decoration to a simple box drawn with
 * {@link Renderer#drawRectangleObject(Graphics2D, Color, Color, int, int, int, int, BoxDecoration)}.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 */
public enum BoxDecoration {
    NONE,
    DOT,
    LINE,
    SQUARE,
    SQUARE_LINE,
    SQUARE_LINE_DUAL;
}
