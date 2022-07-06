package com.demoing.app.core;

import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.gfx.DisplayModeEnum;
import com.demoing.app.core.io.ActionHandler;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.service.collision.CollisionDetector;
import com.demoing.app.core.service.monitor.AppStatus;
import com.demoing.app.core.service.physic.PhysicEngine;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.service.render.Render;
import com.demoing.app.core.service.scene.SceneManager;
import com.demoing.app.core.utils.I18n;
import com.demoing.app.core.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

/**
 * <p>{@link Application} is a Proof of Concept of a game mechanics, satisfying to some rules:
 * <ul>
 * <li>only one main java classe (sub classes and enum are authorized),</li>
 * <li> limit the number of line of code (without javadoc)</li>
 * <li> Build without any external tools but bash and JDK.</li>
 * </ul>
 * </p>
 * <p>
 * <p>The entrypoint is the {@link Application#run()} method to start.
 * Its reading its default configuration from an <code>app.properties</code> file.
 * </p>
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Application extends JPanel implements KeyListener {

    /**
     * The Frame per Second rendering rate default value
     */
    public static final int FPS_DEFAULT = 60;

    /**
     * Display MOde for the application window.
     */
    private DisplayModeEnum displayMode;

    public boolean exit = false;

    public boolean pause = false;

    private final boolean[] prevKeys = new boolean[65536];
    private final boolean[] keys = new boolean[65536];
    private boolean anyKeyPressed;
    private boolean keyCtrlPressed;
    private boolean keyShiftPressed;

    public Configuration config;
    public Render render;
    public SceneManager sceneMgr;
    private PhysicEngine physicEngine;
    private CollisionDetector collisionDetect;
    public ActionHandler actionHandler;


    private AppStatus appStats;

    private long realFps = 0;

    private long computationTime = 0;

    private Map<String, Entity> entities = new HashMap<>();
    public Map<String, Object> attributes = new HashMap<>();

    public World world;


    private JFrame frame;

    public Application(String[] args) {
        NumberFormat.getInstance(Locale.ROOT);
        initialize(args);
    }

    /**
     * Constructor used mainly for test purpose.
     *
     * @param args                  the list of arguments to be parsed by the Configuration
     * @param configurationFileName the configuration file path to be loaded by Configuration.
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
     * Initialize the Application with the default configuration file (app.proprerties)
     * and parse java CLI arguments.
     *
     * @param args the array of arguments from java CLI
     * @see Application#initialize(String[], String)
     */
    public void initialize(String[] args) {
        initialize(args, "/app.properties");
    }

    /**
     * Initialize the application by setting Configuration instance by loading
     * data from <code>configFileName</code>  and parsing java CLI arguments <code>args</code>.
     *
     * @param args           the CLI java arguments to be parsed (if provided)
     * @param configFileName the name of the configuration file to be loaded.
     * @see Configuration
     */
    public void initialize(String[] args, String configFileName) {
        config = new Configuration(configFileName).parseArgs(args);
        I18n.setLanguage(config);
        world = new World()
                .setArea(config.worldWidth, config.worldHeight)
                .setGravity(new Vec2d(0.0, config.worldGravity));
    }

    /**
     * Start the Application by :
     * <ul>
     *     <li>initializing services,</li>
     *     <li>open the Application's window</li>
     *     <li>and then init JMX monitoring service</li>
     * </ul>
     * a boolean start status is return according to the start operations.
     *
     * @return true if every thing goes well, false elsewhere.
     */
    private boolean start() {
        try {
            initializeServices();
            createWindow();
            if (sceneMgr.loadScenes()) {
                initDefaultActions();
                // prepare services
                createJMXStatus(this);
                Logger.log(1, this.getClass(), " scene %s activated and created.\n", sceneMgr.getActiveScene().getName());
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
     *     <li><kbd>F11</kbd> switch between window and fullscreen display,</li>
     *     <li><kbd>Z</kbd> reset the current scene,</li>
     *     <li><kbd>D</kbd> Switch debug level from 0 to 4,</li>
     *     <li><kbd>ESC</kbd> request to quit application,</li>
     *     <li><kbd>K</kbd> Kill player's energy (test quicky),</li>
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
                    setWindowMode(!config.fullScreen);
                    return this;
                }
        ));
    }

    /**
     * Initialize all Application services.
     * <p>
     * <blockquote><em>NOTE</em> This method is now public because of test requirements on services.</blockquote>
     *
     * @since 1.0.5
     */
    public void initializeServices() {
        render = new Render(this, config, world);
        sceneMgr = new SceneManager(this, config);
        physicEngine = new PhysicEngine(this, config, world);
        collisionDetect = new CollisionDetector(this, config, world);
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
            Logger.log(Logger.ERROR, this.getClass(), "ERR: Reset scene issue: " + e.getLocalizedMessage());
        }
    }

    private void createWindow() {
        setWindowMode(config.fullScreen);
    }

    /**
     * Create the JFrame window in fullscreen or windowed mode (according to fullScreenMode boolean value).
     *
     * @param fullScreenMode the display mode to be set:
     *                       <ul>
     *                       <li>true = DISPLAY_MODE_FULLSCREEN,</li>
     *                       <li>false = DISPLAY_MODE_WINDOWED</li>
     *                       </ul>
     */
    private void setWindowMode(boolean fullScreenMode) {
        GraphicsEnvironment graphics =
                GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice device = graphics.getDefaultScreenDevice();

        if (Optional.ofNullable(frame).isPresent() && frame.isVisible()) {
            frame.setVisible(false);
            frame.dispose();
        }

        frame = new JFrame(I18n.get("app.title"));
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/sg-logo-image.png")));
        frame.setContentPane(this);

        displayMode = fullScreenMode ? DisplayModeEnum.DISPLAY_MODE_FULLSCREEN : DisplayModeEnum.DISPLAY_MODE_WINDOWED;

        if (displayMode.equals(DisplayModeEnum.DISPLAY_MODE_FULLSCREEN)) {
            frame.setUndecorated(true);
            device.setFullScreenWindow(frame);
        } else {
            Dimension dim = new Dimension((int) (config.screenWidth * config.displayScale),
                    (int) (config.screenHeight * config.displayScale));
            frame.setSize(dim);
            frame.setPreferredSize(dim);
            frame.setMaximumSize(dim);
            frame.setLocationRelativeTo(null);
            frame.setUndecorated(false);
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setFocusTraversalKeysEnabled(true);

        frame.addKeyListener(this);
        frame.addKeyListener(actionHandler);

        frame.pack();
        frame.setVisible(true);
        if (Optional.ofNullable(frame.getBufferStrategy()).isEmpty()) {
            frame.createBufferStrategy(config.numberOfBuffer);
        }
    }

    public void requestExit() {
        exit = true;
    }

    public void addEntity(Entity entity) {
        render.addToPipeline(entity);
        collisionDetect.add(entity);
        entities.put(entity.name, entity);
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
        collisionDetect.colliders.remove(e);
        entities.remove(name);
    }

    public List<Entity> filterEntitiesOnName(String filterValue, int i) {
        List<Entity> etbr = entities.values()
                .stream()
                .filter(e -> e.name.contains(filterValue))
                .limit(i)
                .toList();
        return etbr;
    }

    public synchronized Entity getEntity(String name) {
        return entities.get(name);
    }

    public Application setAttribute(String attrName, Object attrValue) {
        this.attributes.put(attrName, attrValue);
        return this;
    }

    public Object getAttribute(String attrName, Object defaultValue) {
        return (this.attributes.getOrDefault(attrName, defaultValue));
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
            long waitTime = (config.frameTime > computationTime) ? config.frameTime - (long) computationTime : 1;

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
                Logger.log(Logger.ERROR, this.getClass(), "ERR: Unable to wait for " + waitTime + ": " + ie.getLocalizedMessage());
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

    public boolean isCtrlPressed() {
        return keyCtrlPressed;
    }

    public boolean isShiftPressed() {
        return keyShiftPressed;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        render.draw(realFps);
    }

    public void dispose() {
        if (Optional.ofNullable(frame).isPresent()) {
            frame.dispose();
        }
    }

    public void quit() {
        render.dispose();
        physicEngine.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        prevKeys[e.getKeyCode()] = keys[e.getKeyCode()];
        keys[e.getKeyCode()] = true;
        anyKeyPressed = true;
        this.keyCtrlPressed = e.isControlDown();
        this.keyShiftPressed = e.isShiftDown();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        prevKeys[e.getKeyCode()] = keys[e.getKeyCode()];
        keys[e.getKeyCode()] = false;
        anyKeyPressed = false;
        this.keyCtrlPressed = e.isControlDown();
        this.keyShiftPressed = e.isShiftDown();
    }

    public boolean getKeyPressed(int keyCode) {
        assert (keyCode < keys.length);
        return this.keys[keyCode];
    }

    public boolean getKeyReleased(int keyCode) {
        assert (keyCode < keys.length);
        boolean status = !this.keys[keyCode] && prevKeys[keyCode];
        prevKeys[keyCode] = false;
        return status;
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

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public World getWorld() {
        return world;
    }

    public Render getRender() {
        return render;
    }


    public JFrame getFrame() {
        return frame;
    }


    public long getRealFps() {
        return realFps;
    }

    public long getComputationTime() {
        return computationTime;
    }

    public static void main(String[] args) {
        try {
            Application app = new Application(args);
            app.run();
        } catch (Exception e) {
            Logger.log(Logger.ERROR, Application.class, "ERR: Unable to run application: %s",
                    e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

}
