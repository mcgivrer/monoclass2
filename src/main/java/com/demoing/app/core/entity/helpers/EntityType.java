package com.demoing.app.core.entity.helpers;

import com.demoing.app.core.entity.Entity;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * The {@link EntityType} define the type of rendered entity, a RECTANGLE, an ELLIPSE or an IMAGE (see {@link BufferedImage}.
 */
public enum EntityType {
    /**
     * An {@link Entity} having a {@link EntityType#RECTANGLE} type will be drawn as a rectangle with the {@link Rectangle2D} shape.
     */
    RECTANGLE,
    /**
     * An {@link Entity} having a {@link EntityType#ELLIPSE} type will be drawn as an ellipse with the {@link Ellipse2D} share.
     */
    ELLIPSE,
    /**
     * An {@link Entity} having a {@link EntityType#IMAGE} type will be drawn as a {@link BufferedImage}.
     */
    IMAGE,
    /**
     * for any Entity that are not visible.
     */
    NONE;
}
