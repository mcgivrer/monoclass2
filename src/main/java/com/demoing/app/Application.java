package com.demoing.app;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.text.html.HTMLDocument.HTMLReader.FormAction;

import static com.demoing.app.Application.EntityType.*;

public class Application extends JFrame implements KeyListener {

    private static final int FPS_DEFAULT = 60;

    private static long entityIndex = 0;

    public enum EntityType {
        RECTANGLE,
        ELLIPSE,
        IMAGE;
    }

    public enum PhysicType {
        DYNAMIC,
        STATIC;
    }

    public enum TextAlign {
        LEFT,
        CENTER,
        RIGHT;
    }

    public interface AppStatus {
        long getNbEntities();

        long getPipelineSize();

        long getPauseSatus();

        double getGravity();
    }

    public static class World {
        private Rectangle2D area;
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
    }

    public static class I18n {
        private static ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");

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
    }

    public static class Entity {

        // id & naming attributes
        protected long id = entityIndex++;
        protected String name = "entity_" + id;

        // Rendering attributes
        public int priority;
        protected EntityType type = RECTANGLE;
        public Image image;
        public Color color = Color.BLUE;
        public boolean stickToCamera;

        // Position attributes
        public Rectangle2D.Double box;
        public double x = 0.0, y = 0.0;
        public double width = 0.0, height = 0.0;

        // Physic attributes
        public List<Vec2d> forces = new ArrayList<>();
        protected PhysicType physicType = PhysicType.DYNAMIC;
        public double ax = 0.0, ay = 0.0;
        public double dx = 0.0, dy = 0.0;
        public double mass = 1.0;
        public double elasticity = 1.0, friction = 1.0;

        // internal attributes
        protected int life = -1;
        public Map<String, Object> attributes = new HashMap<>();

        public Entity(String name) {
            this.name = name;
        }

        public Entity setPosition(double x, double y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Entity setSize(double w, double h) {
            this.width = w;
            this.height = h;
            return this;
        }

        public Entity setType(EntityType et) {
            this.type = et;
            return this;
        }

        public Entity setLife(int l) {
            this.life = l;
            return this;
        }

        public Entity setImage(BufferedImage img) {
            this.image = img;
            return this;
        }

        public boolean isAlive() {
            return (life > 0);
        }

        public boolean isStickToCamera() {
            return stickToCamera;
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
            return (this.attributes.containsKey(attrName) ? this.attributes.get(attrName) : defaultValue);
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

            /**
             * position.x += Math.round(
             * (target.position.x + (target.size.x)
             * - ((double) (viewport.getWidth()) * 0.5f)
             * - this.position.x) * tweenFactor * Math.min(elapsed, 10));
             * position.y += Math.round(
             * (target.position.y + (target.size.y)
             * - ((double) (viewport.getHeight()) * 0.5f)
             * - this.position.y) * tweenFactor * Math.min(elapsed, 10));
             */
            x += Math.round((target.x + target.width - (viewport.getWidth() * 0.5) - x) * tweenFactor * elapsed);
            y += Math.round((target.y + target.height - (viewport.getHeight() * 0.5) - y) * tweenFactor * elapsed);

        }
    }

    Properties appProps = new Properties();

    private boolean exit;
    private double width = 320.0, height = 200.0, scale = 2.0;
    private double fps = 0.0;
    private long frameTime = 0;

    private boolean[] prevKeys = new boolean[65536];
    private boolean[] keys = new boolean[65536];

    BufferedImage buffer;

    private List<Entity> gPipeline = new CopyOnWriteArrayList<>();
    private Map<String, Entity> entities = new HashMap<>();

    Camera activeCamera;

    private World world;

    public Application() throws IOException {
        appProps.load(this.getClass().getResourceAsStream("/app.properties"));
    }

    protected void run(String[] args) throws Exception {
        initialize(args);
        loop();
        dispose();
    }

