package com.demoing.app.core.entity.helpers;

import com.demoing.app.core.entity.Light;

/**
 * The list of light type.
 *
 * @author Frédéric Delorme
 * @since 1.0.5
 */
public enum LightType {
    /**
     * An light will display a colored rectangle on all the viewport,
     * with a color corresponding to the defined {@link Light#color}.
     */
    AMBIENT,
    /**
     * A light will display directional light of with {@link Light#color} at {@link Light#pos}.
     * The light direction and length is set by the rotation and {@link Light#height} attributes.
     */
    SPOT,
    AREA_RECTANGLE,
    /**
     * A light will display an ellipse centered light of {@link Light#width} x {@link Light#height}
     * with {@link Light#color} at {@link Light#pos}.
     */
    SPHERICAL
}
