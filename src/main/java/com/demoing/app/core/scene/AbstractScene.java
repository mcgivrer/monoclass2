package com.demoing.app.core.scene;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.io.Resources;
import com.demoing.app.core.service.physic.World;
import com.demoing.app.core.entity.*;
import com.demoing.app.core.utils.I18n;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.demoing.app.core.entity.TextAlign.CENTER;

/**
 * The {@link AbstractScene} provide all the default needed resources and Entities to display a Scene.
 *
 * @author Frédéric Delorme
 * @since 1.0.6
 */
public abstract class AbstractScene implements Scene {
    /**
     * internal name for that {@link Scene}.
     */
    protected final String name;
    /**
     * Specific Scene behaviors.
     */
    public Map<String, List<Behavior>> behaviors = new ConcurrentHashMap<>();
    /**
     * The list of Light manage by this scene
     */
    protected List<Light> lights = new ArrayList<>();

    /**
     * World object to be used with PhysicEngine (if needed)
     */
    protected World world;

    /**
     * A list of specific figures to be displayed on screen (for score, life, etc...)
     */
    protected BufferedImage[] figs;

    /**
     * Some font for displayed messages.
     */
    protected Font messagesFont;

    /**
     * create the {@link AbstractScene} with its name.
     *
     * @param name the name of the Scene to be instantiated.
     */
    public AbstractScene(String name) {
        this.name = name;
    }


    @Override
    public void prepare() {
        // prepare the Figures for score rendering
        prepareFigures("/images/tiles01.png");
        messagesFont = Resources.loadFont("/fonts/FreePixel.ttf")
                .deriveFont(12.0f);
    }

    @Override
    public abstract boolean create(Application app) throws Exception;


