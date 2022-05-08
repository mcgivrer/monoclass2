package com.demoing.app.core;

import javax.imageio.ImageIO;
import javax.management.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.demoing.app.core.Application.EntityType.*;

public class Application extends JFrame implements KeyListener {

    private static final int FPS_DEFAULT = 60;

    private static long entityIndex = 0;
    private Map<String, Scene> scenes = new HashMap<>();
    private boolean sceneReady;

    public static long getEntityIndex() {
        return entityIndex;
    }

    public enum EntityType {
        RECTANGLE,
        ELLIPSE,
        IMAGE
    }

    public enum PhysicType {
        DYNAMIC,
        STATIC
    }

    public enum TextAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    public interface AppStatusMBean {
        Integer getDebugLevel();

        void setDebugLevel(Integer d);

        Integer getNbEntities();

        Integer getPipelineSize();

        Boolean getPauseStatus();

        void setPauseStatus(Boolean pause);

        Long getTimeUpdate();

        Long getTimeRendering();

        Long getTimeComputation();

        Long getRealFPS();

        void requestQuit();

        void requestAddEntity(Integer add);

        void requestremoveEntity(Integer add);

        void requestReset();

    }

    public class AppStatus implements AppStatusMBean {
        private Application app;
        private int debugLevel;
        private int nbEntities, pipelineSize;
        boolean pauseStatus;
        private long realFPS, timeRendering, timeUpdate, computationTime;
        private String programName;

        public AppStatus(Application app, String name) {
            this.programName = name;
            this.nbEntities = 0;
        }

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
        public synchronized void requestAddEntity(Integer nbEntity) {
        }

        @Override
        public synchronized void requestremoveEntity(Integer nbEntity) {
        }

        @Override
        public synchronized void requestReset() {
            app.reset();
        }
    }

    public static class Configuration {
        Properties appProps = new Properties();
        public double screenWidth = 320.0;
        public double screenHeight = 200.0;
        private double displayScale = 2.0;
        private double fps = 0.0;
        public int debug;
        private long frameTime = 0;
        private double worldWidth = 0;
        private double worldHeight = 0;
        private double worldGravity = 1.0;
        private boolean fullScreen = false;

        private double speedMinValue = 0.1;
        private double speedMaxValue = 4.0;
        private double colSpeedMinValue = 0.1;
        private double colSpeedMaxValue = 2.0;

        private double accMinValue = 0.1;
        private double accMaxValue = 0.35;

        public String scenes;
        public String defaultScene;