    private void initialize(String[] args) {
        loadConfig(appProps);
        parseArgs(args);
        createWindow();
        buffer = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
        try {
            createScene();
        } catch (IOException | FontFormatException e) {
            System.out.println("ERR: Unable to initialize scene: " + e.getLocalizedMessage());
        }
    }

    private void reset() {
        try {
            gPipeline.clear();
            entities.clear();
            createScene();
        } catch (IOException | FontFormatException e) {
            System.out.println("ERR : Reset scene issue: " + e.getLocalizedMessage());
        }
    }

    private void loadConfig(Properties config) {
        width = parseDouble(config.getProperty("app.screen.width", "320.0"));
        height = parseDouble(config.getProperty("app.screen.height", "200.0"));
        scale = parseDouble(config.getProperty("app.screen.scale", "2.0"));
        world = new World()
                .setArea(
                        parseDouble(config.getProperty("app.world.width", "640.0")),
                        parseDouble(config.getProperty("app.world.height", "400.0")))
                .setGravity(parseDouble(config.getProperty("app.world.gravity", "400.0")));
        fps = parseInt(config.getProperty("app.render.fps", "" + FPS_DEFAULT));
        frameTime = (long) (1000 / fps);
    }

    private void parseArgs(String[] args) {
        Arrays.asList(args).forEach(arg -> {
            String[] values = arg.split("=");
            switch (values[0].toLowerCase()) {
                case "w":
                case "width":
                    width = parseDouble(values[1]);
                    break;
                case "h":
                case "height":
                    height = parseDouble(values[1]);
                    break;
                case "s":
                case "scale":
                    scale = parseDouble(values[1]);
                    break;
                default:
                    System.out.printf("\nERR : Unknown argument %s\n", arg);
                    break;
            }
        });
    }

    private double parseDouble(String stringValue) {
        return Double.parseDouble(stringValue);
    }

    private double parseInt(String stringValue) {
        return Integer.parseInt(stringValue);
    }

    private void createWindow() {
        setTitle(appProps.getProperty("app.window.title", "Application Demo"));
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/sg-logo-image.png")));
        Dimension dim = new Dimension((int) (width * scale), (int) (height * scale));
        setSize(dim);
        setPreferredSize(dim);
        setMaximumSize(dim);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setFocusTraversalKeysEnabled(true);
        setLocationRelativeTo(null);
        addKeyListener(this);
        pack();
        setVisible(true);
    }

    protected void createScene() throws IOException, FontFormatException {
        world.setGravity(-0.008);

        // A main player Entity.
        Entity player = new Entity("player")
                .setType(RECTANGLE)
                .setPosition(width * 0.5, height * 0.5)
                .setElasticity(0.32)
                .setFriction(0.89)
                .setSize(16, 16)
                .setColor(Color.RED)
                .setPriority(1)
                .setMass(10.0)
                .setAttribute("life", 5)
                .setAttribute("score", 0)
                .setAttribute("energy", 100)
                .setAttribute("mana", 100)
                .setAttribute("accStep", 0.15);
        addEntity(player);

        Camera cam = new Camera("cam01")
                .setViewport(new Rectangle2D.Double(0, 0, width, height))
                .setTarget(player)
                .setTweenFactor(0.005);
        addCamera(cam);

        generateEntity("ball_", 50, 1.2);

        Font wlcFont = Font.createFont(
                        Font.PLAIN,
                        this.getClass().getResourceAsStream("/fonts/FreePixel.ttf"))
                .deriveFont(12.0f);

        // Score Display
        int score = (int) player.getAttribute("score", 0);
        Font scoreFont = wlcFont.deriveFont(16.0f);
        String scoreTxt = String.format("%06d", score);
        TextEntity scoreTxtE = (TextEntity) new TextEntity("score")
                .setText(scoreTxt)
                .setAlign(TextAlign.LEFT)
                .setFont(scoreFont)
                .setPosition(20, 30)
                .setColor(Color.WHITE)
                .setLife(-1)
                .setStickToCamera(true);
        addEntity(scoreTxtE);


        Font lifeFont = new Font("Noto Mono for Powerline", Font.PLAIN, 16);
        TextEntity lifeTxt = (TextEntity) new TextEntity("score")
                .setText("5")
                .setAlign(TextAlign.LEFT)
                .setFont(lifeFont)
                .setPosition(buffer.getWidth() - 40, 30)
                .setColor(Color.RED)
                .setLife(-1)
                .setStickToCamera(true);
        addEntity(lifeTxt);

        // A welcome Text
        TextEntity welcomeMsg = (TextEntity) new TextEntity("welcome")
                .setText(I18n.get("app.message.welcome"))
                .setAlign(TextAlign.CENTER)
                .setFont(wlcFont)
                .setPosition(width * 0.5, height * 0.8)
                .setColor(Color.WHITE)
                .setLife(5000)
                .setStickToCamera(true);
        addEntity(welcomeMsg);
    }

