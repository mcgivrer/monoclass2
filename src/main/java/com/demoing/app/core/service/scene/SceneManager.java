package com.demoing.app.core.service.scene;

import com.demoing.app.core.Application;
import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.scene.Scene;
import com.demoing.app.core.utils.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SceneManager {
    private final Application application;
    private final Configuration config;

    /**
     * THe map of Scene to be activated int o the Application instance.
     * See <code>app.scenes</code> and <code>add.default.scene</code> in configuration files.
     */
    private Map<String, Scene> scenes = new HashMap<>();
    /**
     * Scene readiness flag. After loaded and activated, the scene readiness state is set to true.
     */
    private boolean sceneReady;


    private Scene activeScene;

    public SceneManager(Application application, Configuration config) {
        this.application = application;
        this.config = config;
    }

    /**
     * Read Scenes and set the default scene according to {@link Configuration}.
     * the concerned properties entries are:
     * <ul>
     *     <li><code>app.scene.list</code>is a list of Scene implementation classes comma separated,</li>
     *     <li><code>app.scene.default</code> is the scene to be activated at start (by default).</li>
     * </ul>
     *
     * @return true if Scene is correctly loaded, else false.
     * @see Configuration
     */
    public boolean loadScenes(String[] scenesList) {
        if (Optional.ofNullable(scenesList).isPresent()
                && !scenesList[0].equals("")) {
            for (String scene : scenesList) {
                String[] sceneStr = scene.split(":");
                try {
                    Class<?> clazzScene = Class.forName(sceneStr[1]);
                    final Constructor<?> sceneConstructor = clazzScene.getConstructor(String.class);
                    Scene s = (Scene) sceneConstructor.newInstance(sceneStr[0]);
                    scenes.put(sceneStr[0], s);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException |
                         InvocationTargetException e) {
                    Logger.log(Logger.ERROR, this.getClass(), "ERR: Unable to load scene from configuration file:"
                            + e.getLocalizedMessage()
                            + "scene:" + sceneStr[0] + "=>" + sceneStr[1]);
                    e.printStackTrace(System.out);
                    return false;
                }
            }
            activateScene(config.defaultScene);
        }
        return true;
    }

    public void createScene() throws Exception {
        activeScene.create(application);
    }

    public void activateScene(String name) {
        if (scenes.containsKey(name)) {
            if (Optional.ofNullable(this.activeScene).isPresent()) {
                this.activeScene.dispose();
            }
            Scene scene = scenes.get(name);
            try {
                scene.prepare();
                sceneReady = scene.create(application);
                this.activeScene = scene;
            } catch (Exception e) {
                Logger.log(Logger.ERROR, this.getClass(), "ERR: Unable to initialize the Scene " + name + " => " + e.getLocalizedMessage());
            }
        } else {
            Logger.log(Logger.ERROR, this.getClass(), "ERR: Unable to load unknown scene " + name);
        }
    }

    public Scene getActiveScene() {
        return this.activeScene;
    }

    public boolean isSceneReady() {
        return this.sceneReady;
    }

    public void addScene(String sceneName, Scene scene) {
        scenes.put(sceneName, scene);
    }
}