        public Configuration(String fileName) {
            try {
                appProps.load(this.getClass().getResourceAsStream(fileName));
                loadConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void loadConfig() {
            screenWidth = parseDouble(appProps.getProperty("app.screen.width", "320.0"));
            screenHeight = parseDouble(appProps.getProperty("app.screen.height", "200.0"));
            displayScale = parseDouble(appProps.getProperty("app.screen.scale", "2.0"));
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
            convertStringToBoolean(appProps.getProperty("app.screen.full", "false"));

            scenes = appProps.getProperty("app.scenes");
            defaultScene = appProps.getProperty("app.scene.default");
        }

        private double parseDouble(String stringValue) {
            return Double.parseDouble(stringValue);
        }

        private int parseInt(String stringValue) {
            return Integer.parseInt(stringValue);
        }

        private Configuration parseArgs(String[] args) {
            Arrays.asList(args).forEach(arg -> {
                String[] values = arg.split("=");
                switch (values[0].toLowerCase()) {
                    case "w", "width" -> screenWidth = parseDouble(values[1]);
                    case "h", "height" -> screenHeight = parseDouble(values[1]);
                    case "s", "scale" -> displayScale = parseDouble(values[1]);
                    case "d", "debug" -> debug = parseInt(values[1]);
                    case "ww", "worldwidth" -> worldWidth = parseDouble(values[1]);
                    case "wh", "worldheight" -> worldHeight = parseDouble(values[1]);
                    case "wg", "worldgravity" -> worldGravity = parseDouble(values[1]);
                    case "spmin" -> speedMinValue = parseDouble(values[1]);
                    case "spmax" -> speedMaxValue = parseDouble(values[1]);
                    case "accmin" -> accMinValue = parseDouble(values[1]);
                    case "accmax" -> accMaxValue = parseDouble(values[1]);
                    case "cspmin" -> colSpeedMinValue = parseDouble(values[1]);
                    case "cspmax" -> colSpeedMaxValue = parseDouble(values[1]);
                    case "fps" -> fps = parseDouble(values[1]);
                    case "f", "fullScreen" -> convertStringToBoolean(values[1]);
                    case "scene" -> defaultScene = values[1];
                    default -> System.out.printf("\nERR : Unknown argument %s\n", arg);
                }
            });
            return this;
        }

        private void convertStringToBoolean(String values) {
            fullScreen = "on|ON|true|True|TRUE".contains(values);
        }

    }

    public static class Render {
        private final Configuration config;
        private final World world;
        Application app;
        BufferedImage buffer;
        private Font debugFont;
        long renderingTime = 0;

        private List<Entity> gPipeline = new CopyOnWriteArrayList<>();
        Camera activeCamera;

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
            gPipeline.stream().filter(e -> e.isAlive() || e.isInfiniteLife())
                    .forEach(e -> {
                        if (e.isNotStickToCamera()) {
                            moveCamera(g, activeCamera, -1);
                        }
                        g.setColor(e.color);
                        switch (e) {

                            // This is a TextEntity
                            case TextEntity te -> {
                                g.setFont(te.font);
                                int size = g.getFontMetrics().stringWidth(te.text);
                                double offsetX = te.align.equals(TextAlign.RIGHT) ? -size
                                        : te.align.equals(TextAlign.CENTER) ? -size * 0.5 : 0;
                                g.drawString(te.text, (int) (te.x + offsetX), (int) te.y);
                                e.width = size;
                                e.height = g.getFontMetrics().getHeight();
                                e.box.setRect(e.x + offsetX, e.y - e.height + g.getFontMetrics().getDescent(), e.width,
                                        e.height);
                            }
                            case GaugeEntity ge -> {
                                g.setColor(Color.BLACK);
                                g.fillRect((int) ge.x, (int) ge.y, (int) ge.width, (int) ge.height);
                                g.setColor(ge.border);
                                g.fillRect((int) ge.x, (int) ge.y, (int) ge.width, (int) ge.height);
                                int value = (int) ((ge.value / ge.maxValue) * ge.width - 2);
                                g.setColor(ge.color);
                                g.fillRect((int) (ge.x) + 1, (int) (ge.y) + 1, value, (int) (ge.height) - 2);
                            }
                            // This is a basic entity
                            case Entity ee -> {
                                switch (ee.type) {
                                    case RECTANGLE ->
                                            g.fillRect((int) ee.x, (int) ee.y, (int) ee.width, (int) ee.height);
                                    case ELLIPSE ->
                                            g.fillArc((int) ee.x, (int) ee.y, (int) ee.width, (int) ee.height, 0, 360);
                                    case IMAGE -> {
                                        BufferedImage sprite = (BufferedImage) (ee.getAnimations()
                                                ? ee.animations.getFrame()
                                                : ee.image);
                                        if (ee.getDirection() > 0) {
                                            g.drawImage(sprite, (int) ee.x, (int) ee.y, null);
                                        } else {
                                            g.drawImage(sprite,
                                                    (int) (ee.x + ee.width), (int) ee.y,
                                                    (int) (-ee.width), (int) ee.height,
                                                    null);
                                        }
                                    }
                                }
                            }
                        }
                        drawDebugInfo(g, e);
                        if (e.isNotStickToCamera()) {
                            moveCamera(g, activeCamera, 1);
                        }
                    });
            g.dispose();
            renderToScreen(realFps);
            renderingTime = System.nanoTime() - startTime;
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
                    g.drawRect((int) e.x, (int) e.y, 1, 1);
                }
                // display id
                g.setFont(debugFont);
                int lineHeight = g.getFontMetrics().getHeight();// + g.getFontMetrics().getDescent();
                g.setColor(Color.ORANGE);
                int offsetX = (int) (e.x + e.width + 4);
                int offsetY = (int) (e.y - 8);
                g.drawString(String.format("#%d", e.id), (int) e.x, offsetY);
                // display LifeBar
                if (!e.isInfiniteLife() && e.isAlive()) {
                    g.setColor(Color.RED);
                    g.fillRect((int) e.x, (int) e.y - 4, (int) ((32) * e.duration / e.startDuration), 2);
                }
                if (config.debug > 1) {
                    // display colliding box
                    g.setColor(e.collide ? new Color(1.0f, 0.0f, 0.0f, 0.3f) : new Color(0.0f, 0.0f, 1.0f, 0.3f));
                    g.fill(e.cbox);
                    if (config.debug > 2) {
                        // display 2D parameters
                        g.setColor(Color.ORANGE);
                        g.drawString(String.format(Locale.ROOT, "name:%s", e.name), offsetX, offsetY + lineHeight);
                        g.drawString(String.format(Locale.ROOT, "pos:%03.0f,%03.0f", e.x, e.y), offsetX, offsetY + (lineHeight * 2));
                        g.drawString(String.format("life:%d", e.duration), offsetX, offsetY + (lineHeight * 3));
                        if (config.debug > 3) {
                            // display Physic parameters
                            g.drawString(String.format(Locale.ROOT, "spd:%03.2f,%03.2f", e.dx, e.dy), offsetX,
                                    offsetY + (lineHeight * 4));
                            g.drawString(String.format(Locale.ROOT, "acc:%03.2f,%03.2f", e.ax, e.ay), offsetX,
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
            Graphics2D g2 = (Graphics2D) app.getGraphics();
            g2.drawImage(
                    buffer,
                    0, 0, app.getWidth(), app.getHeight(),
                    0, 0, (int) config.screenWidth, (int) config.screenHeight,
                    null);
            if (config.debug > 0) {
                g2.setFont(debugFont.deriveFont(16.0f));
                g2.setColor(Color.WHITE);
                g2.drawString(String.format("[ dbg: %d| fps:%d | obj:%d | g:%f ]",
                                config.debug,
                                realFps,
                                gPipeline.size(),
                                world.gravity),

                        20, app.getHeight() - 30);
            }
            g2.dispose();
        }

        private void moveCamera(Graphics2D g, Camera cam, double direction) {
            if (Optional.ofNullable(activeCamera).isPresent()) {
                g.translate(cam.x * direction, cam.y * direction);
            }
        }

        public void addToPipeline(Entity entity) {
            if (!gPipeline.contains(entity)) {
                gPipeline.add(entity);
                gPipeline.sort((o1, o2) -> o1.priority < o2.priority ? -1 : 1);
            }
        }

        public void addCamera(Camera cam) {
            this.activeCamera = cam;
        }

        public void clear() {
            gPipeline.clear();
        }

        public void dispose() {
            clear();
            buffer = null;
        }

        public void remove(Entity e) {
            gPipeline.remove(e);
        }
    }

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
    }

    public static class PhysicEngine {
        private final Application app;
        private final World world;
        private final Configuration config;
        public long updateTime;

        public PhysicEngine(Application a, Configuration c, World w) {
            this.app = a;
            this.config = c;
            this.world = w;
        }

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
                        .filter(b -> b.getEvent().contains(Behavior.updateEntity))
                        .collect(Collectors.toList())
                        .forEach(b -> b.update(app, e, elapsed));

            });
            // TODO update Scene Behaviors
            app.activeScene.getBehaviors().values().stream()
                    .filter(b -> b.getEvent().contains(Behavior.updateScene))
                    .collect(Collectors.toList())
                    .forEach(b -> b.update(app, elapsed));
            //  update active camera if presents.
            if (Optional.ofNullable(app.render.activeCamera).isPresent()) {
                app.render.activeCamera.update(elapsed);
            }
            updateTime = System.nanoTime() - start;
        }