    private void generateEntity(String namePrefix, int nbEntity, double acc) {
        for (int i = 0; i < nbEntity; i++) {
            Entity e = new Entity(namePrefix + i)
                    .setType(ELLIPSE)
                    .setSize(8, 8)
                    .setPosition(Math.random() * width, Math.random() * height)
                    .setAcceleration(
                            (Math.random() * 2 * acc) - acc,
                            (Math.random() * 2 * acc) - acc)
                    .setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()))
                    .setLife((int) ((Math.random() * 5) + 5) * 5000)
                    .setElasticity(0.35)
                    .setFriction(0.89)
                    .setMass(20.0);
            addEntity(e);
        }
    }

    private void addEntity(Entity entity) {
        if (!gPipeline.contains(entity)) {
            gPipeline.add(entity);
            gPipeline.sort((o1, o2) -> {
                return o1.priority < o2.priority ? -1 : 1;
            });
            entities.put(entity.name, entity);
        }
    }

    private void addCamera(Camera cam) {
        this.activeCamera = cam;
    }

    private void loop() {
        long previous = System.currentTimeMillis();
        while (!exit) {

            long start = System.currentTimeMillis();
            double elapsed = start - previous;

            input();
            update(Math.min(elapsed, frameTime));
            draw();

            // wait at least 1ms.
            long waitTime = (frameTime > elapsed) ? frameTime - (long) elapsed : 1;

            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ie) {
                System.out.println("Unable to wait for " + waitTime + ": " + ie.getLocalizedMessage());
            }

            previous = start;
        }
    }

    private void input() {
        Entity p = entities.get("player");
        double speed = (double) p.getAttribute("accStep", 4.0);

        if (getKeyPressed(KeyEvent.VK_LEFT)) {
            p.forces.add(new Vec2d(-speed, 0.0));
        }
        if (getKeyPressed(KeyEvent.VK_RIGHT)) {
            p.forces.add(new Vec2d(speed, 0.0));
        }
        if (getKeyPressed(KeyEvent.VK_UP)) {
            p.forces.add(new Vec2d(0.0, -speed));
        }
        if (getKeyPressed(KeyEvent.VK_DOWN)) {
            p.forces.add(new Vec2d(0.0, speed));
        }
        if (getKeyReleased(KeyEvent.VK_ESCAPE)) {
            reset();
        }

    }

    private void update(double elapsed) {
        // update entities
        entities.values().stream().forEach((e) -> {
            if (e.physicType.equals(PhysicType.DYNAMIC)) {
                updateEntity(e, elapsed);
            }
            if (e.isAlive()) {
                if (e.life >= 0 & e.life != -1) {
                    e.life -= Math.max(elapsed, 1.0);
                } else {
                    e.life = 0;
                }
            }
        });
        // update active camera3
        if (Optional.ofNullable(activeCamera).isPresent()) {
            activeCamera.update(elapsed);
        }
    }

    private void updateEntity(Entity e, double elapsed) {
        applyPhysicRuleToEntity(e, elapsed);
        constrainsEntity(e);
        e.ax = 0.0;
        e.ay = 0.0;
    }

    private void applyPhysicRuleToEntity(Entity e, double elapsed) {
        // a small reduction of time
        elapsed *= 0.4;

        e.forces.add(new Vec2d(0, e.mass * -world.gravity));
        for (Vec2d v : e.forces) {
            e.ax += v.x;
            e.ay += v.y;
        }
        e.dx += 0.5 * (e.ax * elapsed);
        e.dy += 0.5 * (e.ay * elapsed);

        e.dx *= e.friction;
        e.dy *= e.friction;

        e.x = Math.round(e.x + e.dx);
        e.y = Math.round(e.y + e.dy);

        e.forces.clear();
    }

    private void constrainsEntity(Entity e) {
        constrainToWorld(e, world);
    }

    private void constrainToWorld(Entity e, World world) {
        if (e.x < 0.0) {
            e.x = 0.0;
            e.dx *= -(e.dx);
            e.ax = 0.0;
        }
        if (e.y < 0.0) {
            e.y = 0.0;
            e.dy *= -(e.dy);
            e.ay = 0.0;
        }
        if (e.x + e.width > world.area.getWidth()) {
            e.x = world.area.getWidth() - e.width;
            e.dx *= -(e.dx);
            e.ax = 0.0;
        }
        if (e.y + e.height > world.area.getHeight()) {
            e.y = world.area.getHeight() - e.height;
            e.dy *= -(e.dy);
            e.ay = 0.0;
        }
    }

    private void draw() {
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, (int) width, (int) height);
        moveCamera(g, activeCamera, -1);
        drawGrid(g, world, 16, 16);
        moveCamera(g, activeCamera, 1);
        gPipeline.stream().filter(e -> e.isAlive() || e.life == -1)
                .forEach(e -> {
                    if (!e.isStickToCamera()) {
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
                        }

                        // This is a basic entity
                        case Entity ee -> {
                            switch (ee.type) {
                                case RECTANGLE -> {
                                    g.fillRect((int) ee.x, (int) ee.y, (int) ee.width, (int) ee.height);
                                }
                                case ELLIPSE -> {
                                    g.fillArc((int) ee.x, (int) ee.y, (int) ee.width, (int) ee.height, 0, 360);
                                }
                                case IMAGE -> {
                                    g.drawImage(ee.image, (int) ee.x, (int) ee.y, null);
                                }
                            }
                        }
                    }
                    if (!e.isStickToCamera()) {
                        moveCamera(g, activeCamera, 1);
                    }
                });
        g.dispose();
        renderToScreen();
    }

    private void drawGrid(Graphics2D g, World world, double tw, double th) {
        g.setColor(Color.BLUE);
        for (double tx = 0; tx < world.area.getWidth(); tx += tw) {
            for (double ty = 0; ty < world.area.getHeight(); ty += th) {
                g.drawRect((int) tx, (int) ty, (int) tw, (int) th);
            }
        }
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, (int) world.area.getWidth(), (int) world.area.getHeight());
    }

    private void renderToScreen() {
        Graphics2D g2 = (Graphics2D) getGraphics();
        g2.drawImage(
                buffer,
                0, 0, getWidth(), getHeight(),
                0, 0, (int) width, (int) height,
                null);
        g2.dispose();
    }

    private void moveCamera(Graphics2D g, Camera cam, double direction) {
        if (Optional.ofNullable(activeCamera).isPresent()) {
            g.translate(cam.x * direction, cam.y * direction);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        draw();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        prevKeys[e.getKeyCode()] = keys[e.getKeyCode()];
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        prevKeys[e.getKeyCode()] = keys[e.getKeyCode()];
        keys[e.getKeyCode()] = false;
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
            Application app = new Application();
            app.run(args);
        } catch (Exception e) {
            System.out.printf("ERR: Unable to run application: %s", e.getLocalizedMessage());
        }
    }

}