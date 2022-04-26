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

        public double ax = 0.0, ay = 0.0;
        public double dx = 0.0, dy = 0.0;
        public double mass = 1.0;
        public int priority;
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
            return (life > 0 || life == -1);
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

    private World world;

    public Application() throws IOException {
        appProps.load(this.getClass().getResourceAsStream("/app.properties"));
    }

    protected void run(String[] args) throws Exception {
        initialize(args);
        loop();
        dispose();
    }

    private void initialize(String[] args) throws IOException, FontFormatException {
        loadConfig(appProps);
        parseArgs(args);
        createWindow();
        buffer = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
        createScene();
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
        addKeyListener(this);
        pack();
        setVisible(true);
    }

    protected void createScene() throws IOException, FontFormatException {
        // A main player Entity.
        Entity player = new Entity("player")
                .setType(RECTANGLE)
                .setPosition(width * 0.5, height * 0.5)
                .setColor(Color.RED)
                .setPriority(1);
        addEntity(player);
        // A welcome text
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

    private void loop() {
        long previous = System.currentTimeMillis();
        while (!exit) {

            long start = System.currentTimeMillis();
            double elapsed = start - previous;

            input();
            update(elapsed);
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
        double speed = 2.0;
        Entity p = entities.get("player");

        boolean jumping = (boolean) p.getAttribute("jumping", false);

        if (getKeyPressed(KeyEvent.VK_LEFT)) {
            p.ax = -speed;
        }
        if (getKeyPressed(KeyEvent.VK_RIGHT)) {
            p.ax = speed;
        }
        if (getKeyPressed(KeyEvent.VK_UP) && !jumping) {
            p.ax = 4.0 * speed;
            p.setAttribute("jumping", true);
        }
        if (getKeyPressed(KeyEvent.VK_DOWN)) {
            p.ax = -speed;
        }

    }

    private void update(double elapsed) {
        entities.values().stream().forEach((e) -> {
            if (e.physicType.equals(PhysicType.DYNAMIC)) {
                updateEntity(e, elapsed);
            }
            if (e.isAlive()) {
                e.life -= elapsed;
            }
        });
    }

    private void updateEntity(Entity e, double elapsed) {
        applyPhysicRuleToEntity(e, elapsed);
        constrainsEntity(e);
    }

    private void constrainsEntity(Entity e) {
        constrainToWorld(e, world);
    }

    private void applyPhysicRuleToEntity(Entity e, double elapsed) {

        e.dx = 0.5 * (e.ax * elapsed);
        e.dy = 0.5 * (e.ay + e.mass * world.gravity) * elapsed;

        e.setX(e.x + e.dx);
        e.setY(e.y + e.dy);

        e.ax = 0.0;
        e.ay = 0.0;
    }

    private void constrainToWorld(Entity e, World world) {
        if (!world.area.contains(e)) {
            if (e.getX() < 0.0) {
                e.setX(0.0);
            }
            if (e.getY() < 0.0) {
                e.setY(0.0);
            }
            if (e.getMaxX() > world.area.getMaxX()) {
                e.setX(world.area.getMaxX() - e.getWidth());
            }
            if (e.getMaxY() > world.area.getMaxY()) {
                e.setX(world.area.getMaxY() - e.getHeight());
            }
        }
    }

    private void draw() {
        Graphics2D g = buffer.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, (int) width, (int) height);
        gPipeline.stream().filter(e -> e.isAlive())
                .forEach(e -> {
                    g.setColor(e.color);
                    switch (e) {
                        // this is a TextEntity
                        case TextEntity te -> {
                            g.setFont(te.font);
                            int size = g.getFontMetrics().stringWidth(te.text);
                            double offsetX = te.align.equals(TextAlign.RIGHT) ? -size : te.align.equals(TextAlign.CENTER) ? -size * 0.5 : 0;
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