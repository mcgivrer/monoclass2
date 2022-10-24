package com.demoing.app.core.behavior;

import com.demoing.app.core.Application;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.scene.AbstractScene;
import com.demoing.app.core.scene.Scene;

public interface Behavior {
    String ON_COLLISION = "onCollide";
    String ON_UPDATE_ENTITY = "updateEntity";
    String ON_UPDATE_SCENE = "updateScene";

    String ON_INPUT_SCENE = "inputScene";

    String filterOnEvent();

    /**
     * A default creator for this behavior to be called on Behavior initialization.
     *
     * @param s the concerned {@link Scene} by the Behavior
     */
    default void initialization(AbstractScene s) {

    }

    /**
     * A default creator for this behavior to be called on Behavior initialization.
     *
     * @param e the concerned {@link Entity} by the Behavior
     */
    default void initialization(Entity e) {

    }

    void update(Application a, Entity e, double elapsed);

    void update(Application a, double elapsed);

    void onCollide(Application a, Entity e1, Entity e2);

    void input(Application a, Scene s);

}
