package com.demoing.app.core.scene;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.entity.Light;

import java.util.List;
import java.util.Map;

public interface Scene {
    void prepare();

    boolean create(Application app) throws Exception;

    /**
     * Update phase for this Scene.
     *
     * @param app     the parent Application instance to access services
     * @param elapsed the elapsed time since previous call.
     */
    void update(Application app, double elapsed);

    /**
     * Manage and capture input at Scene level
     *
     * @param app
     */
    void input(Application app);

    /**
     * Retrieve the name of the scene.
     *
     * @return
     */
    String getName();

    /**
     * The map of behaviors attached to this Scene and executed during the update cycle.
     *
     * @return a Map of Behaviors implementations.
     */
    Map<String, Behavior> getBehaviors();

    /**
     * Retrieve the list of Light manage by the scene.
     *
     * @return a list of Light
     */
    List<Light> getLights();

    void dispose();
}
