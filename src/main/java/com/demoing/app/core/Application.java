package com.demoing.app.core;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;

import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.gfx.Window;
import com.demoing.app.core.io.ActionHandler;
import com.demoing.app.core.service.collision.CollisionDetector;
import com.demoing.app.core.service.monitor.AppStatus;
import com.demoing.app.core.service.physic.PhysicEngine;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.service.scene.SceneManager;
import com.demoing.app.core.utils.I18n;
import com.demoing.app.core.utils.Logger;

/**
 * <p>
 * {@link Application} is a Proof of Concept of a game mechanics, satisfying to
 * some rules:
 * <ul>
 * <li>only one main java classe (sub classes and enum are authorized),</li>
 * <li>limit the number of line of code (without javadoc)</li>
 * <li>Build without any external tools but bash and JDK.</li>
 * </ul>
 * </p>
 * <p>
 * <p>
 * The entrypoint is the {@link Application#run()} method to start.
 * Its reading its default configuration from an <code>app.properties</code>
 * file.
 * </p>
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Application extends JPanel {
    /**
     * Exit flag to request exit from application from the main loop.
     *
     * @see Application#loop()
     */
    public boolean exit = false;
    /**
     * a Pause flag to set the game in pause mode from the mao loop,
     * preventing from rendering and updating applicaiton entities.
     *
     * @see Application#loop()
     */
    public boolean pause = false;
    /**
     * Configuration component supporting all confugruation from
     * file and the ones coming command line arguments.
     */
    public Configuration config;

    /**
     * The Window component lkeeping the link with OS events and graphics display.
     */
    public Window window;

    /**
     * The graphic component to assume all drawing.
     */
    public Renderer render;
    /**
     * THe Scene manager to chnage between different gameplay or game state.
     */
    public SceneManager sceneMgr;
    /**
     * Physic computation engine to update and maintain application's
     * managed entities regarding physic mechanic.
     */
    private PhysicEngine physicEngine;
    /**
     * THis collision detection component will provide new collision event and some
     * hook
     * to proceed to correst collision response, at entity and/or at Scene level.
     */
    private CollisionDetector collisionDetect;
    /**
     * Some actionListener to manage common application action,
     * like processing a specific global key event request
     */
    public ActionHandler actionHandler;

    /**
     * This Aplication status reporter is used by JMX to maintains internal
     * metrics to be shared with monitoring system.
     */
    private AppStatus appStats;

    /**
     * The real measured FPS; a render and update frame rate.
     */
    private long realFps = 0;

    /**
     * An internal metric regarding the global one loop cycle duration.
     */
    private long computationTime = 0;

    /**
     * Map of entities maintained by the Application.
     */
    private final Map<String, Entity> entities = new ConcurrentHashMap<>();
    /**
     * Some shared attributes than can be accessible
     * from everywhere in the application.
     */
    public Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * THe main constructor to start the Applciatiopn.
     * The only input are the possible command line arguments for configuration
     * purpose, overloading the default ones coming from configuration file.
     *
     * @param args the array of arguents coming from the java command line.
     */
    public Application(String[] args) {
        this(args, "app.properties");
    }

    /**
     * Constructor used mainly for test purpose.
     *
     * @param args                  the list of arguments to be parsed by the
     *                              Configuration
     * @param configurationFileName the configuration file path to be loaded by
     *                              Configuration.
     */
    public Application(String[] args, String configurationFileName) {
        NumberFormat.getInstance(Locale.ROOT);
        initialize(args, configurationFileName);
    }

    protected void run() {
        if (start()) {
            loop();
            dispose();
        }
    }

    /**
     * Initialize the application by setting Configuration instance by loading
     * data from <code>configFileName</code> and parsing java CLI arguments
     * <code>args</code>.
     *
     * @param args           the CLI java arguments to be parsed (if provided)
     * @param configFileName the name of the configuration file to be loaded.
     * @see Configuration
     */
    public void initialize(String[] args, String configFileName) {
        config = new Configuration(configFileName).parseArgs(args);
        I18n.setLanguage(config);
    }

    /**
     * Start the Application by :
     * <ul>
     * <li>initializing services,</li>
     * <li>open the Application's window</li>
     * <li>and then init JMX monitoring service</li>
     * </ul>
     * a boolean start status is return according to the start operations.
     *
     * @return true if every thing goes well, false elsewhere.
     */
    private boolean start() {
        try {
            initializeServices();

            if (sceneMgr.loadScenes(config.scenes.split(","))) {
                initDefaultActions();
                // prepare services
                createJMXStatus(this);
                Logger.log(1, this.getClass(), " scene %s activated and created.\n",
                        sceneMgr.getActiveScene().getName());
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR, this.getClass(), "ERR: Unable to initialize scene: " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Define some pre-wired actions on some keys:
     * <ul>
     * <li><kbd>F11</kbd> switch between window and fullscreen display,</li>
     * <li><kbd>Z</kbd> reset the current scene,</li>
     * <li><kbd>D</kbd> Switch debug level from 0 to 4,</li>
     * <li><kbd>ESC</kbd> request to quit application,</li>
     * <li><kbd>K</kbd> Kill player's energy (test quicky),</li>
     * </ul>
     *
     * @author Frédéric Delorme
     * @since 1.0.4
     */
    private void initDefaultActions() {
        actionHandler.actionMapping.putAll(Map.of(
                // reset the scene
                KeyEvent.VK_Z, o -> {
                    reset();
                    return this;
                },
                // manage debug level
                KeyEvent.VK_D, o -> {
                    config.debug = config.debug + 1 < 5 ? config.debug + 1 : 0;
                    return this;
                },
                // I quit !
                KeyEvent.VK_ESCAPE, o -> {
                    requestExit();
                    return this;
                },
                KeyEvent.VK_K, o -> {
                    Entity p = entities.get("player");
                    p.setAttribute("energy", 0);
                    return this;
                },
                KeyEvent.VK_F11, o -> {
                    window.setWindowMode(!config.fullScreen);
                    return this;
                }));
    }

    /**
     * Initialize all Application services.
     * <p>
     * <blockquote><em>NOTE</em> This method is now public because of test
     * requirements on services.</blockquote>
     *
     * @since 1.0.5
     */
    public void initializeServices() {

        // create window.
        window = new Window(this);
        sceneMgr = new SceneManager(this, config);
        physicEngine = new PhysicEngine(this, config);
        render = new Renderer(this, physicEngine.getWorld());
        collisionDetect = new CollisionDetector(this, config, physicEngine.getWorld());
        actionHandler = new ActionHandler(this);
    }

    private void createJMXStatus(Application application) {
        appStats = new AppStatus(this, application, "Application");
        appStats.register(application);
    }

    public void reset() {
        try {
            render.clear();
            entities.clear();
            Entity.entityIndex = 0;
            sceneMgr.createScene();
        } catch (Exception e) {
            Logger.log(Logger.ERROR, this.getClass(),
                    "ERR: Reset scene issue: ",
                    e.getLocalizedMessage());
        }
    }

    public void requestExit() {
        exit = true;
    }

    private void loop() {
        long timeFrame = 0, frames = 0;
        long previous = System.currentTimeMillis();
        while (!exit) {

            long start = System.currentTimeMillis();
            double elapsed = start - previous;

            input();
            update(elapsed);
            render.draw(realFps);

            // wait at least 1ms.
            computationTime = System.currentTimeMillis() - start;
            long waitTime = (config.frameTime > computationTime) ? config.frameTime - computationTime : 1;

            timeFrame += elapsed;
            frames += 1;
            if (timeFrame > 1000) {
                timeFrame = 0;
                realFps = frames;
                frames = 0;
            }
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ie) {
                Logger.log(Logger.ERROR, this.getClass(),
                        "ERR: Unable to wait for " + waitTime + ": " + ie.getLocalizedMessage());
            }

            // Update JMX metrics
            appStats.update(this);

            previous = start;
        }
    }

    private void input() {
        sceneMgr.getActiveScene().input(this);
    }

    private synchronized void update(double elapsed) {
        if (!pause) {
            double maxElapsedTime = Math.min(elapsed, config.frameTime);
            physicEngine.update(maxElapsedTime);
            collisionDetect.update(maxElapsedTime);
            if (sceneMgr.isSceneReady()) {
                sceneMgr.getActiveScene().update(this, elapsed);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        render.draw(realFps);
    }

    public void dispose() {
        if (Optional.ofNullable(window).isPresent()) {
            window.dispose();
        }
    }

    public void quit() {
        render.dispose();
        physicEngine.dispose();
    }

    public void addEntity(Entity entity) {
        render.addToPipeline(entity);
        collisionDetect.add(entity);
        entities.put(entity.name, entity);
        entity.getChild().forEach(this::addEntity);
    }

    public void removeEntity(String filterValue, int i) {
        i = (i == -1) ? entities.size() : i;
        List<Entity> etbr = filterEntitiesOnName(filterValue, i);
        for (int idx = 0; idx < i; idx++) {
            if (idx < etbr.size()) {
                Entity e = etbr.get(idx);
                removeEntity(e.name);
            }
        }
    }

    public void removeEntity(String name) {
        Entity e = entities.get(name);
        render.remove(e);
        collisionDetect.remove(e);
        entities.remove(name);
    }

    public List<Entity> filterEntitiesOnName(String filterValue, int i) {
        return entities.values()
                .stream()
                .filter(e -> e.name.contains(filterValue))
                .limit(i)
                .toList();
    }

    public synchronized Entity getEntity(String name) {
        return entities.get(name);
    }

    public Application setAttribute(String attrName, Object attrValue) {
        this.attributes.put(attrName, attrValue);
        return this;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public Renderer getRender() {
        return render;
    }

    public SceneManager getSceneManager() {
        return this.sceneMgr;
    }

    public PhysicEngine getPhysicEngine() {
        return physicEngine;
    }

    public CollisionDetector getCollisionDetector() {
        return collisionDetect;
    }

    public Window getWindow() {
        return this.window;
    }

    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public long getRealFps() {
        return realFps;
    }

    public long getComputationTime() {
        return computationTime;
    }

    public Object getAttribute(String attrName, Object defaultValue) {
        return (this.attributes.getOrDefault(attrName, defaultValue));
    }

    public static void main(String[] args) {
        try {
            Application app = new Application(args);
            app.run();
        } catch (Exception e) {
            Logger.log(Logger.ERROR,
                    Application.class,
                    "ERR: Unable to run application: %s",
                    e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

}
