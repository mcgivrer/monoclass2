package com.demoing.app.core.entity;

import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.scene.Scene;

import java.awt.*;

import static com.demoing.app.core.entity.EntityType.NONE;

/**
 * <p>A {@link Light} class to simulate lights in a {@link Scene}.</p>
 * It can be a {@link LightType#SPOT}, an {@link LightType#AMBIENT} or a {@link LightType#SPHERICAL} one.
 * It will have an {@link Light#energy}, and specific {@link Light#rotation}
 * angle and a {@link Light#height}  (for SPOT only), or a {@link Light#width} and {@link Light#height}
 * of the ellipse size(for SPHERICAL only) and a glitter effect (see {@link Light#glitterEffect},
 * to simulate neon glittering light.
 *
 * @author Frédéric Delorme
 * @since 1.0.5
 */
public class Light extends Entity {
    public Color[] colors;
    public float[] dist;
    public RadialGradientPaint rgp;
    public double energy;
    public LightType lightType;
    private double rotation;
    public double glitterEffect;

    /**
     * Create a new Light with a name
     *
     * @param name the name of this new light in the Scene.
     */
    public Light(String name) {
        super(name);
        setType(NONE);
        setPhysicType(PhysicType.NONE);
    }

    /**
     * Set the light type;
     *
     * @param lt the LightType to be assigned to this light.
     * @return the updated Light entity.
     */
    public Light setLightType(LightType lt) {
        this.lightType = lt;
        return this;
    }

    /**
     * Define the energy for this Light.
     *
     * @param e the value of energy from 0 to 1.0.
     * @return the updated Light entity.
     */
    public Light setEnergy(double e) {
        this.energy = e;
        return this;
    }

    /**
     * Define the light spot direction (only for {@link LightType#SPOT}
     *
     * @param r the rotation angle in radian.
     * @return the updated Light entity.
     */
    public Light setRotation(double r) {
        this.rotation = r;
        return this;
    }

    /**
     * The glitterEffect factor, adding an offset to the light center to create aglitter effect.
     *
     * @param ge the Glitter factor from 0 to 1.0
     * @return the updated Light entity.
     */
    public Light setGlitterEffect(double ge) {
        this.glitterEffect = ge;
        return this;
    }

    @Override
    public void update(double elapsed) {
        super.update(elapsed);
    }
}
