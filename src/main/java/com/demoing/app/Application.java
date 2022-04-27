package com.demoing.app;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

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

    public static class Entity extends Rectangle2D.Double {

        protected long id = entityIndex++;
        protected String name = "entity_" + id;
        protected EntityType type = RECTANGLE;
        protected PhysicType physicType = PhysicType.DYNAMIC;

        protected int life = -1;
        public Image image;
        public Color color = Color.BLUE;

        public int priority;

        public double ax = 0.0, ay = 0.0;
        public double dx = 0.0, dy = 0.0;
        public double mass = 1.0;
        public double elasticity = 1.0, friction = 1.0;

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
            setX(getCenterX() + (target.getCenterX() - getCenterX()) * tweenFactor * elapsed);
            setY(getCenterY() + (target.getCenterY() - getCenterY()) * tweenFactor * elapsed);
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

    private List<Entity> gPipeline = new ArrayList<>();
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
        world.setGravity(0.0);
        // A main player Entity.
        Entity player = new Entity("player")
                .setType(RECTANGLE)
                .setPosition(width * 0.5, height * 0.5)
                .setElasticity(0.95)
                .setFriction(0.30)
                .setSize(16, 16)
                .setColor(Color.RED)
                .setPriority(1)
                .setMass(10.0)
                .setAttribute("life", 5)
                .setAttribute("score", 0)
                .setAttribute("energy", 100)
                .setAttribute("mana", 100);
        addEntity(player);

        Camera cam = new Camera("cam01")
                .setViewport(new Rectangle2D.Double(0, 0, width, height))
                .setTarget(player)
                .setTweenFactor(0.02);
        addCamera(cam);

        generateEntity("ball_", 10);

        // A welcome Text
        Font wlcFont = Font.createFont(Font.PLAIN, this.getClass().getResourceAsStream("/fonts/FreePixel.ttf"))
                .deriveFont(12.0f);
        TextEntity welcomeMsg = (TextEntity) new TextEntity("welcome")
                .setText(I18n.get("app.message.welcome"))
                .setAlign(TextAlign.CENTER)
                .setFont(wlcFont)
                .setPosition(width * 0.5, height * 0.8)
                .setColor(Color.WHITE)
                .setLife(5000);
        addEntity(welcomeMsg);
        // Score Display

        int score = (int) player.getAttribute("score", 0);
        Font scoreFont = wlcFont.deriveFont(16.0f);
        TextEntity scoreTxt = (TextEntity) new TextEntity("score")
                .setText(String.format("%60d", score))
                .setAlign(TextAlign.CENTER)
                .setFont(wlcFont)
                .setPosition(20, 20)
                .setColor(Color.WHITE)
                .setLife(-1);
        addEntity(scoreTxt);

    }

    private void generateEntity(String namePrefix, int nbEntity) {
        for (int i = 0; i < nbEntity; i++) {
            Entity e = new Entity(namePrefix + i)
                    .setType(ELLIPSE)
                    .setSize(8, 8)
                    .setPosition(Math.random() * width, Math.random() * height)
                    .setAcceleration(Math.random() * 2.0, Math.random() * 2.0)
                    .setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()))
                    .setLife((int) ((Math.random() * 5) + 5) * 1000)
                    .setMass(Math.random() * 10);
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
        double speed = (double) p.getAttribute("accStep", 2.0);

        if (getKeyPressed(KeyEvent.VK_LEFT)) {
            p.ax = -speed;
        }
        if (getKeyPressed(KeyEvent.VK_RIGHT)) {
            p.ax = speed;
        }
        if (getKeyPressed(KeyEvent.VK_UP)) {
            p.ay = 4.0 * -speed;
        }
        if (getKeyPressed(KeyEvent.VK_DOWN)) {
            p.ay = speed;
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
        // update active camera
        if (Optional.ofNullable(activeCamera).isPresent()) {
            activeCamera.update(elapsed);
        }
    }

    private void updateEntity(Entity e, double elapsed) {
        applyPhysicRuleToEntity(e, elapsed);
        constrainsEntity(e);
    }

    private void applyPhysicRuleToEntity(Entity e, double elapsed) {
        elapsed *= 0.4;

        e.dx += 0.5 * (e.ax * elapsed);
        e.dy += 0.5 * (e.ay + (e.mass * -world.gravity) * elapsed);

        e.dx *= e.friction;
        e.dy *= e.friction;

        e.setX(e.x + e.dx);
        e.setY(e.y + e.dy);

        /*
         * e.ax = 0.0;
         * e.ay = 0.0;
         */
    }

    private void constrainsEntity(Entity e) {
        constrainToWorld(e, world);
    }

    private void constrainToWorld(Entity e, World world) {
        if (e.getX() <= 0.0) {
            e.setX(0.0);
            e.dx *= -e.dx * e.elasticity;
        }
        if (e.getY() <= 0.0) {
            e.setY(0.0);
            e.dy *= -e.dy * e.elasticity;
        }
        if (e.getX() + e.getWidth() >= world.area.getWidth()) {
            e.setX(world.area.getWidth() - e.getWidth());
            e.dx *= -e.dx * e.elasticity;
        }
        if (e.getY() + e.getHeight() >= world.area.getHeight()) {
            e.setY(world.area.getHeight() - e.getHeight());
            e.dy *= -e.dy * e.elasticity;
        }
    }

    private void draw() {
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, (int) width, (int) height);
        gPipeline.stream().filter(e -> e.isAlive() || e.life == -1)
                .forEach(e -> {
                    if (Optional.ofNullable(activeCamera).isPresent()) {
                        moveCamera(g, activeCamera, 1);
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
                    if (Optional.ofNullable(activeCamera).isPresent()) {
                        moveCamera(g, activeCamera, -1);
                    }
                });
        g.dispose();
        renderToScreen();
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
        g.translate(cam.getX() * direction, cam.getY() * direction);

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