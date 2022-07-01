package com.demoing.app.core.service.monitor;

import com.demoing.app.core.Application;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * Implementation of the JMX service to deliver the AppStatusMBean.
 * (please see JMX API on officiel support site)
 */
public class AppStatus implements AppStatusMBean {
    private final Application application;
    private Application app;
    private int debugLevel;
    private int nbEntities, pipelineSize;
    boolean pauseStatus;
    private long realFPS, timeRendering, timeUpdate, computationTime;
    private String programName;

    /**
     * Creating the AppStatus object to full feed all the {@link AppStatusMBean} attributes with the
     * {@link Application} and other services measures.
     *
     * @param app  The parent {@link Application} this {@link AppStatus} belongs to.
     * @param name the name for this AppStatus object displayed by the JMX client.
     */
    public AppStatus(Application application, Application app, String name) {
        this.application = application;
        this.programName = name;
        this.nbEntities = 0;
    }

    /**
     * Registering the Application into the JMX API.
     *
     * @param app the parent {@link Application} this AppStatus will be feed with.
     */
    public void register(Application app) {

        this.app = app;
        try {
            // Register the object in the MBeanServer
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName("com.demoing.app:name=" + programName);
            platformMBeanServer.registerMBean(this, objectName);
        } catch (InstanceAlreadyExistsException
                 | MBeanRegistrationException
                 | NotCompliantMBeanException
                 | MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    /**
     * The update mechanism to retrieve metrics values.
     *
     * @param app the parent {@link Application} this {@link AppStatus} belongs to.
     */
    public synchronized void update(Application app) {
        nbEntities = app.entities.size();
        realFPS = app.getRealFps();
        pipelineSize = app.render.getgPipeline().size();
        timeRendering = app.render.getRenderingTime();
        timeUpdate = app.getPhysicEngine().updateTime;
        pauseStatus = app.pause;
        debugLevel = app.config.debug;
        computationTime = app.getComputationTime();
    }

    public synchronized Integer getDebugLevel() {
        return debugLevel;
    }

    @Override
    public synchronized void setDebugLevel(Integer d) {
        application.config.debug = d;
    }

    @Override
    public synchronized Integer getNbEntities() {
        return nbEntities;
    }

    @Override
    public synchronized Integer getPipelineSize() {
        return pipelineSize;
    }

    @Override
    public synchronized Boolean getPauseStatus() {
        return pauseStatus;
    }

    @Override
    public void setPauseStatus(Boolean p) {
        application.pause = p;
    }

    @Override
    public synchronized Long getTimeUpdate() {
        return timeUpdate;
    }

    @Override
    public synchronized Long getTimeRendering() {
        return timeRendering;
    }

    @Override
    public synchronized Long getTimeComputation() {
        return computationTime;
    }

    @Override
    public synchronized Long getRealFPS() {
        return realFPS;
    }

    @Override
    public synchronized void requestQuit() {
        app.exit = true;
    }

    @Override
    @Deprecated
    public synchronized void requestAddEntity(Integer nbEntity) {
    }

    @Override
    @Deprecated
    public synchronized void requestRemoveEntity(Integer nbEntity) {
    }

    /**
     * Action to request the reset of the {@link Application} to restart the current game level.
     */
    @Override
    public synchronized void requestReset() {
        app.reset();
    }
}