    protected void createHUD(Application app, Entity player, Map<String, Color> colorMapping) {
        int score = (int) app.getAttribute("score", 0);

        ValueEntity scoreEntity = (ValueEntity) new ValueEntity("score")
                .setValue(score)
                .setFormat("%06d")
                .setFigures(figs)
                .setPosition(20, 20)
                .setSize(6 * 8, 16)
                .setStickToCamera(true)
                .setLayer(1);
        app.addEntity(scoreEntity);

        long time = (long) app.getAttribute("time", 0);
        Font timeFont = messagesFont.deriveFont(16.0f);
        ValueEntity timeTxtE = (ValueEntity) new ValueEntity("time")
                .setFormat("%3d")
                .setValue((int) (time / 1000))
                .setFigures(figs)
                .setSize(3 * 8, 16)
                .setPosition(app.config.screenWidth / 2, 20)
                .setStickToCamera(true)
                .setLayer(1);
        app.addEntity(timeTxtE);

        ValueEntity lifeTxt = (ValueEntity) new ValueEntity("life")
                .setValue(5)
                .setFigures(figs)
                .setSize(8, 16)
                .setPosition(app.config.screenWidth - 40, 20)
                .setPriority(2)
                .setStickToCamera(true)
                .setLayer(1);
        app.addEntity(lifeTxt);

        GaugeEntity energyGauge = (GaugeEntity) new GaugeEntity("energy")
                .setMax(100.0)
                .setMin(0.0)
                .setValue((int) player.getAttribute("energy", 100.0))
                .setColor(Color.RED)
                .setSize(32, 6)
                .setPriority(2)
                .setLayer(1)
                .setPosition(app.config.screenWidth - 40 - 4 - 32, 30);
        app.addEntity(energyGauge);

        GaugeEntity manaGauge = (GaugeEntity) new GaugeEntity("mana")
                .setMax(100.0)
                .setMin(0.0)
                .setValue((int) player.getAttribute("mana", 100.0))
                .setColor(Color.BLUE)
                .setShadow(Color.BLACK)
                .setSize(32, 6)
                .setPriority(2)
                .setLayer(1)
                .setPosition(app.config.screenWidth - 40 - 4 - 32, 20);
        app.addEntity(manaGauge);

        // Add a Map display
        MapEntity mapEntity = (MapEntity) new MapEntity("map")
                .setColorMapping(
                        colorMapping)
                .setRefEntities(app.getEntities().values().stream().toList())
                .setWorld(world)
                .setSize(48, 32)
                .setPosition(8, app.config.screenHeight - 40)
                .setLayer(1);
        app.addEntity(mapEntity);

        // ---- Everything about Messages ----

        // A welcome Text
        TextEntity welcomeMsg = (TextEntity) new TextEntity("welcome")
                .setText(I18n.get("app.message.welcome"))
                .setAlign(CENTER)
                .setFont(messagesFont)
                .setBackgroundOffset(8)
                .setShadowOffset(4, 3)
                .setBorderColor(Color.LIGHT_GRAY)
                .setColor(Color.WHITE)
                .setShadow(new Color(0.0f, 0.0f, 0.0f, 0.5f))
                .setPosition(
                        app.config.screenWidth * 0.5,
                        app.config.screenHeight * 0.8)
                .setInitialDuration(5000)
                .setBackgroundColor(new Color(0.0f, 0.0f, 0.6f, 0.7f))
                .setPriority(5)
                .setLayer(0)
                .setStickToCamera(true);
        app.addEntity(welcomeMsg);


        // A welcome Text
        TextEntity helpMsg = (TextEntity) new TextEntity("help")
                .setText(I18n.get("app.message.help"))
                .setAlign(TextAlign.LEFT)
                .setFont(messagesFont)
                .setBackgroundOffset(8)
                .setShadowOffset(4, 3)
                .setBorderColor(Color.LIGHT_GRAY)
                .setColor(Color.WHITE)
                .setShadow(new Color(0.0f, 0.0f, 0.0f, 0.5f))
                .setPosition(
                        app.config.screenWidth * 0.2,
                        app.config.screenHeight * 0.5)
                .setBackgroundColor(new Color(0.0f, 0.0f, 0.6f, 0.7f))
                .setPriority(0)
                .setLayer(0)
                .setStickToCamera(true)
                .setActive(false);
        app.addEntity(helpMsg);

        // You are dead Text
        TextEntity youAreDeadTxt = (TextEntity) new TextEntity("YouAreDead")
                .setText(I18n.get("app.player.dead"))
                .setAlign(CENTER)
                .setFont(messagesFont)
                .setPosition(
                        app.config.screenWidth * 0.5,
                        app.config.screenHeight * 0.8)
                .setColor(Color.WHITE)
                .setInitialDuration(0)
                .setPriority(20)
                .setLayer(1)
                .setStickToCamera(true);
        app.addEntity(youAreDeadTxt);


        app.getActionHandler().actionMapping.put(KeyEvent.VK_H, (e) -> {
            boolean active = !app.getEntity("help").isActive();
            app.getEntity("help").setActive(active);
            return this;
        });
    }

    @Override
    public abstract void update(Application app, double elapsed);

    @Override
    public void input(Application app) {
        getBehaviors(Behavior.ON_INPUT_SCENE).stream()
                .filter(e ->
                        e.filterOnEvent().contains(Behavior.ON_INPUT_SCENE))
                .forEach(b -> {
                    b.input(app, this);
                });
    }

    @Override
    public abstract String getName();

    @Override
    public List<Behavior> getBehaviors(String eventType) {
        return behaviors.get(eventType);
    }

    @Override
    public List<Light> getLights() {
        return null;
    }

    @Override
    public void dispose() {

    }

    private void prepareFigures(String pathToImage) {
        BufferedImage figuresImage = Resources.loadImage(pathToImage);
        figs = new BufferedImage[10];
        for (int i = 0; i < 10; i++) {
            figs[i] = figuresImage.getSubimage(i * 8, 4 * 16, 8, 16);
        }
    }

    public Scene addBehavior(Behavior b) {
        this.addBehavior(b.filterOnEvent(), b);
        b.initialization(this);
        return this;
    }


    protected Scene addBehavior(String behaviorEventType, Behavior b) {
        behaviors.compute(
                behaviorEventType,
                (s, behaviorList) ->
                        behaviorList == null ?
                                new ArrayList<Behavior>() :
                                behaviorList).add(b);

        return this;
    }
}
