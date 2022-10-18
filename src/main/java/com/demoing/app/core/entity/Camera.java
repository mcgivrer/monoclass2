package com.demoing.app.core.entity;

import com.demoing.app.core.Application;
import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.scene.Scene;

import java.awt.geom.Rectangle2D;

/**
 * <p>The {@link Camera}, extending the {@link Entity} object, has a specific role. It will set the point of view to
 * show all the {@link Entity} from the {@link Scene} in the {@link World}.</p>
 * <p>This camera position will be set according to an {@link Entity}  target position, to be tracked.</p>
 * <p>The {@link Camera} position will be computed with a specific tweenFactor, adding a certain delay to the tracking
 * position, acting as a spring between the camera and its target.</p>
 * <p>To define a camera, you must set the camera name, and its target and its viewport:</p>
 *
 * <pre>
 * Camera cam = new Camera("cam01")
 *   .setViewport(new Rectangle2D.Double(0, 0, app.config.screenWidth, app.config.screenHeight))
 *   .setTarget(player)
 *   .setTweenFactor(0.005);
 * app.render.addCamera(cam);
 * </pre>
 *
 * <p>The viewport mainly corresponds to the size of the displayed window
 * (see {@link Configuration#screenWidth} and {@link Configuration#screenHeight}).</p>
 *
 * <p>The tweenFactor a value from 0.0 to 1.0 is a delay on target tracking.</p>
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Camera extends Entity {

    private Entity target;
    private double tweenFactor;
    private Rectangle2D viewport;

    /**
     * Create a new {@link Camera} with its name.
     *
     * @param name the name for the newly created {@link Camera}.
     */
    public Camera(String name) {
        super(name);
        this.physicType = PhysicType.STATIC;
    }

    /**
     * Define the {@link Camera} target to be tracked.
     *
     * @param target the target to tracked by this {@link Camera}, it must be an {@link Entity}.
     * @return this {@link Camera} with its new target to be tracked.
     */
    public Camera setTarget(Entity target) {
        this.target = target;
        return this;
    }

    /**
     * The tween factor value to compute the delay on tracking the target.
     *
     * @param tf the new tweenFactor for this {@link Camera}.
     * @return the {@link Camera} with its new tween factor
     */
    public Camera setTweenFactor(double tf) {
        this.tweenFactor = tf;
        return this;
    }

    /**
     * The {@link Camera#viewport} display corresponding to the JFrame display size. This is the view from the camera.
     *
     * @param vp the new viewport for this {@link Camera}.
     * @return this {@link Camera} with ots new Viewport.
     */
    public Camera setViewport(Rectangle2D vp) {
        this.viewport = vp;
        return this;
    }

    /**
     * This {@link Camera#pos} will be computed during the update phase of the {@link Application#update(double)},
     * according to the {@link Camera#target} position and the {@link Camera#tweenFactor}.
     *
     * @param elapsed the elapsed time since the previous call, contributing to the new {@link Camera}'s position
     *                computation with the tweenFactor value and the {@link Camera#target}.
     */
    public void update(double elapsed) {
        pos.x += Math.round((target.pos.x + target.width - (viewport.getWidth() * 0.5) - pos.x) * tweenFactor * elapsed);
        pos.y += Math.round((target.pos.y + target.height - (viewport.getHeight() * 0.5) - pos.y) * tweenFactor * elapsed);
    }

    public Rectangle2D getViewport() {
        return viewport;
    }
}
