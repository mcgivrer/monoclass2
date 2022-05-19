## Move to GamePLay

To go further in the demonstration, we need to offer the possibility to dynamically load scene to the core application.
To let instantiate Scene dynamically we need to extract the DemoScene from Application to a outer class.

We need some attributes scope review from private to public in most of the service, but anyway, first change this scope.

And the most important of this change is the way we are going to configure the scenes.

The new method loadScene() and a full reorganisation of the Application start will bring new flexibility:

### Configuration

All start by some new configuration attributes:

- `app.scenes` the list of scene with a  `[code]:[class],` list format,
- `app.scene.default` the default scene to be activated at game start.

```properties
# scenes
app.scenes=demo:com.demoing.app.scenes.DemoScene
app.scene.default=demo
```

### Loading Scenes

Loading the coma separated list of scene classes from `app.scenes` with their own activation key code, we provision the
list of Scene ready to be used, and use the `app.scene.default` scene at start.

```java
public class Application {
    //...
    private boolean loadScenes() {
        String[] scenesList = config.scenes.split(",");
        for (String scene : scenesList) {
            String[] sceneStr = scene.split(":");
            try {
                Class<?> clazzScene = Class.forName(sceneStr[1]);
                final Constructor<?> sceneConstructor = clazzScene.getConstructor(String.class);
                Scene s = (Scene) sceneConstructor.newInstance(sceneStr[0]);
                scenes.put(sceneStr[0], s);
                activateScene(config.defaultScene);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                    InvocationTargetException e) {
                System.out.println("ERR: Unable to load scene from configuration file:"
                        + e.getLocalizedMessage()
                        + "scene:" + sceneStr[0] + "=>" + sceneStr[1]);
                return false;
            }
        }
        return true;
    }
    //...
}
```

### Activating a scene

The scene activation will do 3 things:

1. release the previously activated scene,
2. then, find the requested scene,
3. create the scene.

```java
public class Application {
    //...
    protected void activateScene(String name) {
        if (scenes.containsKey(name)) {
            // (1)
            if (Optional.ofNullable(this.activeScene).isPresent()) {
                this.activeScene.dispose();
            }
            // (2)
            Scene scene = scenes.get(name);
            try {
                // (3)
                sceneReady = scene.create(this);
                this.activeScene = scene;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.print("ERR: Unable to load unknown scene " + name);
        }
    }
    //...
}
```

### Application start modification

The initialization is now really simplified and only read some configuration.

```java
public class Application {
    //...
    private void initialize(String[] args) {
        config = new Configuration("/app.properties").parseArgs(args);
        world = new World()
                .setArea(config.worldWidth, config.worldHeight)
                .setGravity(config.worldGravity);
    }
    //...

}
```

The run operation is now simplified and conditioned by the start resulting status

```java
public class Application {
    //...
    protected void run() {
        if (start()) {
            loop();
            dispose();
        }
    }
    //...
}
```

The start is initializing

1. all the intern services,
2. load the scenes and
3. created the window.
4. create the JMX metrics service

```java
public class Application {
    //...
    private boolean start() {
        try {
            // (1)
            initializeServices();
            // (2)
            createWindow();
            // (3)
            if (loadScenes()) {
                // (4)
                createJMXStatus(this);
                System.out.printf("INFO: scene %s activated and created.\n", activeScene.getName());
            }
        } catch (Exception e) {
            System.out.println("ERR: Unable to initialize scene: " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }
    //...
}
```

