package com.demoing.app.core.entity;

import com.demoing.app.core.service.physic.PhysicType;

import java.awt.image.BufferedImage;

/**
 * <p>The {@link ValueEntity} extends the {@link Entity} by adding the capability to display an integer value.</p>
 * <p>The ValueEntity will be created as follow: setting {@link ValueEntity} specific attributes like
 * the <code>value</code>, the display <code>format</code>, the array of {@link BufferedImage}
 * as <code>figures</code>, and then, the {@link Entity} inherited attributes:</p>
 * <pre>
 * ValueEntity scoreEntity = (ValueEntity) new ValueEntity("score")
 *   .setValue(score)
 *   .setFormat("%06d")
 *   .setFigures(figs)
 *   .setPosition(20, 20)
 *   .setSize(6 * 8, 16)
 *   .setStickToCamera(true);
 * app.addEntity(scoreEntity);
 * </pre>
 *
 * @author Frédéric Delorme
 * @since 1.0.2
 */
public class ValueEntity extends Entity {
    int value;
    public String valueTxt;
    public BufferedImage[] figures;
    private String format = "%d";

    /**
     * Create a new {@link ValueEntity} with its name.
     *
     * @param name the name of this new {@link ValueEntity}.
     */
    public ValueEntity(String name) {
        super(name);
        this.physicType = PhysicType.STATIC;
    }

    /**
     * Value displayed as text must be updated according to the value and its string format.
     * This is what happened during the update() processing.
     *
     * @param elapsed the elapsed time since previous call.
     */
    @Override
    public void update(double elapsed) {
        super.update(elapsed);
        valueTxt = String.format(format, value);
    }

    /**
     * Define the {@link ValueEntity} value to be displayed.
     *
     * @param value the new value for this {@link ValueEntity}.
     * @return this ValueEntity with its new value.
     */
    public ValueEntity setValue(int value) {
        this.value = value;
        return this;
    }

    /**
     * Define the array of {@link BufferedImage} as {@link ValueEntity#figures} to render this integer
     * {@link ValueEntity#value}.
     *
     * @param figures the new BufferedImage array to be used as figures (Must provide an array of 10 {@link BufferedImage},
     *                corresponding to the 10 digits from 0 to 9).
     * @return this {@link ValueEntity} with its new figures to be used to draw the integer value.
     */
    public ValueEntity setFigures(BufferedImage[] figures) {
        this.figures = figures;
        return this;
    }

    /**
     * The value for {@link ValueEntity} must be transformed to a String with some conversion rule according
     * to the {@link String#format(String, Object...)} method.
     *
     * @param f the new {@link String#format(String, Object...)} value to be used for integer to String
     *          conversion (default is "%d").
     * @return this {@link ValueEntity} with is new String format attribute.
     */
    public ValueEntity setFormat(String f) {
        this.format = f;
        return this;
    }
}
