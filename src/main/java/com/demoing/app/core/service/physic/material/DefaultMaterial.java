package com.demoing.app.core.service.physic.material;

import com.demoing.app.core.service.physic.material.Material;

/**
 * Define a Bunch of default and standard material. See
 * As a reference :
 * <pre>
 *  Rock       Density : 0.6  Restitution : 0.1
 *  Wood       Density : 0.3  Restitution : 0.2
 *  Steal      Density : 1.2  Restitution : 0.05
 *  BouncyBall Density : 0.3  Restitution : 0.8
 *  SuperBall  Density : 0.3  Restitution : 0.95
 *  Pillow     Density : 0.1  Restitution : 0.2
 *  Static     Density : 0.0  Restitution : 0.0
 * </pre>
 */
public enum DefaultMaterial {
    DEFAULT(new Material("default", 1.0, 0.0, 1.0)),
    /*
    // convert from Material(elasticity,density,dynFriction,staticFriction)
    ROCK(new Material("rock", 0.6, 1, 1, 1)),
    WOOD(new Material("wood", 0.1, 0.69, 0.69, 0.3)),
    STEEL(new Material("metal", 0.05, 1, 1, 1.2)),
    RUBBER(new Material("rubber", 0.8, 0.88, 0.98, 0.3)),
    GLASS(new Material("glass", 0.4, 1, 1, 1)),
    ICE(new Material("ice", 0.1, 0.1, 1, 1)),
    AIR(new Material("air", 1, 1, 1, 0.01)),
    STATIC(new Material("static", 0, 0, 0, 0)),
    NEUTRAL(new Material("neutral", 1, 1, 1, 1));
    */;

    Material material;

    DefaultMaterial(Material m) {
        this.material = m;
    }

    public Material get() {
        return this.material;
    }
}
