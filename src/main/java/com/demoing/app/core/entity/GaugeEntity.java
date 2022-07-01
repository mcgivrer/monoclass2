package com.demoing.app.core.entity;

import com.demoing.app.core.service.physic.PhysicType;

import java.awt.*;

/**
 * <p> A {@link GaugeEntity} extending the {@link Entity} to display a Gauge on the HUD, to show Energy or Mana
 * of a dedicated Entity.</p>
 * <p></p>At declaration, you must set a Min and  max <code>value</code> representing the gauge <code>minValue</code>
 * and <code>maxValue</code> for your value:
 * <pre>
 * GaugeEntity myGE = new GaugeEntity("myValue")
 *   .setPosition(10,10)
 *   .setSize(8,40)
 *   .setMin(0)
 *   .setMax(100)
 *   .setColor(Color.RED);
 * </pre>
 * </p>
 */
public class GaugeEntity extends Entity {
    public double value = 0;
    public double maxValue;
    private double minValue;
    public Color border = Color.GRAY;
    public Color shadow = Color.DARK_GRAY;

    public GaugeEntity(String name) {
        super(name);
        physicType = PhysicType.STATIC;
        stickToCamera = true;
    }

    public GaugeEntity setValue(double v) {
        this.value = v;
        return this;
    }

    public GaugeEntity setMax(double mxV) {
        this.maxValue = mxV;
        return this;
    }

    public GaugeEntity setMin(double mnV) {
        this.minValue = mnV;
        return this;
    }
}
