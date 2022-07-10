package com.demoing.app.core.service.render;

import com.demoing.app.core.Application;
import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.entity.*;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.utils.Logger;
import com.demoing.app.core.utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@link Render} service will provide the drawing process to  display entities to the {@link Application}
 * display buffer,  and then copy the buffer to the application window (see {@link JFrame}.
 */
public class Render {
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
    private Camera activeCamera;
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
            Logger.log(Logger.ERROR, this.getClass(), "ERR: Unable to initialize Render: " + e.getLocalizedMessage());
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
        gPipeline.stream()
                .filter(e -> !(e instanceof Light)
                        && e.isAlive() || e.isPersistent())
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
                        // This is a MapEntity
                        case MapEntity me -> {
                            drawMapEntity(g, me);
                        }
                        // This is an Influencer
                        case Influencer ie -> {
                            drawInfluencer(g, ie);
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

    private void drawInfluencer(Graphics2D g, Influencer ie) {
        drawEntity(g, ie);
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
                g.setColor(
                        e.collide && e.physicType == PhysicType.DYNAMIC
                        ? new Color(1.0f, 0.0f, 0.0f, 0.4f)
                        : new Color(0.0f, 0.0f, 1.0f, 0.3f));
                g.fill(e.cbox);
                if (config.debugObjectFilter.contains(e.name) && config.debug > 2) {
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
                        if(Optional.ofNullable(e.material).isPresent()) {
                            g.drawString(String.format(Locale.ROOT, "mat[e:%03.2f f:%03.2f]", e.elasticity, e.friction), offsetX,
                                    offsetY + (lineHeight * 6));
                        }
                        if (e.getAnimations()) {
                            g.drawString(String.format("anim:%s/%d",
                                            e.animations.currentAnimationSet,
                                            e.animations.currentFrame),
                                    offsetX, offsetY + (lineHeight * 7));
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
        JFrame frame = app.getFrame();
        Graphics2D g2 = (Graphics2D) frame.getBufferStrategy().getDrawGraphics();
        g2.drawImage(
                buffer,
                0, 0, (int) frame.getWidth(), (int) frame.getHeight(),
                0, 0, (int) config.screenWidth, (int) config.screenHeight,
                null);
        drawDebugString(g2, realFps);
        g2.dispose();
        frame.getBufferStrategy().show();
    }

    public void drawDebugString(Graphics2D g, double realFps) {
        if (config.debug > 0) {
            g.setFont(debugFont.deriveFont(16.0f));
            g.setColor(Color.WHITE);
            g.drawString(
                    String.format(
                            "[ dbg: %d | fps:%3.0f | obj:%d | {g:%1.03f, a(%3.0fx%3.0f) }]",
                            config.debug,
                            realFps,
                            gPipeline.size(),
                            world.gravity.y * 1000.0,
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
        String filename = String.format("%s/screenshots/%s-%d.png", path, System.nanoTime(), i);

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectory(targetDir);
            }
            File out = new File(filename);
            ImageIO.write(buffer, "PNG", out);

            System.out.printf(" Write screenshot to %s\n", filename);
        } catch (IOException e) {
            Logger.log(Logger.ERROR, this.getClass(), "Unable to write screenshot to %s: %s", filename, e.getMessage());
        }
    }

    public Camera getActiveCamera() {
        return activeCamera;
    }


    public long getRenderingTime() {
        return renderingTime;
    }

    public List<Entity> getgPipeline() {
        return gPipeline;
    }
}
