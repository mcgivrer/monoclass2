package com.demoing.app.core.service.render;

import com.demoing.app.core.Application;
import com.demoing.app.core.config.Configuration;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.entity.Camera;
import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.service.render.plugins.*;
import com.demoing.app.core.utils.Logger;
import com.demoing.app.core.utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@link Renderer} service will provide the drawing process to  display entities to the {@link Application}
 * display buffer,  and then copy the buffer to the application window (see {@link JFrame}.
 */
public class Renderer {

    /**
     * The World object defining the play area limit.
     */
    private final World world;
    private final com.demoing.app.core.gfx.Window window;
    private final Configuration config;

    /**
     * The internal rendering graphics buffer.
     */
    private BufferedImage buffer;
    /**
     * The debug font to be used to display debug level information.
     */
    private Font debugFont;
    /**
     * Internal metric to measure rendering time.
     */
    public long renderingTime = 0;

    /**
     * The list of rendering plugin to be used to render all the type of entities.
     */
    private final Map<Class<? extends Entity>, RenderPlugin<Entity>> plugins = new ConcurrentHashMap<>();

    /**
     * The list of object to be rendered: the rendering pipeline.
     */
    private final List<Entity> gPipeline = new CopyOnWriteArrayList<>();

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
     * @param app   the parent application of this Render service.
     * @param world the World object defining the play area limits.
     */
    public Renderer(Application app, World world) {
        this.config = app.getConfiguration();
        this.window = app.getWindow();
        this.world = world;
        buffer = new BufferedImage((int) config.screenWidth, (int) config.screenHeight,
                BufferedImage.TYPE_INT_ARGB);
        try {
            debugFont = Font.createFont(
                            Font.PLAIN,
                            Objects.requireNonNull(this.getClass().getResourceAsStream("/fonts/FreePixel.ttf")))
                    .deriveFont(9.0f);
        } catch (FontFormatException | IOException e) {
            Logger.log(Logger.ERROR, this.getClass(), "Unable to initialize Renderer: {0}",e.getLocalizedMessage());
        }

        addPlugin(new TextRenderPlugin());
        addPlugin(new GaugeRenderPlugin());
        addPlugin(new MapRenderPlugin());
        addPlugin(new ValueRenderPlugin());
        addPlugin(new LightRenderPlugin());
        addPlugin(new EntityRenderPlugin());
        addPlugin(new ParticleSystemRenderPlugin());
        addPlugin(new InfluencerRenderPlugin());
        addPlugin(new TileMapRenderPlugin());


    }

    /**
     * Add a new Rendering plugin to provide new rendering capabilities for {@link Entity} inheritance.
     *
     * @param renderPlugin the new {@link RenderPlugin} implementation to serve new rendering needs.
     * @return the updated {@link Renderer} system.
     */
    public Renderer addPlugin(RenderPlugin renderPlugin) {
        plugins.put(renderPlugin.getRegisteringClass(), renderPlugin);
        return this;
    }

    /**
     * Drawing all object in the {@link Renderer#gPipeline}, according to the priority
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
                .filter(e -> e.isAlive() || e.isPersistent())
                .forEach(e -> drawEntity(g, e));
        g.dispose();
        renderToScreen(realFps);
        renderingTime = System.nanoTime() - startTime;
    }

    private void drawEntity(Graphics2D g, Entity e) {
        if (e.isNotStickToCamera()) {
            moveCamera(g, activeCamera, -1);
        }
        if (plugins.containsKey(e.getClass())) {
            plugins.get(e.getClass()).draw(this, g, e);
        } else {
            Logger.log(Logger.ERROR,
                this.getClass(),
                "No RenderPlugin implementation found for %s.",
                e.getClass().toString());
        }
        drawDebugInfo(g, e);
        if (e.isNotStickToCamera()) {
            moveCamera(g, activeCamera, 1);
        }
        // Draw all child entities.
        e.getChild().forEach(ce -> drawEntity(g, ce));
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
                        if (Optional.ofNullable(e.material).isPresent()) {
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
        float ratio;
        if (e.isPersistent()) {
            g.setColor(Color.ORANGE);
            ratio = 1.0f;
        } else {
            ratio = (1.0f * e.duration) / (1.0f * e.startDuration);
        }
        g.fillRect((int) e.pos.x, (int) e.pos.y - 4, (int) (32.0 * ratio), 2);
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
     * After the Buffer rendering operation performed in {@link Renderer#draw(long)},
     * the buffer is coped to the JFrame content.
     *
     * @param realFps the measured frame rate per seconds
     */
    public void renderToScreen(long realFps) {
        JFrame frame = window.getFrame();
        Graphics2D g2 = (Graphics2D) frame.getBufferStrategy().getDrawGraphics();
        g2.drawImage(
                buffer,
                0, 0, frame.getWidth(), frame.getHeight(),
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
                    20, window.getHeight() - 20);
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
            gPipeline.sort((o1, o2) -> o1.priority > o2.priority ? -1 : 1);
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

    public List<Entity> getGPipeline() {
        return gPipeline;
    }

    public Configuration getConfiguration() {
        return config;
    }
}
