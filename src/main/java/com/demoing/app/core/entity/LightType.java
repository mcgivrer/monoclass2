package com.demoing.app.core.entity;

/**
 * The list of light type.
 *
 * @author Frédéric Delorme
 * @since 1.0.5
 */
public enum LightType {
    /**
     * An {@link LightType#AMBIENT} light will display a colored rectangle on all the viewport,
     * with a color corresponding to the defined {@link Light#color}.
     */
    AMBIENT,
    /**
     * A {@link LightType#SPOT} light will display directional light of with {@link Light#color} at {@link Light#pos}.
     * The light direction and length is set by the {@link Light#rotation} and {@link Light#height} attributes.
     */
    SPOT,
    AREA_RECTANGLE,
    /**
     * A {@link LightType#SPHERICAL} light will display an ellipse centered light of {@link Light#width} x {@link Light#height}
     * with {@link Light#color} at {@link Light#pos}.
     */
    SPHERICAL
}