        private void updateEntity(Entity e, double elapsed) {
            applyPhysicRuleToEntity(e, elapsed);
            constrainsEntity(e);
        }

        private void applyPhysicRuleToEntity(Entity e, double elapsed) {
            e.oldPos.x = e.x;
            e.oldPos.y = e.y;

            // a small reduction of time
            elapsed *= 0.4;
            e.ax = 0.0;
            e.ay = 0.0;
            e.forces.add(new Vec2d(0, e.mass * -world.gravity));
            for (Vec2d v : e.forces) {
                e.ax += v.x;
                e.ay += v.y;
            }
            e.dx += 0.5 * (Utils.ceilMinMaxValue(e.ax * elapsed, config.accMinValue, config.accMaxValue));
            e.dy += 0.5 * (Utils.ceilMinMaxValue(e.ay * elapsed, config.accMinValue, config.accMaxValue));

            e.dx *= e.friction * world.friction;
            e.dy *= e.friction * world.friction;

            e.x += Utils.ceilMinMaxValue(e.dx, config.speedMinValue, config.speedMaxValue);
            e.y += Utils.ceilMinMaxValue(e.dy, config.speedMinValue, config.speedMaxValue);

            e.forces.clear();
        }

        private double threshold(double value, double valueThreshold) {
            return (value < valueThreshold) ? 0.0 : value;
        }

