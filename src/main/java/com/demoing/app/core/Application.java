package com.demoing.app.core;

import javax.imageio.ImageIO;
import javax.management.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.demoing.app.core.Application.EntityType.*;

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
    private static final int FPS_DEFAULT = 60;
    /**
     * internal counter for entity id.
     */
    private static long entityIndex = 0;
    /**
     * THe map of Scene to be activated int o the Application instance.
     * See <code>app.scenes</code> and <code>add.default.scene</code> in configuration files.
     */
    private Map<String, Scene> scenes = new HashMap<>();
    /**
     * Scene readiness flag. After loaded and activated, the scene readiness state is set to true.
     */
    private boolean sceneReady;

    /**
     * Display MOde for the application window.
     */
    private DisplayModeEnum displayMode;

    /**
     * The possible display mode for the {@link Application} window.
     */
    public enum DisplayModeEnum {
        /**
         * The {@link Render} will display the {@link Application} window (see {@link JFrame}) in a Full screen mode.
         */
        DISPLAY_MODE_FULLSCREEN,
        /**
         * The {@link Render} will display the {@link Application} window (see {@link JFrame}) as a normal window with a title bar.
         */
        DISPLAY_MODE_WINDOWED,
    }

    /**
     * The {@link EntityType} define the type of rendered entity, a RECTANGLE, an ELLIPSE or an IMAGE (see {@link BufferedImage}.
     */
    public enum EntityType {
        /**
         * An {@link Entity} having a {@link EntityType#RECTANGLE} type will be drawn as a rectangle with the {@link Rectangle2D} shape.
         */
        RECTANGLE,
        /**
         * An {@link Entity} having a {@link EntityType#ELLIPSE} type will be drawn as an ellipse with the {@link Ellipse2D} share.
         */
        ELLIPSE,
        /**
         * An {@link Entity} having a {@link EntityType#IMAGE} type will be drawn as a {@link BufferedImage}.
         */
        IMAGE,
        /**
         * for any Entity that are not visible.
         */
        NONE;
    }

    /**
     * The {@link PhysicType} is used by the PhysicEngine to compute physic behavior for the object.
     * It can be STATIC for static object like static platform, or DYNAMIC for moving objects.
     */
    public enum PhysicType {
        /**
         * An {@link Entity} with a {@link PhysicType#DYNAMIC} physic type will be managed by the {@link PhysicEngine} as a dynamic object,
         */
        DYNAMIC,
        /**
         * An {@link Entity} with a {@link PhysicType#STATIC} physic type will not be modified by the {@link PhysicEngine}.
         */
        STATIC,
        /**
         * An Entity with a {@link PhysicType#NONE} physic type will not be manage in any way by the physic engine.
         */
        NONE
    }

    /**
     * THe TextAlign attribute value is use for TextEntity only, to define how the rendered text must be aligned.
     * Possible values are LEFT, CENTER and RIGHT.
     */
    public enum TextAlign {
        /**
         * The text provided for the {@link TextEntity} will be justified on LEFT side of the text rectangle position.
         */
        LEFT,
        /**
         * The text provided for the {@link TextEntity} will be centered on its current position.
         */
        CENTER,
        /**
         * The text provided for the {@link TextEntity} will be justified on RIGHT side of the text rectangle position.
         */
        RIGHT
    }

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
         * Return the number of elements in  the {@link Render} graphic pipeline (see {@link Render#gPipeline}).
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

    /**
     * Implementation of the JMX service to deliver the AppStatusMBean.
     * (please see JMX API on officiel support site)
     */
    public class AppStatus implements AppStatusMBean {
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
        public AppStatus(Application app, String name) {
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
            realFPS = app.realFps;
            pipelineSize = app.render.gPipeline.size();
            timeRendering = app.render.renderingTime;
            timeUpdate = app.physicEngine.updateTime;
            pauseStatus = app.pause;
            debugLevel = app.config.debug;
            computationTime = app.computationTime;
        }

        public synchronized Integer getDebugLevel() {
            return debugLevel;
        }

        @Override
        public synchronized void setDebugLevel(Integer d) {
            config.debug = d;
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
            pause = p;
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

    /**
     * The Configuration class provide default attributes values provision from a <code>app.properties</code> file.
     * Based on a simple {@link Properties} java class, it eases the initialization of the {@link Application}.
     */
    public static class Configuration {
        Properties appProps = new Properties();
        /**
         * default width of the screen
         */
        public double screenWidth = 320.0;
        /**
         * default height of the screen
         */
        public double screenHeight = 200.0;
        /**
         * display pixel scale at start.
         */
        public double displayScale = 2.0;
        /**
         * the required Frame Per Second to update game mechanic and render to screen.
         */
        public double fps = 0.0;
        /**
         * The internal display debug level to display infirmation at rendering time on screen
         * (level from 0 =No debug to 5 max level debug info).
         */
        public int debug;

        public long frameTime = 0;

        /**
         * Default World play area width
         */
        public double worldWidth = 0;
        /**
         * Default World play area height
         */
        public double worldHeight = 0;
        /**
         * Default World play area gravity
         */
        public double worldGravity = 1.0;
        /**
         * Flag to define fullscreen mode.  true=> full screen.
         */
        public boolean fullScreen = false;

        public int numberOfBuffer = 2;

        /**
         * Default minimum speed for PhysicEngine. under this value, considere 0.
         */
        public double speedMinValue = 0.1;
        /**
         * Default maximum speed for PhysicEngine, fixing upper threshold.
         */
        public double speedMaxValue = 4.0;
        /**
         * Default minimum acceleration for PhysicEngine. under this value, considere 0.
         */
        public double accMinValue = 0.1;
        /**
         * Default maximum acceleration for PhysicEngine, fixing upper threshold.
         */
        public double accMaxValue = 0.35;

        /**
         * Default minimum speed for CollisionDetector. under this value, considere 0.
         */
        public double colSpeedMinValue = 0.1;
        /**
         * Default maximum speed for CollisionDetector, fixing upper threshold.
         */
        public double colSpeedMaxValue = 2.0;

        /**
         * The default Scenes list.
         * format "[code1]:[path_to_class1];[code1]:[path_to_class1];"
         */
        public String scenes;
        /**
         * Default scene to be activated at start e.g.: 'code1'.
         */
        public String defaultScene;

        /**
         * Default language to be activated at start (e.g. en_EN).
         *
         * @see I18n
         */
        public String defaultLanguage;


        /**
         * Initialize configuration with the filename properties file.
         *
         * @param fileName the path and name of the properties file to be loaded.
         */
        public Configuration(String fileName) {
            try {
                InputStream is = this.getClass().getResourceAsStream(fileName);
                appProps.load(is);
                loadConfig();
            } catch (Exception e) {
                System.err.printf("ERR: Unable to read the configuration file %s : %s\n", fileName, e.getLocalizedMessage());
            }
        }

        /**
         * Map Properties attributes values to Configuration attributes.
         */
        private void loadConfig() {
            screenWidth = parseDouble(appProps.getProperty("app.screen.width", "320.0"));
            screenHeight = parseDouble(appProps.getProperty("app.screen.height", "200.0"));
            displayScale = parseDouble(appProps.getProperty("app.screen.scale", "2.0"));
            numberOfBuffer = parseInt(appProps.getProperty("app.render.buffers", "2"));

            worldWidth = parseDouble(appProps.getProperty("app.world.area.width", "640.0"));
            worldHeight = parseDouble(appProps.getProperty("app.world.area.height", "400.0"));
            worldGravity = parseDouble(appProps.getProperty("app.world.gravity", "400.0"));

            speedMinValue = parseDouble(appProps.getProperty("app.physic.speed.min", "0.1"));
            speedMaxValue = parseDouble(appProps.getProperty("app.physic.speed.max", "8.0"));
            accMinValue = parseDouble(appProps.getProperty("app.physic.acceleration.min", "0.01"));
            accMaxValue = parseDouble(appProps.getProperty("app.physic.acceleration.max", "3.0"));

            colSpeedMinValue = parseDouble(appProps.getProperty("app.collision.speed.min", "0.1"));
            colSpeedMaxValue = parseDouble(appProps.getProperty("app.collision.speed.max", "8.0"));

            fps = parseInt(appProps.getProperty("app.screen.fps", "" + FPS_DEFAULT));
            frameTime = (long) (1000 / fps);
            debug = parseInt(appProps.getProperty("app.debug.level", "0"));
            convertStringToBoolean(appProps.getProperty("app.window.mode.fullscreen", "false"));

            scenes = appProps.getProperty("app.scene.list");
            defaultScene = appProps.getProperty("app.scene.default");

            defaultLanguage = appProps.getProperty("app.language.default", "en_EN");
        }

        /**
         * Parse a String value to a Double one.
         *
         * @param stringValue the string value to be converted to double.
         * @return
         */
        private double parseDouble(String stringValue) {
            return Double.parseDouble(stringValue);
        }

        /**
         * Part s String value to an Integer value.
         *
         * @param stringValue the string value to be converted to int.
         * @return
         */
        private int parseInt(String stringValue) {
            return Integer.parseInt(stringValue);
        }

        /**
         * Parse a list of arguments (typically produced from command line interface) and extract arguments values
         * to configuration attributes values.
         *
         * @param args the main arguments list
         * @return the updated Configuration object.
         */
        private Configuration parseArgs(String[] args) {
            // args not null and not empty ? parse it !
            if (Optional.ofNullable((args)).isPresent() && args.length > 0) {
                Arrays.asList(args).forEach(arg -> {
                    String[] argSplit = arg.split("=");
                    System.out.println("- arg:" + argSplit[0] + "=" + argSplit[1]);
                    switch (argSplit[0].toLowerCase()) {
                        case "w", "width" -> screenWidth = parseDouble(argSplit[1]);
                        case "h", "height" -> screenHeight = parseDouble(argSplit[1]);
                        case "s", "scale" -> displayScale = parseDouble(argSplit[1]);
                        case "b", "buffers" -> numberOfBuffer = parseInt(argSplit[1]);
                        case "d", "debug" -> debug = parseInt(argSplit[1]);
                        case "ww", "worldwidth" -> worldWidth = parseDouble(argSplit[1]);
                        case "wh", "worldheight" -> worldHeight = parseDouble(argSplit[1]);
                        case "wg", "worldgravity" -> worldGravity = parseDouble(argSplit[1]);
                        case "spmin" -> speedMinValue = parseDouble(argSplit[1]);
                        case "spmax" -> speedMaxValue = parseDouble(argSplit[1]);
                        case "accmin" -> accMinValue = parseDouble(argSplit[1]);
                        case "accmax" -> accMaxValue = parseDouble(argSplit[1]);
                        case "cspmin" -> colSpeedMinValue = parseDouble(argSplit[1]);
                        case "cspmax" -> colSpeedMaxValue = parseDouble(argSplit[1]);
                        case "fps" -> fps = parseDouble(argSplit[1]);
                        case "f", "fullScreen" -> convertStringToBoolean(argSplit[1]);
                        case "scene" -> defaultScene = argSplit[1];
                        case "l", "language", "lang" -> defaultLanguage = argSplit[1];
                        default -> System.out.printf("\nERR : Unknown argument %s\n", arg);
                    }
                });
            }
            return this;
        }

        /**
         * Convert a String value to a boolean value. Will transform "ON", "on", "true", "TRUE", "1" or "True" to a true bollean value.
         *
         * @param value the String value to be converted to boolean.
         */
        private void convertStringToBoolean(String value) {
            fullScreen = "on|ON|true|True|TRUE|1".contains(value);
        }

    }

    /**
     * The {@link Render} service will provide the drawing process to  display entities to the {@link Application}
     * display buffer,  and then copy the buffer to the application window (see {@link JFrame}.
     */
    public static class Render {
        /**
         * A reference to the Application configuration.
         */
        private final Configuration config;
        /**
         * The World object defining the play area limit.
         */
        private final World world;
        /**
         * The Parent App.
         */
        Application app;
        /**
         * The internal rendering graphics buffer.
         */
        BufferedImage buffer;
        /**
         * The debug font to be used to display debug level information.
         */
        private Font debugFont;
        /**
         * Intrenal metric to measure rendering time.
         */
        long renderingTime = 0;
        /**
         * The list of object to be rendered: the rendering pipeline.
         */
        private List<Entity> gPipeline = new CopyOnWriteArrayList<>();
        /**
         * The current active camera to draw all the scene entities from this point of view.
         */
        Camera activeCamera;
        /**
         * Internal Counter for screenshots
         */
        private static int screenShotIndex;

        /**
         * Initialize the Render service with the parent Application, the current Configuration, and its World object.
         *
         * @param a the parent Application
         * @param c the Configuration object for this instance.
         * @param w the World object defining the play area limits.
         */
        public Render(Application a, Configuration c, World w) {
            this.app = a;
            this.config = c;
            this.world = w;
            buffer = new BufferedImage((int) config.screenWidth, (int) config.screenHeight,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = buffer.createGraphics();
            try {
                debugFont = Font.createFont(
                                Font.PLAIN,
                                Objects.requireNonNull(this.getClass().getResourceAsStream("/fonts/FreePixel.ttf")))
                        .deriveFont(9.0f);
            } catch (FontFormatException | IOException e) {
                System.out.println("ERR: Unable to initialize Render: " + e.getLocalizedMessage());
            }
        }

        /**
         * Drawing all object in the {@link Render#gPipeline}, according to the priority
         * sort order.
         *
         * @param realFps the real measured Frame Per Second value to be displayed in
         *                debug mode (if required).
         */
        public void draw(long realFps) {
            long startTime = System.nanoTime();
            Graphics2D g = buffer.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, (int) config.screenWidth, (int) config.screenHeight);
            moveCamera(g, activeCamera, -1);
            drawGrid(g, world, 16, 16);
            moveCamera(g, activeCamera, 1);
            gPipeline.stream().filter(e -> !(e instanceof Light) && e.isAlive() || e.isPersistent())
                    .forEach(e -> {
                        if (e.isNotStickToCamera()) {
                            moveCamera(g, activeCamera, -1);
                        }
                        g.setColor(e.color);
                        switch (e) {
                            // This is a TextEntity
                            case TextEntity te -> {
                                drawText(g, e, te);
                            }
                            // This is a GaugeEntity
                            case GaugeEntity ge -> {
                                drawGauge(g, ge);
                            }
                            // This is a ValueEntity
                            case ValueEntity se -> {
                                drawValue(g, se);
                            }
                            case MapEntity me -> {
                                drawMapEntity(g, me);
                            }
                            // This is a basic entity
                            case Entity ee -> {
                                drawEntity(g, ee);
                            }
                        }
                        drawDebugInfo(g, e);
                        if (e.isNotStickToCamera()) {
                            moveCamera(g, activeCamera, 1);
                        }
                    });
            // Draw all lights
            gPipeline.stream().filter(e -> e instanceof Light).forEach(l -> {
                if (l.isNotStickToCamera()) {
                    moveCamera(g, activeCamera, -1);
                }
                drawLight(g, (Light) l);
                if (l.isNotStickToCamera()) {
                    moveCamera(g, activeCamera, 1);
                }
            });
            g.dispose();
            renderToScreen(realFps);
            renderingTime = System.nanoTime() - startTime;
        }

        private void drawLight(Graphics2D g, Light l) {
            switch (l.lightType) {
                case SPOT -> drawSpotLight(g, l);
                case SPHERICAL -> drawSphericalLight(g, l);
                case AMBIENT -> drawAmbiantLight(g, l);
                case AREA_RECTANGLE -> drawLightArea(g, l);
            }
        }

        private void drawLightArea(Graphics2D g, Light l) {

            Camera cam = app.render.activeCamera;
            Configuration conf = app.config;

            final Area ambientArea = new Area(new Rectangle2D.Double(l.pos.x, l.pos.y, l.width, l.height));
            g.setColor(l.color);
            Composite c = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) l.energy));
            g.fill(ambientArea);
            g.setComposite(c);
        }

        private void drawAmbiantLight(Graphics2D g, Light l) {
            Camera cam = app.render.activeCamera;
            Configuration conf = app.config;

            final Area ambientArea = new Area(new Rectangle2D.Double(cam.pos.x, cam.pos.y, conf.screenWidth, conf.screenHeight));
            g.setColor(l.color);
            Composite c = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) l.energy));
            g.fill(ambientArea);
            g.setComposite(c);
        }

        private void drawSphericalLight(Graphics2D g, Light l) {
            l.color = brighten(l.color, l.energy);
            Color medColor = brighten(l.color, l.energy * 0.5);
            Color endColor = new Color(0.0f, 0.0f, 0.0f, 0.2f);

            l.colors = new Color[]{l.color,
                    medColor,
                    endColor};
            l.dist = new float[]{0.0f, 0.05f, 0.5f};
            l.rgp = new RadialGradientPaint(
                    new Point(
                            (int) (l.pos.x + (l.width * 0.5) + (10 * Math.random() * l.glitterEffect)),
                            (int) (l.pos.y + (l.width * 0.5) + (10 * Math.random() * l.glitterEffect))),
                    (int) (l.width),
                    l.dist,
                    l.colors);
            g.setPaint(l.rgp);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) l.energy));
            g.fill(new Ellipse2D.Double(l.pos.x, l.pos.y, l.width, l.width));
        }

        private void drawSpotLight(Graphics2D g, Light l) {

        }

        /**
         * Make a color brighten.
         *
         * @param color    Color to make brighten.
         * @param fraction Darkness fraction.
         * @return Lighter color.
         * @link https://stackoverflow.com/questions/18648142/creating-brighter-color-java
         */
        public static Color brighten(Color color, double fraction) {

            int red = (int) Math.round(Math.min(255, color.getRed() + 255 * fraction));
            int green = (int) Math.round(Math.min(255, color.getGreen() + 255 * fraction));
            int blue = (int) Math.round(Math.min(255, color.getBlue() + 255 * fraction));

            int alpha = color.getAlpha();

            return new Color(red, green, blue, alpha);

        }

        private void drawMapEntity(Graphics2D g, MapEntity me) {
            g.setColor(me.color);
            g.drawRect((int) me.pos.x, (int) me.pos.y, (int) me.width, (int) me.height);
            g.setColor(me.backgroundColor);
            g.fillRect((int) me.pos.x, (int) me.pos.y, (int) me.width, (int) me.height);
            me.entitiesRef.stream()
                    .filter(e -> (e.isAlive() || e.isPersistent()))
                    .forEach(e -> {
                        me.colorEntityMapping.entrySet().forEach(cm -> {
                            if (e.name.contains(cm.getKey())) {
                                g.setColor(cm.getValue());
                                int px = (int) (me.pos.x + (me.width * (e.pos.x / me.world.area.getWidth())));
                                int py = (int) (me.pos.y + me.height * (e.pos.y / me.world.area.getHeight()));
                                int pw = (int) (me.width * (e.width / me.world.area.getWidth()));
                                int ph = (int) (me.height * (e.height / me.world.area.getHeight()));
                                g.drawRect(px, py, pw, ph);
                            }
                        });
                    });
        }

        private void drawEntity(Graphics2D g, Entity ee) {
            switch (ee.type) {
                case RECTANGLE -> g.fillRect((int) ee.pos.x, (int) ee.pos.y, (int) ee.width, (int) ee.height);
                case ELLIPSE -> g.fillArc((int) ee.pos.x, (int) ee.pos.y, (int) ee.width, (int) ee.height, 0, 360);
                case IMAGE -> {
                    if (ee.getDirection() > 0) {
                        g.drawImage(
                                ee.getImage(),
                                (int) ee.pos.x, (int) ee.pos.y,
                                null);
                    } else {
                        g.drawImage(
                                ee.getImage(),
                                (int) (ee.pos.x + ee.width), (int) ee.pos.y,
                                (int) (-ee.width), (int) ee.height,
                                null);
                    }
                }
                case NONE -> {
                }
            }
        }

        public BufferedImage setAlpha(BufferedImage sprite, float alpha) {

            BufferedImage drawImage = new BufferedImage(
                    sprite.getWidth(), sprite.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            float[] scales = {1f, 1f, 1f, alpha};
            float[] offsets = {0f, 0f, 0f, 0f};

            RescaleOp rop = new RescaleOp(scales, offsets, null);
            rop.filter(sprite, drawImage);

            return drawImage;
        }

        private void drawText(Graphics2D g, Entity e, TextEntity te) {
            g.setFont(te.font);
            int size = g.getFontMetrics().stringWidth(te.text);
            double offsetX = te.align.equals(TextAlign.RIGHT) ? -size
                    : te.align.equals(TextAlign.CENTER) ? -size * 0.5 : 0;
            g.drawString(te.text, (int) (te.pos.x + offsetX), (int) te.pos.y);
            e.width = size;
            e.height = g.getFontMetrics().getHeight();
            e.box.setRect(e.pos.x + offsetX, e.pos.y - e.height + g.getFontMetrics().getDescent(), e.width,
                    e.height);
        }

        private void drawGauge(Graphics2D g, GaugeEntity ge) {
            g.setColor(ge.shadow);
            g.fillRect((int) ge.pos.x - 1, (int) ge.pos.y - 1, (int) ge.width + 2, (int) ge.height + 2);
            g.setColor(ge.border);
            g.fillRect((int) ge.pos.x, (int) ge.pos.y, (int) ge.width, (int) ge.height);
            int value = (int) ((ge.value / ge.maxValue) * ge.width - 2);
            g.setColor(ge.color);
            g.fillRect((int) (ge.pos.x) + 1, (int) (ge.pos.y) + 1, value, (int) (ge.height) - 2);
        }

        /**
         * Display debug information on the {@link Entity } according to the current
         * Application level of debug.
         *
         * @param g Graphics API to use to draw things !
         * @param e the Entity to be displayed info to.
         */
        private void drawDebugInfo(Graphics2D g, Entity e) {

            if (config.debug > 0) {
                // display bounding box
                if (Optional.ofNullable(e.box).isPresent()) {
                    // collision box
                    g.setColor(new Color(1.0f, 0.5f, 0.1f, 0.8f));
                    g.draw(e.box);
                    //initial coordinate
                    g.setColor(Color.WHITE);
                    g.drawRect((int) e.pos.x, (int) e.pos.y, 1, 1);
                }
                // display id
                g.setFont(debugFont);
                int lineHeight = g.getFontMetrics().getHeight();// + g.getFontMetrics().getDescent();
                g.setColor(Color.ORANGE);
                int offsetX = (int) (e.pos.x + e.width + 4);
                int offsetY = (int) (e.pos.y - 8);
                g.drawString(String.format("#%d", e.id), (int) e.pos.x, offsetY);
                // display LifeBar
                if (e.isAlive()) {
                    drawLifeBar(g, e);
                }
                if (config.debug > 1) {
                    // display colliding box
                    g.setColor(e.collide ? new Color(1.0f, 0.0f, 0.0f, 0.7f) : new Color(0.0f, 0.0f, 1.0f, 0.3f));
                    g.fill(e.cbox);
                    if (config.debug > 2) {
                        // display 2D parameters
                        g.setColor(Color.ORANGE);
                        g.drawString(String.format(Locale.ROOT, "name:%s", e.name), offsetX, offsetY + lineHeight);
                        g.drawString(String.format(Locale.ROOT, "pos:%03.0f,%03.0f", e.pos.x, e.pos.y), offsetX, offsetY + (lineHeight * 2));
                        g.drawString(String.format("life:%d", e.duration), offsetX, offsetY + (lineHeight * 3));
                        if (config.debug > 3) {
                            // display Physic parameters
                            g.drawString(String.format(Locale.ROOT, "spd:%03.2f,%03.2f", e.vel.x, e.vel.y), offsetX,
                                    offsetY + (lineHeight * 4));
                            g.drawString(String.format(Locale.ROOT, "acc:%03.2f,%03.2f", e.acc.x, e.acc.y), offsetX,
                                    offsetY + (lineHeight * 5));
                            if (e.getAnimations()) {
                                g.drawString(String.format("anim:%s/%d",
                                                e.animations.currentAnimationSet,
                                                e.animations.currentFrame),
                                        offsetX, offsetY + (lineHeight * 6));
                            }
                        }
                    }
                }
            }
        }

        private void drawLifeBar(Graphics2D g, Entity e) {
            g.setColor(Color.RED);
            float ratio = 0.0f;
            if (e.isPersistent()) {
                g.setColor(Color.ORANGE);
                ratio = 1.0f;
            } else {
                ratio = (1.0f * e.duration) / (1.0f * e.startDuration);
            }
            g.fillRect((int) e.pos.x, (int) e.pos.y - 4, (int) (32.0 * ratio), 2);
        }

        /**
         * Draw score with digital characters
         *
         * @param g  the Graphics2D API
         * @param se ValueEntity object
         */
        private void drawValue(Graphics2D g, ValueEntity se) {
            String textValue = se.valueTxt.strip();
            byte c[] = textValue.getBytes(StandardCharsets.US_ASCII);
            for (int pos = 0; pos < textValue.length(); pos++) {
                //convert character ascii value to number from 0 to 9.
                int v = c[pos] - 48;
                drawFig(g, se, v, se.pos.x + (pos * 8), se.pos.y);
            }
        }

        /**
         * Draw a simple figure
         *
         * @param g     the Graphics2D API
         * @param value number value to draw
         * @param x     horizontal position
         * @param y     vertical position
         */
        private void drawFig(Graphics2D g, ValueEntity se, int value, double x, double y) {
            assert (value > -1);
            assert (value < 10);
            g.drawImage(se.figures[value], (int) x, (int) y, null);
        }

        /**
         * Draw a global grid corresponding to the World object defining the play area.
         *
         * @param g     Graphics API to use to draw things !
         * @param world the World object to be used as reference to build grid and debug
         *              info.
         * @param tw    width of a grid cell.
         * @param th    height of a grid cell.
         */
        private void drawGrid(Graphics2D g, World world, double tw, double th) {
            g.setColor(Color.BLUE);
            for (double tx = 0; tx < world.area.getWidth(); tx += tw) {
                for (double ty = 0; ty < world.area.getHeight(); ty += th) {
                    double rh = th;
                    if (ty + th > world.area.getHeight()) {
                        rh = world.area.getHeight() % th;
                    }
                    g.drawRect((int) tx, (int) ty, (int) tw, (int) rh);
                }
            }
            g.setColor(Color.DARK_GRAY);
            g.drawRect(0, 0, (int) world.area.getWidth(), (int) world.area.getHeight());
        }

        /**
         * After the Buffer rendering operation performed in {@link Render#draw(long)},
         * the buffer is coped to the JFrame content.
         *
         * @param realFps the measured frame rate per seconds
         */
        public void renderToScreen(long realFps) {

            Graphics2D g2 = (Graphics2D) app.frame.getBufferStrategy().getDrawGraphics();
            g2.drawImage(
                    buffer,
                    0, 0, (int) app.frame.getWidth(), (int) app.frame.getHeight(),
                    0, 0, (int) config.screenWidth, (int) config.screenHeight,
                    null);
            drawDebugString(g2, realFps);
            g2.dispose();
            app.frame.getBufferStrategy().show();
        }

        public void drawDebugString(Graphics2D g, double realFps) {
            if (config.debug > 0) {
                g.setFont(debugFont.deriveFont(16.0f));
                g.setColor(Color.WHITE);
                g.drawString(String.format("[ dbg: %d | fps:%3.0f | obj:%d | {g:%1.03f, a(%3.0fx%3.0f) }]",
                                config.debug,
                                realFps,
                                gPipeline.size(),
                                world.gravity * 1000.0,
                                world.area.getWidth(), world.area.getHeight()),

                        20, (int) app.getHeight() - 20);
            }

        }

        /**
         * Move rendering point of view to Camera cam, with a direction -1 move to camera, 1 move back from camera.
         *
         * @param g         the Graphics API device.
         * @param cam       the camera to move to/from.
         * @param direction the direction of the move : -1 move to camera, 1 move back from camera.
         */
        private void moveCamera(Graphics2D g, Camera cam, double direction) {
            if (Optional.ofNullable(activeCamera).isPresent()) {
                g.translate(cam.pos.x * direction, cam.pos.y * direction);
            }
        }

        /**
         * Add an entity to the rendering pipeline.
         *
         * @param entity te Entity to be added to the rendering process.
         */
        public void addToPipeline(Entity entity) {
            if (!gPipeline.contains(entity)) {
                gPipeline.add(entity);
                gPipeline.sort((o1, o2) -> o1.priority < o2.priority ? -1 : 1);
            }
        }

        /**
         * Define the active Camera.
         *
         * @param cam A Camera object to be activated as the Rendering point of view.
         */
        public void addCamera(Camera cam) {
            this.activeCamera = cam;
        }

        /**
         * Clear the current rendering pipeline.
         */
        public void clear() {
            gPipeline.clear();
        }

        /**
         * Free all resources before closing the service.
         */
        public void dispose() {
            clear();
            buffer = null;
        }

        /**
         * Remove an entity from the rendering pipeline.
         *
         * @param e the Entity to be removed from the pipeline.
         */
        public void remove(Entity e) {
            gPipeline.remove(e);
        }

        /**
         * Write out to the class root path ./screenshots directory a new screenshot from the current displayed buffer.
         */
        public void saveScreenshot() {
            String path = Utils.getJarPath();
            Path targetDir = Paths.get(path + "/screenshots");
            int i = screenShotIndex++;
            String filename = String.format("%s/screenshots/%s-%d.png", path, java.lang.System.nanoTime(), i);

            try {
                if (!Files.exists(targetDir)) {
                    Files.createDirectory(targetDir);
                }
                File out = new File(filename);
                ImageIO.write(buffer, "PNG", out);

                System.out.printf("INFO: Write screenshot to %s\n", filename);
            } catch (IOException e) {
                System.out.printf("Unable to write screenshot to %s: %s", filename, e.getMessage());
            }
        }
    }

    /**
     * {@link Utils} is an utilities class to provide some basic operations
     *
     * @author Frédéric Delorme
     * @since 1.0.4
     */
    public static class Utils {

        /**
         * toolbox to define and fix ceil value
         *
         * @param x    the value to "ceilled"
         * @param ceil the level of ceil to apply to x value.
         * @return value with ceil applied.
         */
        public static double ceilValue(double x, double ceil) {
            return Math.copySign((Math.abs(x) < ceil ? 0 : x), x);
        }

        /**
         * min-max-range to apply to a x value.
         *
         * @param x   the value to be constrained between min and max.
         * @param min minimum for the x value.
         * @param max maximum for the x value.
         * @return
         */
        public static double ceilMinMaxValue(double x, double min, double max) {
            return ceilValue(Math.copySign((Math.abs(x) > max ? max : x), x), min);
        }

        /**
         * Retrieve the root path for the current .class or .jar.
         *
         * @return a String path for the current .class or JAR file.
         */
        public static String getJarPath() {
            String jarDir = null;
            CodeSource codeSource = Application.class.getProtectionDomain().getCodeSource();
            try {
                File jarFile = new File(codeSource.getLocation().toURI().getPath());
                jarDir = jarFile.getParentFile().getPath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return jarDir;
        }
    }

    /**
     * A Physic computation engine to process Object moves according to their resulting process acceleration and speed
     * from the applied forces to each Entity.
     *
     * @Author Frédéric Delorme
     * @since 1.0.2
     */
    public static class PhysicEngine {
        private final Application app;
        private final World world;
        private final Configuration config;
        public long updateTime;
        private Map<String, Influencer> influencers = new ConcurrentHashMap<>();

        /**
         * Initialize the Physic Engine for the parent Application a, with the Configuration c
         * and in a World w.
         *
         * @param a the parent Application
         * @param c the Configuration where to find Physic Engine initialization parameters
         * @param w the World where the Entities will evolve in.
         */
        public PhysicEngine(Application a, Configuration c, World w) {
            this.app = a;
            this.config = c;
            this.world = w;
        }

        /**
         * Update and process physics attributes the Entity (acceleration, speed and position) from the
         * <code>app.entities map</code>, with  the elapsed time since previous call.
         *
         * @param elapsed
         */
        public synchronized void update(double elapsed) {
            long start = System.nanoTime();

            // update entities
            app.entities.values().forEach((e) -> {
                if (e.physicType.equals(PhysicType.DYNAMIC)) {
                    updateEntity(e, elapsed);
                }
                e.update(elapsed);

                // TODO update Entity Behavior
                e.behaviors.values().stream()
                        .filter(b -> b.filterOnEvent()
                        .contains(Behavior.updateEntity))
                        .toList()
                        .forEach(b -> b.update(app, e, elapsed));
            });

            // TODO update Scene Behaviors
            if (Optional.ofNullable(app.activeScene.getBehaviors()).isPresent()) {
                app.activeScene.getBehaviors().values().stream()
                        .filter(b -> b.filterOnEvent().contains(Behavior.updateScene))
                        .toList()
                        .forEach(b -> b.update(app, elapsed));
            }
            //  update active camera if presents.
            if (Optional.ofNullable(app.render.activeCamera).isPresent()) {
                app.render.activeCamera.update(elapsed);
            }
            updateTime = System.nanoTime() - start;
        }

        /**
         * Update one Entity
         *
         * @param e       The Entity to be updated.
         * @param elapsed the elapsed time since previous call.
         */
        private void updateEntity(Entity e, double elapsed) {
            applyWorldInfluencers(e);
            applyPhysicRuleToEntity(e, elapsed);
            constrainsEntity(e);
        }

        private void applyWorldInfluencers(Entity e) {
            getInfluencers().values()
                    .stream()
                    .filter(i -> i.box.contains(e.box))
                    .forEach(i2 -> {
                        if (Optional.ofNullable(i2.getGravtity()).isPresent()) {
                            e.forces.add(i2.getGravtity());
                        }
                        if (Optional.ofNullable(i2.getForce()).isPresent()) {
                            e.forces.add(i2.getForce());
                        }
                    });
        }

        /**
         * Apply Physic computation on the Entity e for the elapsed time.
         *
         * @param e       the Entity to compute Physic for.
         * @param elapsed the elapsed tile since previous call.
         */
        private void applyPhysicRuleToEntity(Entity e, double elapsed) {
            e.oldPos.x = e.pos.x;
            e.oldPos.y = e.pos.y;

            // a small reduction of time
            elapsed *= 0.4;

            e.forces.add(new Vec2d(0, e.mass * -world.gravity));

            e.acc = new Vec2d(0.0, 0.0);
            e.acc.add(e.forces);

            e.vel.add(e.acc.minMax(config.accMinValue, config.accMaxValue).multiply(0.5 * elapsed * e.friction * world.friction));
            e.vel.minMax(config.speedMinValue, config.speedMaxValue);

            e.pos.add(e.vel);

            e.forces.clear();
        }

        /**
         * Apply a lower threshold on the double value
         *
         * @param value          the value to be zero-ified
         * @param valueThreshold the Threshold value below we consider value as 0.
         * @return the threshold value.
         */
        private double threshold(double value, double valueThreshold) {
            return (value < valueThreshold) ? 0.0 : value;
        }

        /**
         * Apply the constraints to the Entity.
         *
         * @param e the Entity to be constrained.
         */
        private void constrainsEntity(Entity e) {
            if (e.isAlive() || e.isPersistent()) {
                constrainToWorld(e, world);
            }
        }

        /**
         * Apply the World limitations to the Entity.
         *
         * @param e the Entity to be world constrained.
         */
        private void constrainToWorld(Entity e, World world) {
            if (e.cbox.getBounds().getX() < 0.0) {
                e.pos.x = 0.0;
                e.vel.x *= -1 * e.elasticity;
                e.acc.x = 0.0;
            }
            if (e.cbox.getBounds().getY() < 0.0) {
                e.pos.y = 0.0;
                e.vel.y *= -1 * e.elasticity;
                e.acc.y = 0.0;
            }
            if (e.cbox.getBounds().getX() + e.cbox.getBounds().getWidth() > world.area.getWidth()) {
                e.pos.x = world.area.getWidth() - e.width;
                e.vel.x *= -1 * e.elasticity;
                e.acc.x = 0.0;
            }
            if (e.cbox.getBounds().getY() + e.cbox.getBounds().getHeight() > world.area.getHeight()) {
                e.pos.y = world.area.getHeight() - e.height;
                e.vel.x *= -1 * e.elasticity;
                e.acc.x = 0.0;
            }
        }

        public void dispose() {

        }

        public Map<String, Influencer> getInfluencers() {
            return influencers;
        }

        public PhysicEngine addInfluencer(Influencer i) {
            influencers.put(i.name, i);
            return this;
        }
    }

    /**
     * The {@link World} object to define game play area limits and a default gravity and friction.
     *
     * <blockquote>May more to comes in the next release with some <code>Influencers</code> to
     * dynamically modify entity display or physic attributes</blockquote>
     */
    public static class World {
        /**
         * {@link World} friction factor applied to ALL entities.
         */
        public double friction = 1.0;
        /**
         * Area for this {@link World} object.
         */
        public Rectangle2D area;
        /**
         * THe World default gravity is set to the Earth gravity value. it can be changed for your own usage.
         */
        public double gravity = 0.981;
        /**
         * The map of World {@link Influencer} to be applied to any {@link Entity} moving in this {@link World}.
         */
        private Map<String, Influencer> influencers = new ConcurrentHashMap<>();

        /**
         * Initialize the world with some default values with an area of 320.0 x 200.0.
         */
        public World() {
            area = new Rectangle2D.Double(0.0, 0.0, 320.0, 200.0);
        }

        /**
         * You can set your own {@link World} area dimension of width x height.
         *
         * @param width  the area width for this new {@link World}
         * @param height the area Height for this new {@link World}.
         * @return a World with ots new area of width x height.
         */
        public World setArea(double width, double height) {
            area = new Rectangle2D.Double(0.0, 0.0, width, height);
            return this;
        }

        /**
         * Yot can also set the gravity for your {@link World}.
         *
         * @param g the new gravity for this World to be applied to all {@link Entity} in this {@link World}.
         * @return the world updated with its new gravity.
         */
        public World setGravity(double g) {
            this.gravity = g;
            return this;
        }

        /**
         * The {@link World} default friction can be changed to a new <code>f</code> value.
         *
         * @param f the value for the new friction applied to all {@link Entity} evolving in this {@link World}.
         * @return the World updated with its new friction factor.
         */
        public World setFriction(double f) {
            this.friction = f;
            return this;
        }

        /**
         * add an {@link Influencer} in the game {@link World}.
         *
         * @param i the new {@link Influencer} to be added to the {@link World} environment.
         */
        public void addInfluencer(Influencer i) {
            influencers.put(i.name, i);
        }

        /**
         * Retrieve all {@link Influencer} for this {@link World}.
         *
         * @return a Collection of {@link Influencer}.
         */
        public Collection<Influencer> getInfluencers() {
            return influencers.values();
        }
    }

    /**
     * The {@link Influencer} extending {@link Entity} to provide environmental influencer to change {@link Entity}
     * behavior has soon the Entity is contained by the {@link Influencer}.
     * An influencer can change temporarily some {@link Entity} attribute's values.
     *
     * @author Frédéric Delorme
     * @since 1.0.5
     */
    public static class Influencer extends Entity {

        public Influencer(String name) {
            super(name);
            addBehavior(new Behavior() {
                @Override
                public String filterOnEvent() {
                    return Behavior.onCollision;
                }

                @Override
                public void update(Application a, Entity e, double elapsed) {

                }

                @Override
                public void update(Application a, double elapsed) {

                }

                @Override
                public void onCollide(Application a, Entity e1, Entity e2) {
                    Influencer i1 = (Influencer) e1;
                    e2.forces.add(i1.getForce());
                }
            });
        }

        /**
         * Define the {@link Influencer}'s gravity attribute, 0 means World's default gravity.
         *
         * @param g the new gravity for this {@link Influencer} zone
         * @return the updated Influencer with its new gravity.
         */
        public Influencer setGravity(Vec2d g) {
            this.attributes.put("gravity", g);
            return this;
        }

        /**
         * Define the {@link Influencer}'s attribute force to be applied to any {@link Entity}
         * contained by this {@link Influencer}..
         *
         * @param f the force to be applied in this {@link Influencer} zone.
         * @return the updated {@link Influencer} with its new force.
         */
        public Influencer setForce(Vec2d f) {
            this.attributes.put("force", f);
            return this;
        }

        /**
         * Define the {@link Influencer}'s attribute elasticity to be applied to any {@link Entity}
         * contained by this {@link Influencer}..
         *
         * @param e the elasticity to be applied in this {@link Influencer} zone.
         * @return the updated {@link Influencer} with its new elasticity.
         */
        public Influencer setElasticity(double e) {
            this.attributes.put("elasticity", e);
            return this;
        }

        /**
         * Define the {@link Influencer}'s attribute friction to be applied to any {@link Entity}
         * contained by this {@link Influencer}..
         *
         * @param f the friction to be applied in this {@link Influencer} zone.
         * @return the updated {@link Influencer} with its new friction.
         */
        public Influencer setFriction(double f) {
            this.attributes.put("friction", f);
            return this;
        }

        /**
         * retrieve the current gravity attribute value for this {@link Influencer}
         *
         * @return value for this Influencer's gravity.
         */
        public Vec2d getGravtity() {
            return (Vec2d) this.attributes.get("gravity");
        }

        /**
         * retrieve the current force attribute value for this {@link Influencer}
         *
         * @return value for this Influencer's force.
         */
        public Vec2d getForce() {
            return (Vec2d) this.attributes.get("force");
        }

        /**
         * retrieve the current elasticity attribute value for this {@link Influencer}
         *
         * @return value for this Influencer's elasticity.
         */
        public double getElasticity() {
            return (double) this.attributes.get("elasticity");
        }

        /**
         * retrieve the current friction attribute value for this {@link Influencer}
         *
         * @return value for this Influencer's friction.
         */
        public double getFriction() {
            return (double) this.attributes.get("friction");
        }

    }

    /**
     * Collision Detector Service.
     *
     * @author Frédéric Delorme
     * @since 1.0.3
     */
    public static class CollisionDetector {
        private final Configuration config;
        private final Application app;
        private final World world;

        // ToDo! maintain a binTree to 'sub-space' world.
        public Map<String, Entity> colliders = new ConcurrentHashMap<>();

        public CollisionDetector(Application a, Configuration c, World w) {
            this.config = c;
            this.app = a;
            this.world = w;
        }

        public void add(Entity e) {
            colliders.put(e.name, e);
        }

        public void update(double elapsed) {
            detect();
        }

        private void detect() {
            List<Entity> targets = colliders.values().stream().filter(e -> e.isAlive() || e.isPersistent()).toList();
            for (Entity e1 : colliders.values()) {
                e1.collide = false;
                for (Entity e2 : targets) {
                    e2.collide = false;
                    if (e1.id != e2.id && e1.cbox.getBounds().intersects(e2.cbox.getBounds())) {
                        resolve(e1, e2);
                        e1.behaviors.values().stream()
                                .filter(b -> b.filterOnEvent().equals(Behavior.onCollision)).toList()
                                .forEach(b -> b.onCollide(app, e1, e2));
                    }
                }
            }
        }

        /**
         * Collision response largely inspired by the article from
         * https://spicyyoghurt.com/tutorials/html5-javascript-game-development/collision-detection-physics
         *
         * @param e1 first Entity in the collision
         * @param e2 second Entity in the collision
         */
        private void resolve(Entity e1, Entity e2) {
            e1.collide = true;
            e2.collide = true;
            Vec2d vp = new Vec2d((e2.pos.x - e1.pos.x), (e2.pos.y - e1.pos.y));
            double distance = Math.sqrt((e2.pos.x - e1.pos.x) * (e2.pos.x - e1.pos.x) + (e2.pos.y - e1.pos.y) * (e2.pos.y - e1.pos.y));
            Vec2d colNorm = new Vec2d(vp.x / distance, vp.y / distance);
            if (e1.physicType == PhysicType.DYNAMIC && e2.physicType == PhysicType.DYNAMIC) {
                Vec2d vRelSpeed = new Vec2d(e1.vel.x - e2.vel.x, e1.vel.y - e2.vel.y);
                double colSpeed = vRelSpeed.x * colNorm.x + vRelSpeed.y * colNorm.y;
                var impulse = 2 * colSpeed / (e1.mass + e2.mass);
                e1.vel.x -= Utils.ceilMinMaxValue(impulse * e2.mass * colSpeed * colNorm.x,
                        config.speedMinValue, config.colSpeedMaxValue);
                e1.vel.y -= Utils.ceilMinMaxValue(impulse * e2.mass * colSpeed * colNorm.y,
                        config.speedMinValue, config.colSpeedMaxValue);
                e2.vel.x += Utils.ceilMinMaxValue(impulse * e1.mass * colSpeed * colNorm.x,
                        config.speedMinValue, config.colSpeedMaxValue);
                e2.vel.y += Utils.ceilMinMaxValue(impulse * e1.mass * colSpeed * colNorm.y,
                        config.speedMinValue, config.colSpeedMaxValue);
                if (e1.name.equals("player") && config.debug > 4) {
                    System.out.printf("e1.%s collides e2.%s Vp=%s / dist=%f / norm=%s\n", e1.name, e2.name, vp, distance, colNorm);
                }
            } else {
                if (e1.physicType == PhysicType.DYNAMIC && e2.physicType == PhysicType.STATIC) {
                    if (e1.pos.y + e1.height > e2.pos.y && vp.y > 0) {
                        e1.pos.y = e2.pos.y - e1.height;
                        e1.vel.y = -e1.vel.y * e1.elasticity;
                    } else {
                        e1.vel.y = -e1.vel.y * e1.elasticity;
                        e1.pos.y = e2.pos.y + e2.height;
                    }
                    if (e1.name.equals("player") && config.debug > 4) {
                        System.out.printf("e1.%s collides static e2.%s\n", e1.name, e2.name);
                    }
                }
            }
        }
    }

    /**
     * A Resource manager to load and buffered all necessary resources.
     */
    public static class Resources {
        /**
         * THe internal buffer for resources
         */
        static Map<String, Object> resources = new ConcurrentHashMap<>();

        /**
         * Load an image and store it into buffer.
         *
         * @param path path of the image
         * @return the loaded image.
         */
        public static BufferedImage loadImage(String path) {
            BufferedImage img = null;
            if (resources.containsKey(path)) {
                img = (BufferedImage) resources.get(path);
            } else {
                try {
                    InputStream is = Resources.class.getResourceAsStream(path);
                    img = ImageIO.read(Objects.requireNonNull(is));
                    resources.put(path, img);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return img;
        }

        /**
         * Load a Font and store it into buffer.
         *
         * @param path path of the Font (True Type Font)
         * @return the loaded Font
         */
        public static Font loadFont(String path) {
            Font f = null;
            if (resources.containsKey(path)) {
                f = (Font) resources.get(path);
            } else {
                try {
                    f = Font.createFont(
                            Font.PLAIN,
                            Objects.requireNonNull(Resources.class.getResourceAsStream(path)));
                } catch (FontFormatException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return f;
        }

        /**
         * Free all loaded resources.
         */
        public static void dispose() {
            resources.clear();
        }
    }

    /**
     * A brand-new service to manage action on KeyEvent.
     *
     * @author Frédéric Delorme
     * @since 1.0.3
     */
    public static class ActionHandler implements KeyListener {
        private final boolean[] prevKeys = new boolean[65536];
        private final boolean[] keys = new boolean[65536];
        private final Application app;
        private boolean anyKeyPressed;

        public Map<Integer, Function> actionMapping = new HashMap<>();

        /**
         * Initialize the service with ots parent {@link Application}.
         *
         * @param a THe parent application to link the ActionHandler to.
         */
        public ActionHandler(Application a) {
            this.app = a;
            this.actionMapping.put(KeyEvent.VK_F3, (e) -> {
                app.render.saveScreenshot();
                return this;
            });
        }


        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (actionMapping.containsKey(e.getKeyCode())) {
                Function f = actionMapping.get(e.getKeyCode());
                f.apply(e);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    /**
     * A Translating service to adapt user graphics text to tits preferred language
     *
     * @author Frédéric Delorme
     * @since 1.0.2
     */
    public static class I18n {
        private static ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");

        private I18n() {
        }

        /**
         * Set the preferred language to the default one from the configuration object..
         *
         * @param config the parent configuration.
         */
        public static void setLanguage(Configuration config) {
            String[] langCountry = config.defaultLanguage.split("_");
            messages = ResourceBundle.getBundle("i18n.messages", new Locale(langCountry[0], langCountry[1]));
        }

        /**
         * Return the translated message for key.
         *
         * @param key the  key of the message to retrieved
         * @return the translated value for the key message.
         */
        public static String get(String key) {
            return messages.getString(key);
        }

        /**
         * Return the translated parametric message for key.
         *
         * @param key  the key of the message to retrieved
         * @param args the list of parameters to be applied to the translated message.
         * @return the translated value for the key message.
         */
        public static String get(String key, Object... args) {
            return String.format(messages.getString(key), args);
        }
    }

    /**
     * A 2D math Vector model class.
     *
     * @author Frédéric Delorme
     * @since 1.0.3
     */
    public static class Vec2d {
        public double x;
        public double y;

        public Vec2d() {
            this.x = 0.0;
            this.y = 0.0;
        }


        public Vec2d(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Vec2d add(Vec2d v) {
            x = x + v.x;
            y = y + v.y;
            return this;
        }

        public Vec2d add(List<Vec2d> vl) {
            for (Vec2d v : vl) {
                add(v);
            }
            return this;
        }

        public Vec2d normalize() {
            // sets length to 1
            //
            double length = Math.sqrt(x * x + y * y);

            if (length != 0.0) {
                double s = 1.0f / length;
                x = x * s;
                y = y * s;
            }

            return new Vec2d(x, y);
        }

        public Vec2d multiply(double v) {
            return new Vec2d(x * v, y * v);
        }

        public Vec2d minMax(double minVal, double maxVal) {
            this.x = Utils.ceilMinMaxValue(x, minVal, maxVal);
            this.y = Utils.ceilMinMaxValue(y, minVal, maxVal);
            return this;
        }

        public Vec2d setX(double x) {
            this.x = x;
            return this;
        }

        public Vec2d setY(double y) {
            this.y = y;
            return this;
        }

        public String toString() {
            return String.format("(%4.2f,%4.2f)", x, y);
        }

    }

    /**
     * Definition for all {@link Entity} managed by the small game framework.
     * A lot of attributes and <a href="https://en.wikipedia.org/wiki/Fluent_interface#Java">Fluent API</a> methods
     * to ease the Entity initialization.
     * <p>
     * Support some {@link PhysicEngine}, {@link Render}, {@link Animation}, and {@link CollisionDetector} information,
     * mandatory attributes for multiple services.
     *
     * @author Frédéric Delorme
     * @since 1.0.0
     */
    public static class Entity {

        public boolean collide;
        // id & naming attributes
        protected long id = entityIndex++;
        public String name = "entity_" + id;

        // Rendering attributes
        public int priority;
        protected EntityType type = RECTANGLE;
        public Image image;
        public Animation animations;
        public Color color = Color.BLUE;
        public boolean stickToCamera;

        // Position attributes
        public Rectangle2D.Double box = new Rectangle2D.Double(0, 0, 0, 0);
        public Shape offsetbox = new Rectangle2D.Double(0, 0, 0, 0);
        public Shape cbox = new Rectangle2D.Double(0, 0, 0, 0);

        public Vec2d pos = new Vec2d(0.0, 0.0);
        public Vec2d oldPos = new Vec2d(0, 0);
        public double width = 0.0, height = 0.0;

        // Physic attributes
        public List<Vec2d> forces = new ArrayList<>();
        protected PhysicType physicType = PhysicType.DYNAMIC;
        public Vec2d vel = new Vec2d(0.0, 0.0);
        public Vec2d acc = new Vec2d(0.0, 0.0);
        public double mass = 1.0;
        public double elasticity = 1.0, friction = 1.0;

        // internal attributes
        protected int startDuration = -1;
        protected int duration = -1;
        public Map<String, Object> attributes = new ConcurrentHashMap<>();

        public Map<String, Behavior> behaviors = new ConcurrentHashMap<>();
        private Color shadowColor = Color.BLACK;

        public Entity(String name) {
            this.name = name;
        }

        public Entity setPosition(double x, double y) {
            this.pos.x = x;
            this.pos.y = y;
            this.update(0);
            return this;
        }

        public Entity setSize(double w, double h) {
            this.width = w;
            this.height = h;
            box.setRect(pos.x, pos.y, w, h);
            setCollisionBox(0, 0, 0, 0);
            return this;
        }

        /**
         * The collision shape position is relative the entity position.
         * eg. Entity is 32x32, the shapebox is Ellipse2D at 16,16 and r1=r2=8.
         *
         * @param left   left offset into box
         * @param top    top offset into box
         * @param right  right offset into box
         * @param bottom bottom offset into box
         * @return the updated Entity.
         */
        public Entity setCollisionBox(double left, double top, double right, double bottom) {
            switch (type) {
                case IMAGE, RECTANGLE, default -> this.offsetbox = new Rectangle2D.Double(left, top, right, bottom);
                case ELLIPSE -> this.offsetbox = new Ellipse2D.Double(left, top, right, bottom);
            }
            update(0.0);
            return this;
        }

        public Entity setType(EntityType et) {
            this.type = et;
            return this;
        }

        public Entity setPhysicType(PhysicType t) {
            this.physicType = t;
            return this;
        }

        public synchronized Entity setDuration(int l) {
            this.duration = l;
            return this;
        }

        public synchronized Entity setInitialDuration(int l) {
            this.duration = l;
            this.startDuration = l;
            return this;
        }

        public Entity setImage(BufferedImage img) {
            this.image = img;
            return this;
        }

        public synchronized boolean isAlive() {
            if (attributes.containsKey("energy")) {
                return ((int) attributes.get("energy")) > 0;
            }
            return (duration > 0);
        }

        public boolean isPersistent() {
            return this.duration == -1;
        }

        public boolean isNotStickToCamera() {
            return !stickToCamera;
        }

        public Entity setStickToCamera(boolean stc) {
            this.stickToCamera = stc;
            return this;
        }

        public Entity setX(double x) {
            this.pos.x = x;
            return this;
        }

        public Entity setY(double y) {
            this.pos.y = y;
            return this;
        }

        public Entity setColor(Color c) {
            this.color = c;
            return this;
        }

        public Entity setPriority(int p) {
            this.priority = p;
            return this;
        }

        public Entity setAttribute(String attrName, Object attrValue) {
            this.attributes.put(attrName, attrValue);
            return this;
        }

        public Object getAttribute(String attrName, Object defaultValue) {
            return (this.attributes.getOrDefault(attrName, defaultValue));
        }

        public Entity setMass(double m) {
            this.mass = m;
            return this;
        }

        public Entity setElasticity(double e) {
            this.elasticity = e;
            return this;
        }

        public Entity setFriction(double f) {
            this.friction = f;
            return this;
        }

        public Entity setSpeed(double dx, double dy) {
            this.vel.x = dx;
            this.vel.y = dy;
            return this;
        }

        public Entity setAcceleration(double ax, double ay) {
            this.acc.x = ax;
            this.acc.y = ay;
            return this;
        }

        public void update(double elapsed) {
            if (!isPersistent()) {
                int val = (int) Math.max(elapsed, 1.0);
                if (duration - val > 0) {
                    setDuration(duration - val);
                } else {
                    setDuration(0);
                }
            }
            box.setRect(pos.x, pos.y, width, height);
            switch (type) {
                case RECTANGLE, IMAGE, default -> cbox = new Rectangle2D.Double(
                        box.getX() + offsetbox.getBounds().getX(),
                        box.getY() + offsetbox.getBounds().getY(),
                        box.getWidth() - (offsetbox.getBounds().getWidth() + offsetbox.getBounds().getX()),
                        box.getHeight() - (offsetbox.getBounds().getHeight() + offsetbox.getBounds().getY()));
                case ELLIPSE -> cbox = new Ellipse2D.Double(
                        box.getX() + offsetbox.getBounds().getX(),
                        box.getY() + offsetbox.getBounds().getY(),
                        box.getWidth() - (offsetbox.getBounds().getWidth() + offsetbox.getBounds().getX()),
                        box.getHeight() - (offsetbox.getBounds().getHeight() + offsetbox.getBounds().getY()));
            }

            if (Optional.ofNullable(animations).isPresent()) {
                animations.update((long) elapsed);
            }
        }

        public BufferedImage getImage() {
            return (BufferedImage) (getAnimations()
                    ? animations.getFrame()
                    : image);
        }

        public Entity addAnimation(String key, int x, int y, int tw, int th, int[] durations, String pathToImage, int loop) {
            if (Optional.ofNullable(this.animations).isEmpty()) {
                this.animations = new Animation();
            }
            this.animations.addAnimationSet(key, pathToImage, x, y, tw, th, durations, loop);
            return this;
        }

        public boolean getAnimations() {
            return Optional.ofNullable(this.animations).isPresent();
        }

        public Entity activateAnimation(String key) {
            animations.activate(key);
            return this;
        }

        public Entity addBehavior(Behavior b) {
            this.behaviors.put(b.filterOnEvent(), b);
            return this;
        }

        public int getDirection() {
            return this.vel.x > 0 ? 1 : -1;
        }

        public String toString() {
            return this.getClass().getSimpleName() + "[name:" + name + "]";
        }

        public Entity setShadow(Color shadow) {
            this.shadowColor = shadow;
            return this;
        }
    }

    /**
     * {@link AnimationSet} defining a series of Frames and their duration for a specific animation name.
     * This animationSet object is used in the {@link Animation#animationSet} attributes, to defined all the possible
     * animation into an Entity (see {@link Entity#animations}.
     * <p>
     * Here is a Fluent API to ease the Animation set definition.
     *
     * @author Frédéric Delorme
     * @Since 1.0.3
     */
    public static class AnimationSet {
        String name;
        BufferedImage[] frames;
        int[] durations;
        int loop;
        int counter;
        private int width;
        private int height;

        public AnimationSet(String key) {
            this.name = key;
        }

        public AnimationSet setFramesDuration(int[] d) {
            this.durations = d;
            return this;
        }

        public AnimationSet setSize(int w, int h) {
            this.width = w;
            this.height = h;
            return this;
        }

        public AnimationSet setLoop(int l) {
            this.loop = l;
            return this;
        }
    }

    /**
     * {@link Animation} is the animations manager for an {@link Entity}.
     * A simple Fluent APi is provided to declare animationSet and to activate one of those set.
     * <p>
     * the {@link Animation#update(long)} process will refresh the and compute the frame according to the elapsed
     * time since previous call, while the {@link Animation#getFrame()} will return the current active frame
     * ({@link BufferedImage}) for the current active AnimationSet.
     *
     * @author Frédéric Delorme
     * @since 1.0.3
     */
    public static class Animation {
        Map<String, AnimationSet> animationSet = new HashMap<>();
        public String currentAnimationSet;
        public int currentFrame;
        private long internalAnimationTime;

        private boolean loop = true;

        public Animation() {
            currentAnimationSet = null;
            currentFrame = 0;
        }

        public Animation activate(String key) {
            this.currentAnimationSet = key;
            AnimationSet aSet = this.animationSet.get(key);
            if (currentFrame > aSet.frames.length) {
                this.currentFrame = 0;
                this.internalAnimationTime = 0;
                aSet.counter = 0;
            }
            return this;
        }

        public Animation addAnimationSet(String key, String imgSrc, int x, int y, int tw, int th, int[] durations, int loop) {
            AnimationSet aSet = new AnimationSet(key).setSize(tw, th);
            BufferedImage image = Resources.loadImage(imgSrc);
            aSet.frames = new BufferedImage[durations.length];
            for (int i = 0; i < durations.length; i++) {
                BufferedImage frame = image.getSubimage(x + (i * tw), y, tw, th);
                aSet.frames[i] = frame;
            }
            aSet.setFramesDuration(durations);
            aSet.setLoop(loop);
            animationSet.put(key, aSet);
            return this;
        }

        public synchronized void update(long elapsedTime) {
            internalAnimationTime += elapsedTime;
            AnimationSet aSet = animationSet.get(currentAnimationSet);
            currentFrame = aSet.durations.length > currentFrame ? currentFrame : 0;
            if (aSet.durations[currentFrame] <= internalAnimationTime) {
                internalAnimationTime = 0;
                if (currentFrame + 1 < aSet.frames.length) {
                    currentFrame = currentFrame + 1;
                } else {
                    if (aSet.counter + 1 < aSet.loop) {
                        aSet.counter++;
                    }
                    currentFrame = 0;
                }
            }
        }

        public synchronized BufferedImage getFrame() {
            if (animationSet.get(currentAnimationSet) != null
                    && currentFrame < animationSet.get(currentAnimationSet).frames.length) {
                return animationSet.get(currentAnimationSet).frames[currentFrame];
            }
            return null;
        }

    }

    /**
     * A {@link TextEntity} extending the {@link Entity} will display a {@link TextEntity#text} with a dedicated
     * {@link TextEntity#font}, with an {@link TextEntity#align} as required on the {@link TextEntity#pos}.
     */
    public static class TextEntity extends Entity {
        private String text;
        private Font font;
        private TextAlign align = TextAlign.LEFT;

        /**
         * Create a new {@link TextEntity} with a name.
         *
         * @param name the name for this new TextEntity (see {@link Entity#Entity(String)}
         */
        public TextEntity(String name) {
            super(name);
            this.physicType = PhysicType.STATIC;
        }

        /**
         * Set the text value t to be displayed for this {@link TextEntity}.
         *
         * @param t the text for the {@link TextEntity}.
         * @return the {@link TextEntity} with its new text.
         */
        public TextEntity setText(String t) {
            this.text = t;
            return this;
        }

        /**
         * Set the {@link Font} t for this {@link TextEntity}.
         *
         * @param f the {@link Font} to be assigned to this {@link TextEntity}.
         * @return the {@link TextEntity} object with its new assigned {@link Font}.
         */
        public TextEntity setFont(Font f) {
            this.font = f;
            return this;
        }

        /**
         * Set the {@link TextAlign} value for this {@link TextEntity}.
         *
         * @param a the {@link TextAlign} value defining howto draw the text at its {@link TextEntity#pos}.
         * @return the {@link TextEntity} object with its new text alignement defined.
         */
        public TextEntity setAlign(TextAlign a) {
            this.align = a;
            return this;
        }

    }

    /**
     * <p> A {@link GaugeEntity} extending the {@link Entity} to display a Gauge on the HUD, to show Energy or Mana
     * of a dedicated Entity.</p>
     * <p></p>At declaration, you must set a Min and  max <code>value</code> representing the gauge <code>minValue</code>
     * and <code>maxValue</code> for your value:
     * <pre>
     * GaugeEntity myGE = new GaugeEntity("myValue")
     *   .setPosition(10,10)
     *   .setSize(8,40)
     *   .setMin(0)
     *   .setMax(100)
     *   .setColor(Color.RED);
     * </pre>
     * </p>
     */
    public static class GaugeEntity extends Entity {
        double value = 0;
        private double maxValue;
        private double minValue;
        private Color border = Color.GRAY;
        private Color shadow = Color.DARK_GRAY;

        public GaugeEntity(String name) {
            super(name);
            physicType = PhysicType.STATIC;
            stickToCamera = true;
        }

        public GaugeEntity setValue(double v) {
            this.value = v;
            return this;
        }

        public GaugeEntity setMax(double mxV) {
            this.maxValue = mxV;
            return this;
        }

        public GaugeEntity setMin(double mnV) {
            this.minValue = mnV;
            return this;
        }
    }

    /**
     * <p>The {@link ValueEntity} extends the {@link Entity} by adding the capability to display an integer value.</p>
     * <p>The ValueEntity will be created as follow: setting {@link ValueEntity} specific attributes like
     * the <code>value</code>, the display <code>format</code>, the array of {@link BufferedImage}
     * as <code>figures</code>, and then, the {@link Entity} inherited attributes:</p>
     * <pre>
     * ValueEntity scoreEntity = (ValueEntity) new ValueEntity("score")
     *   .setValue(score)
     *   .setFormat("%06d")
     *   .setFigures(figs)
     *   .setPosition(20, 20)
     *   .setSize(6 * 8, 16)
     *   .setStickToCamera(true);
     * app.addEntity(scoreEntity);
     * </pre>
     *
     * @author Frédéric Delorme
     * @since 1.0.2
     */
    public static class ValueEntity extends Entity {
        int value;
        String valueTxt;
        private BufferedImage[] figures;
        private String format = "%d";

        /**
         * Create a new {@link ValueEntity} with its name.
         *
         * @param name the name of this new {@link ValueEntity}.
         */
        public ValueEntity(String name) {
            super(name);
            this.physicType = PhysicType.STATIC;
        }

        /**
         * Value displayed as text must be updated according to the value and its string format.
         * This is what happened during the update() processing.
         *
         * @param elapsed the elapsed time since previous call.
         */
        @Override
        public void update(double elapsed) {
            super.update(elapsed);
            valueTxt = String.format(format, value);
        }

        /**
         * Define the {@link ValueEntity} value to be displayed.
         *
         * @param value the new value for this {@link ValueEntity}.
         * @return this ValueEntity with its new value.
         */
        public ValueEntity setValue(int value) {
            this.value = value;
            return this;
        }

        /**
         * Define the array of {@link BufferedImage} as {@link ValueEntity#figures} to render this integer
         * {@link ValueEntity#value}.
         *
         * @param figures the new BufferedImage array to be used as figures (Must provide an array of 10 {@link BufferedImage},
         *                corresponding to the 10 digits from 0 to 9).
         * @return this {@link ValueEntity} with its new figures to be used to draw the integer value.
         */
        public ValueEntity setFigures(BufferedImage[] figures) {
            this.figures = figures;
            return this;
        }

        /**
         * The value for {@link ValueEntity} must be transformed to a String with some conversion rule according
         * to the {@link String#format(String, Object...)} method.
         *
         * @param f the new {@link String#format(String, Object...)} value to be used for integer to String
         *          conversion (default is "%d").
         * @return this {@link ValueEntity} with is new String format attribute.
         */
        public ValueEntity setFormat(String f) {
            this.format = f;
            return this;
        }
    }

    /**
     * MapEntity is a displayed map of the all Scene existing active objects.
     * The Render will display all object according to a defined color code.
     *
     * @author Frédéric Delorme
     * @since 1.0.4
     */
    public static class MapEntity extends Entity {
        List<Entity> entitiesRef = new ArrayList<>();
        Map<String, Color> colorEntityMapping = new HashMap<>();
        Color backgroundColor = new Color(0.1f, 0.1f, 0.1f, 0.4f);
        private World world;

        public MapEntity(String name) {
            super(name);
            setPhysicType(PhysicType.STATIC);
            setStickToCamera(true);
        }

        public MapEntity setWorld(World w) {
            this.world = w;
            return this;
        }

        public MapEntity setColorMapping(Map<String, Color> mp) {
            this.colorEntityMapping = mp;
            return this;
        }

        public MapEntity setRefEntities(List<Entity> le) {
            this.entitiesRef = le;
            return this;
        }
    }

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
    public static class Camera extends Entity {

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
    }

    /**
     * The list of light type.
     *
     * @author Frédéric Delorme
     * @since 1.0.5
     */
    public enum LightType {
        /**
         * An {@link LightType#AMBIENT} light will display a colored rectangle on all the viewport,
         * with a color corresponding to the defined {@link Light#color}.
         */
        AMBIENT,
        /**
         * A {@link LightType#SPOT} light will display directional light of with {@link Light#color} at {@link Light#pos}.
         * The light direction and length is set by the {@link Light#rotation} and {@link Light#height} attributes.
         */
        SPOT,
        AREA_RECTANGLE,
        /**
         * A {@link LightType#SPHERICAL} light will display an ellipse centered light of {@link Light#width} x {@link Light#height}
         * with {@link Light#color} at {@link Light#pos}.
         */
        SPHERICAL
    }

    /**
     * <p>A {@link Light} class to simulate lights in a {@link Scene}.</p>
     * It can be a {@link LightType#SPOT}, an {@link LightType#AMBIENT} or a {@link LightType#SPHERICAL} one.
     * It will have an {@link Light#energy}, and specific {@link Light#rotation}
     * angle and a {@link Light#height}  (for SPOT only), or a {@link Light#width} and {@link Light#height}
     * of the ellipse size(for SPHERICAL only) and a glitter effect (see {@link Light#glitterEffect},
     * to simulate neon glittering light.
     *
     * @author Frédéric Delorme
     * @since 1.0.5
     */
    public static class Light extends Entity {
        public Color[] colors;
        public float[] dist;
        public RadialGradientPaint rgp;
        private double energy;
        private LightType lightType;
        private double rotation;
        private double glitterEffect;

        /**
         * Create a new Light with a name
         *
         * @param name the name of this new light in the Scene.
         */
        public Light(String name) {
            super(name);
            setType(NONE);
            setPhysicType(PhysicType.NONE);
            setStickToCamera(false);
        }

        /**
         * Set the light type;
         *
         * @param lt the LightType to be assigned to this light.
         * @return the updated Light entity.
         */
        public Light setLightType(LightType lt) {
            this.lightType = lt;
            return this;
        }

        /**
         * Define the energy for this Light.
         *
         * @param e the value of energy from 0 to 1.0.
         * @return the updated Light entity.
         */
        public Light setEnergy(double e) {
            this.energy = e;
            return this;
        }

        /**
         * Define the light spot direction (only for {@link LightType#SPOT}
         *
         * @param r the rotation angle in radian.
         * @return the updated Light entity.
         */
        public Light setRotation(double r) {
            this.rotation = r;
            return this;
        }

        /**
         * The glitterEffect factor, adding an offset to the light center to create aglitter effect.
         *
         * @param ge the Glitter factor from 0 to 1.0
         * @return the updated Light entity.
         */
        public Light setGlitterEffect(double ge) {
            this.glitterEffect = ge;
            return this;
        }

        @Override
        public void update(double elapsed) {
            super.update(elapsed);
        }
    }

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

    public interface Behavior {
        String onCollision = "onCollide";
        String updateEntity = "updateEntity";
        String updateScene = "updateScene";

        String filterOnEvent();

        void update(Application a, Entity e, double elapsed);

        void update(Application a, double elapsed);

        void onCollide(Application a, Entity e1, Entity e2);

    }

    public boolean exit = false;

    public boolean pause = false;

    private final boolean[] prevKeys = new boolean[65536];
    private final boolean[] keys = new boolean[65536];
    private boolean anyKeyPressed;
    private boolean keyCtrlPressed;
    private boolean keyShiftPressed;

    public Configuration config;
    public Render render;
    private PhysicEngine physicEngine;
    private CollisionDetector collisionDetect;
    public ActionHandler actionHandler;
    private Scene activeScene;

    private AppStatus appStats;

    private long realFps = 0;

    private long computationTime = 0;

    public Map<String, Entity> entities = new HashMap<>();
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
                .setGravity(config.worldGravity);
    }

    private boolean start() {
        try {
            initializeServices();
            createWindow();
            if (loadScenes()) {
                initDefaultActions();
                // prepare services
                createJMXStatus(this);
                System.out.printf("INFO: scene %s activated and created.\n", activeScene.getName());
            }
        } catch (Exception e) {
            System.out.println("ERR: Unable to initialize scene: " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

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
        physicEngine = new PhysicEngine(this, config, world);
        collisionDetect = new CollisionDetector(this, config, world);
        actionHandler = new ActionHandler(this);
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
    public boolean loadScenes() {
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
                e.printStackTrace(System.out);
                return false;
            }
        }
        return true;
    }

    protected void createScene() throws Exception {
        activeScene.create(this);
    }

    protected void activateScene(String name) {
        if (scenes.containsKey(name)) {
            if (Optional.ofNullable(this.activeScene).isPresent()) {
                this.activeScene.dispose();
            }
            Scene scene = scenes.get(name);
            try {
                scene.prepare();
                sceneReady = scene.create(this);
                this.activeScene = scene;
            } catch (Exception e) {
                System.out.println("ERR: Unable to initialize the Scene " + name + " => " + e.getLocalizedMessage());
            }
        } else {
            System.out.print("ERR: Unable to load unknown scene " + name);
        }
    }

    private void createJMXStatus(Application application) {
        appStats = new AppStatus(application, "Application");
        appStats.register(application);
    }

    public void reset() {
        try {
            render.clear();
            entities.clear();
            entityIndex = 0;
            createScene();
        } catch (Exception e) {
            System.out.println("ERR: Reset scene issue: " + e.getLocalizedMessage());
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
                System.out.println("ERR: Unable to wait for " + waitTime + ": " + ie.getLocalizedMessage());
            }

            // Update JMX metrics
            appStats.update(this);

            previous = start;
        }
    }

    private void input() {
        activeScene.input(this);
    }

    private synchronized void update(double elapsed) {
        if (!pause) {
            double maxElapsedTime = Math.min(elapsed, config.frameTime);
            physicEngine.update(maxElapsedTime);
            collisionDetect.update(maxElapsedTime);
            if (sceneReady) {
                activeScene.update(this, elapsed);
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

    /**
     * Retrieve the internal entity Index current value.
     *
     * @return
     */
    public static long getEntityIndex() {
        return entityIndex;
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

    public static void main(String[] args) {
        try {
            Application app = new Application(args);
            app.run();
        } catch (Exception e) {
            System.out.printf("ERR: Unable to run application: %s",
                    e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

}
