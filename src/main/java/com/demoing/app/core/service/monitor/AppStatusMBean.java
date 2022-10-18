package com.demoing.app.core.service.monitor;

import com.demoing.app.core.Application;
import com.demoing.app.core.service.render.Renderer;

/**
 * This MBean is for metrics and action exposition through the JMX service. Connecting with the JConsole
 * to this java process will provide some metrics and action.
 */
public interface AppStatusMBean {
    /**
     * Current level of debugging
     *
     * @return
     */
    Integer getDebugLevel();

    /**
     * Set the level of debugging.
     *
     * @param d an int value from 0 to 5
     */
    void setDebugLevel(Integer d);

    /**
     * Retrieve the current number of entities.
     *
     * @return an INteger value correspondong to the size of the {@link Application#entities} Map.
     */
    Integer getNbEntities();

    /**
     * Return the number of elements in  the {@link Renderer} graphic pipeline (see {@link Renderer#gPipeline}).
     *
     * @return the size fo the gPipeline list.
     */
    Integer getPipelineSize();

    /**
     * Return the current status of the PAUSE flag.
     *
     * @return true f the application is in pause mode.
     */
    Boolean getPauseStatus();

    /**
     * Define and set the Pause ode to true or false.
     *
     * @param pause
     */
    void setPauseStatus(Boolean pause);

    /**
     * Retrieve the value for the update spent time.
     *
     * @return a value in nanoseconds.
     */
    Long getTimeUpdate();

    /**
     * Retrieve the value for the rendering spent time.
     *
     * @return a value in nanoseconds.
     */
    Long getTimeRendering();

    /**
     * Retrieve the value for the global computation spent time.
     *
     * @return a value in nanoseconds.
     */
    Long getTimeComputation();

    /**
     * Retrieve the real Frame Per Second measured in the main loop.
     *
     * @return
     */
    Long getRealFPS();

    /**
     * Request to exit from application.
     */
    void requestQuit();

    /**
     * Add <code>nbEntitiesToAdd</code> random entities
     *
     * @param nbEntitiesToAdd the number of entities to be added.
     * @deprecated
     */
    @Deprecated
    void requestAddEntity(Integer nbEntitiesToAdd);

    /**
     * Remove <code>nbEntitiesToRemove</code> entities
     *
     * @param nbEntitiesToRemove the number of entities to be removed
     * @deprecated
     */
    @Deprecated
    void requestRemoveEntity(Integer nbEntitiesToRemove);

    /**
     * Request to reset the current active Scene.
     */
    void requestReset();

}