        private void constrainsEntity(Entity e) {
            if (e.isAlive() || e.isInfiniteLife()) {
                constrainToWorld(e, world);
            }
        }

        private void constrainToWorld(Entity e, World world) {
            if (e.cbox.getBounds().getX() < 0.0) {
                e.x = 0.0;
                e.dx *= -1 * e.elasticity;
                e.ax = 0.0;
            }
            if (e.cbox.getBounds().getY() < 0.0) {
                e.y = 0.0;
                e.dy *= -1 * e.elasticity;
                e.ay = 0.0;
            }
            if (e.cbox.getBounds().getX() + e.cbox.getBounds().getWidth() > world.area.getWidth()) {
                e.x = world.area.getWidth() - e.width;
                e.dx *= -1 * e.elasticity;
                e.ax = 0.0;
            }
            if (e.cbox.getBounds().getY() + e.cbox.getBounds().getHeight() > world.area.getHeight()) {
                e.y = world.area.getHeight() - e.height;
                e.dx *= -1 * e.elasticity;
                e.ax = 0.0;
            }
        }

        public void dispose() {

        }
    }

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
            List<Entity> targets = colliders.values().stream().filter(e -> e.isAlive() || e.isInfiniteLife()).toList();
            for (Entity e1 : colliders.values()) {
                e1.collide = false;
                for (Entity e2 : targets) {
                    e2.collide = false;
                    if (e1.id != e2.id && e1.cbox.getBounds().intersects(e2.cbox.getBounds())) {
                        resolve(e1, e2);
                        e1.behaviors.values().stream()
                                .filter(b -> b.getEvent().equals("onCollision"))
                                .collect(Collectors.toList())
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
            Vec2d vp = new Vec2d((e2.x - e1.x), (e2.y - e1.y));
            double distance = Math.sqrt((e2.x - e1.x) * (e2.x - e1.x) + (e2.y - e1.y) * (e2.y - e1.y));
            Vec2d colNorm = new Vec2d(vp.x / distance, vp.y / distance);
            if (e1.physicType == PhysicType.DYNAMIC && e2.physicType == PhysicType.DYNAMIC) {
                Vec2d vRelSpeed = new Vec2d(e1.dx - e2.dx, e1.dy - e2.dy);
                double colSpeed = vRelSpeed.x * colNorm.x + vRelSpeed.y * colNorm.y;
                var impulse = 2 * colSpeed / (e1.mass + e2.mass);
                e1.dx -= Utils.ceilMinMaxValue(impulse * e2.mass * colSpeed * colNorm.x,
                        config.speedMinValue, config.colSpeedMaxValue);
                e1.dy -= Utils.ceilMinMaxValue(impulse * e2.mass * colSpeed * colNorm.y,
                        config.speedMinValue, config.colSpeedMaxValue);
                e2.dx += Utils.ceilMinMaxValue(impulse * e1.mass * colSpeed * colNorm.x,
                        config.speedMinValue, config.colSpeedMaxValue);
                e2.dy += Utils.ceilMinMaxValue(impulse * e1.mass * colSpeed * colNorm.y,
                        config.speedMinValue, config.colSpeedMaxValue);
                if (e1.name.equals("player") && config.debug > 4) {
                    System.out.printf("e1.%s collides e2.%s Vp=%s / dist=%f / norm=%s\n", e1.name, e2.name, vp, distance, colNorm);
                }
            } else {
                if (e1.physicType == PhysicType.DYNAMIC && e2.physicType == PhysicType.STATIC) {
                    if (e1.y + e1.height > e2.y && vp.y > 0) {
                        e1.y = e2.y - e1.height;
                        e1.dy = -e1.dy * e1.elasticity;
                    } else {
                        e1.dy = -e1.dy * e1.elasticity;
                        e1.y = e2.y + e2.height;
                    }
                    if (e1.name.equals("player") && config.debug > 4) {
                        System.out.printf("e1.%s collides static e2.%s\n", e1.name, e2.name);
                    }
                }
            }
        }
    }

    public static class ActionHandler implements KeyListener {
        private final boolean[] prevKeys = new boolean[65536];
        private final boolean[] keys = new boolean[65536];
        private boolean anyKeyPressed;

        public Map<Integer, Function> actionMapping = new HashMap<>();

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

    public static class I18n {
        private static final ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");

        private I18n() {

        }

        public static String get(String key) {
            return messages.getString(key);
        }

        public static String get(String key, Object... args) {
            return String.format(messages.getString(key), args);
        }
    }

    public static class Vec2d {
        public double x;
        public double y;

        public Vec2d(double x, double y) {
            this.x = x;
            this.y = y;
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

        public Vec2d maximize(double v) {
            return new Vec2d(
                    Math.signum(x) * Math.max(Math.abs(x), v),
                    Math.signum(y) * Math.max(Math.abs(y), v));
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

    public static class World {
        public double friction = 1.0;
        public Rectangle2D area;
        public double gravity = 0.981;

        public World() {
            area = new Rectangle2D.Double(0.0, 0.0, 320.0, 200.0);
        }

        public World setArea(double width, double height) {
            area = new Rectangle2D.Double(0.0, 0.0, width, height);
            return this;
        }

        public World setGravity(double g) {
            this.gravity = g;
            return this;
        }

        public World setFriction(double f) {
            this.friction = f;
            return this;
        }
    }

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
        public Shape bbox = new Rectangle2D.Double(0, 0, 0, 0);
        public Shape cbox = new Rectangle2D.Double(0, 0, 0, 0);
        public double x = 0.0, y = 0.0;
        public Vec2d oldPos = new Vec2d(0, 0);
        public double width = 0.0, height = 0.0;

        // Physic attributes
        public List<Vec2d> forces = new ArrayList<>();
        protected PhysicType physicType = PhysicType.DYNAMIC;
        public double ax = 0.0, ay = 0.0;
        public double dx = 0.0, dy = 0.0;
        public double mass = 1.0;
        public double elasticity = 1.0, friction = 1.0;

        // internal attributes
        protected int startDuration = -1;
        protected int duration = -1;
        public Map<String, Object> attributes = new HashMap<>();

        public Map<String, Behavior> behaviors = new HashMap<>();

        public Entity(String name) {
            this.name = name;
        }

        public Entity setPosition(double x, double y) {
            this.x = x;
            this.y = y;
            this.update(0);
            return this;
        }

        public Entity setSize(double w, double h) {
            this.width = w;
            this.height = h;
            box.setRect(x, y, w, h);
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
                case IMAGE, RECTANGLE, default -> this.bbox = new Rectangle2D.Double(left, top, right, bottom);
                case ELLIPSE -> this.bbox = new Ellipse2D.Double(left, top, right, bottom);
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
            return (duration > 0);
        }

        public boolean isInfiniteLife() {
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
            this.x = x;
            return this;
        }

        public Entity setY(double y) {
            this.y = y;
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
            this.dx = dx;
            this.dy = dy;
            return this;
        }

        public Entity setAcceleration(double ax, double ay) {
            this.ax = ax;
            this.ay = ay;
            return this;
        }

        public void update(double elapsed) {
            if (isAlive()) {
                if (isAlive()) {
                    setDuration(duration - (int) Math.max(elapsed, 1.0));
                } else {
                    setDuration(0);
                }
            }
            box.setRect(x, y, width, height);
            switch (type) {
                case RECTANGLE, IMAGE, default -> cbox = new Rectangle2D.Double(
                        box.getX() + bbox.getBounds().getX(),
                        box.getY() + bbox.getBounds().getY(),
                        box.getWidth() - (bbox.getBounds().getWidth() + bbox.getBounds().getX()),
                        box.getHeight() - (bbox.getBounds().getHeight() + bbox.getBounds().getY()));
                case ELLIPSE -> cbox = new Ellipse2D.Double(
                        box.getX() + bbox.getBounds().getX(),
                        box.getY() + bbox.getBounds().getY(),
                        box.getWidth() - (bbox.getBounds().getWidth() + bbox.getBounds().getX()),
                        box.getHeight() - (bbox.getBounds().getHeight() + bbox.getBounds().getY()));
            }

            if (Optional.ofNullable(animations).isPresent()) {
                animations.update((long) elapsed);
            }
        }

        public Entity addAnimation(String key, int x, int y, int tw, int th, int nbFrames, String pathToImage) {
            if (Optional.ofNullable(this.animations).isEmpty()) {
                this.animations = new Animation();
            }
            this.animations.addAnimationSet(key, pathToImage, x, y, tw, th, nbFrames);
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
            this.behaviors.put(b.getEvent(), b);
            return this;
        }

        public Entity setFrameDuration(String key, int frameDuration) {
            animations.setFrameDuration(key, frameDuration);
            return this;
        }

        public int getDirection() {
            return this.dx > 0 ? 1 : -1;
        }
    }

    public static class Animation {
        Map<String, BufferedImage[]> animationSet = new HashMap<>();
        Map<String, Integer> frameDuration = new HashMap<>();
        public String currentAnimationSet;
        public int currentFrame;
        private long internalAnimationTime;

        private boolean loop = true;

        public Animation() {
            currentAnimationSet = null;
            currentFrame = 0;
        }

        public Animation setFrameDuration(String key, int time) {

            this.frameDuration.put(key, time);
            return this;
        }

        public Animation activate(String key) {
            this.currentAnimationSet = key;
            if (currentFrame > this.animationSet.get(key).length) {
                this.currentFrame = 0;
                this.internalAnimationTime = 0;
            }
            return this;
        }

        public void addAnimationSet(String key, String imgSrc, int x, int y, int tw, int th, int nbFrames) {
            try {
                BufferedImage image = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream(imgSrc)));
                BufferedImage[] buffer = new BufferedImage[nbFrames];
                for (int i = 0; i < nbFrames; i++) {
                    BufferedImage frame = image.getSubimage(x + (i * tw), y, tw, th);
                    buffer[i] = frame;
                }
                animationSet.put(key, buffer);
            } catch (IOException e) {
                System.out.println("ERR: unable to read image from '" + imgSrc + "'");
            }
        }

        public synchronized void update(long elapsedTime) {
            internalAnimationTime += elapsedTime;
            if (internalAnimationTime > frameDuration.get(currentAnimationSet)) {
                internalAnimationTime = 0;
                currentFrame = currentFrame + 1 < animationSet.get(currentAnimationSet).length ? currentFrame + 1 : (loop ? 0 : currentFrame);
            }
        }

        public synchronized BufferedImage getFrame() {
            if (animationSet.get(currentAnimationSet) != null
                    && currentFrame < animationSet.get(currentAnimationSet).length) {
                return animationSet.get(currentAnimationSet)[currentFrame];
            }
            return null;
        }

    }

    public static class TextEntity extends Entity {
        private String text;
        private Font font;
        private TextAlign align = TextAlign.LEFT;

        public TextEntity(String name) {
            super(name);
            this.physicType = PhysicType.STATIC;
        }

        public TextEntity setText(String t) {
            this.text = t;
            return this;
        }

        public TextEntity setFont(Font f) {
            this.font = f;
            return this;
        }

        public TextEntity setAlign(TextAlign a) {
            this.align = a;
            return this;
        }

    }

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

    public static class Camera extends Entity {

        private Entity target;
        private double tweenFactor;
        private Rectangle2D viewport;

        public Camera(String name) {
            super(name);
            this.physicType = PhysicType.STATIC;
        }

        public Camera setTarget(Entity target) {
            this.target = target;
            return this;
        }

        public Camera setTweenFactor(double tf) {
            this.tweenFactor = tf;
            return this;
        }

        public Camera setViewport(Rectangle2D vp) {
            this.viewport = vp;
            return this;
        }

        public void update(double elapsed) {
            x += Math.round((target.x + target.width - (viewport.getWidth() * 0.5) - x) * tweenFactor * elapsed);
            y += Math.round((target.y + target.height - (viewport.getHeight() * 0.5) - y) * tweenFactor * elapsed);
        }
    }

    public interface Scene {
        boolean create(Application app) throws Exception;

        void update(Application app, double elapsed);

        void input(Application app);

        String getName();

        Map<String, Behavior> getBehaviors();

        void dispose();
    }

    public interface Behavior {
        public final String onCollision = "onCollide";
        public final String updateEntity = "updateEntity";
        public final String updateScene = "updateScene";

        public String getEvent();

        public void update(Application a, Entity e, double elapsed);

        public void update(Application a, double elapsed);

        public void onCollide(Application a, Entity e1, Entity e2);
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

    public Application(String[] args) {
        NumberFormat.getInstance(Locale.ROOT);
        initialize(args);
    }

    protected void run() {
        if (start()) {
            loop();
            dispose();
        }
    }

    private void initialize(String[] args) {
        config = new Configuration("/app.properties").parseArgs(args);
        world = new World()
                .setArea(config.worldWidth, config.worldHeight)
                .setGravity(config.worldGravity);
    }

    private boolean start() {
        try {
            initializeServices();
            createWindow();
            if (loadScenes()) {
                // prepapre services
                createJMXStatus(this);
                System.out.printf("INFO: scene %s activated and created.\n", activeScene.getName());
            }
        } catch (Exception e) {
            System.out.println("ERR: Unable to initialize scene: " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private void initializeServices() {
        render = new Render(this, config, world);
        physicEngine = new PhysicEngine(this, config, world);
        collisionDetect = new CollisionDetector(this, config, world);
        actionHandler = new ActionHandler();
    }

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
                sceneReady = scene.create(this);
                this.activeScene = scene;
            } catch (Exception e) {
                throw new RuntimeException(e);
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
        setTitle(I18n.get("app.title"));

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/sg-logo-image.png")));

        Dimension dim = new Dimension((int) (config.screenWidth * config.displayScale),
                (int) (config.screenHeight * config.displayScale));

        setSize(dim);
        setPreferredSize(dim);
        setMaximumSize(dim);
        if (config.fullScreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setUndecorated(true);
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setFocusTraversalKeysEnabled(true);

        setLocationRelativeTo(null);
        addKeyListener(this);
        addKeyListener(actionHandler);
        pack();
        setVisible(true);
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
            double maxElapsed = Math.min(elapsed, config.frameTime);
            physicEngine.update(Math.min(elapsed, config.frameTime));
            collisionDetect.update(maxElapsed);
            //if (sceneReady) {
            activeScene.update(this, elapsed);
            //}
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

    @Override
    public void dispose() {
        super.dispose();
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