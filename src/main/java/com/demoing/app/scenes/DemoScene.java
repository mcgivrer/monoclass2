package com.demoing.app.scenes;

import com.demoing.app.core.Application;
import com.demoing.app.core.Application.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.demoing.app.core.Application.EntityType.*;
import static com.demoing.app.core.Application.PhysicType.STATIC;
import static com.demoing.app.core.Application.TextAlign.CENTER;
import static com.demoing.app.core.Application.TextAlign.LEFT;

public class DemoScene implements Scene {
    private final String name;

    Font wlcFont;

    private Map<String, Behavior> behaviors = new ConcurrentHashMap<>();

    public DemoScene(String name) {
        this.name = name;
    }

    @Override
    public boolean create(Application app) throws IOException, FontFormatException {
        // define default world friction (air resistance ?)
        app.world.setFriction(0.98);
        // define Game global variables
        app.setAttribute("life", 5);
        app.setAttribute("score", 0);
        app.setAttribute("time", (long) (180 * 1000));

        Entity floor = new Entity("floor")
                .setType(RECTANGLE)
                .setPhysicType(STATIC)
                .setColor(Color.LIGHT_GRAY)
                .setPosition(32, app.world.area.getHeight() - 16)
                .setSize(app.world.area.getWidth() - 64, 16)
                .setCollisionBox(0, 0, 0, 0)
                .setElasticity(0.1)
                .setFriction(0.70)
                .setMass(10000);
        app.addEntity(floor);

        Entity opf1 = new Entity("outPlatform_1")
                .setType(RECTANGLE)
                .setPhysicType(STATIC)
                .setColor(Color.RED)
                .setPosition(app.world.area.getWidth() - 32, app.world.area.getHeight() - 8)
                .setSize(32, 8)
                .setCollisionBox(0, 0, 0, 0)
                .setElasticity(0.1)
                .setFriction(0.70)
                .setMass(10000)
                .setAttribute("dead", true);
        app.addEntity(opf1);

        Entity opf2 = new Entity("outPlatform_2")
                .setType(RECTANGLE)
                .setPhysicType(STATIC)
                .setColor(Color.RED)
                .setPosition(0, app.world.area.getHeight() - 8)
                .setSize(32, 8)
                .setCollisionBox(0, 0, 0, 0)
                .setElasticity(0.1)
                .setFriction(0.70)
                .setMass(10000)
                .setAttribute("dead", true);
        app.addEntity(opf2);

        generatePlatforms(app, 15);

        // A main player Entity.
        Entity player = new Entity("player")
                .setType(IMAGE)
                .setPosition(app.world.area.getWidth() * 0.5, app.world.area.getHeight() * 0.5)
                .setSize(32.0, 32.0)
                .setElasticity(0.0)
                .setFriction(0.98)
                .setColor(Color.RED)
                .setPriority(1)
                .setMass(40.0)
                .setCollisionBox(+4, -8, -4, -2)
                .setAttribute("energy", 100)
                .setAttribute("mana", 100)
                .setAttribute("accStep", 0.05)
                .addAnimation("idle",
                        0, 0,
                        32, 32,
                        new int[]{450, 60, 60, 250, 60, 60, 60, 450, 60, 60, 60, 250, 60},
                        "/images/sprites01.png", -1)
                .addAnimation("walk",
                        0, 32,
                        32, 32,
                        new int[]{60, 60, 60, 150, 60, 60, 60, 150},
                        "/images/sprites01.png", -1)
                .addAnimation("jump",
                        0, 5 * 32,
                        32, 32,
                        new int[]{60, 60, 250, 250, 60, 60},
                        "/images/sprites01.png", -1)
                .addAnimation("dead",
                        0, 7 * 32,
                        32, 32,
                        new int[]{160, 160, 160, 160, 160, 160, 500},
                        "/images/sprites01.png", 0)
                .activateAnimation("idle")
                .addBehavior(new Behavior() {

                    @Override
                    public String filterOnEvent() {
                        return onCollision;
                    }

                    @Override
                    public void onCollide(Application a, Entity e1, Entity e2) {
                        if (e2.name.contains("ball_")) {
                            reducePlayerEnergy(a, e1, e2);
                        }
                    }

                    @Override
                    public void update(Application a, Entity e, double d) {

                    }

                    public void update(Application a, double d) {

                    }
                });
        app.addEntity(player);

        Camera cam = new Camera("cam01")
                .setViewport(new Rectangle2D.Double(0, 0, app.config.screenWidth, app.config.screenHeight))
                .setTarget(player)
                .setTweenFactor(0.005);
        app.render.addCamera(cam);

        generateEntity(app, "ball_", 10, 2.5);

        wlcFont = Font.createFont(
                        Font.PLAIN,
                        Objects.requireNonNull(this.getClass().getResourceAsStream("/fonts/FreePixel.ttf")))
                .deriveFont(12.0f);

        // Score Display
        int score = (int) app.getAttribute("score", 0);
        Font scoreFont = wlcFont.deriveFont(16.0f);
        String scoreTxt = String.format("%06d", score);
        TextEntity scoreTxtE = (TextEntity) new TextEntity("score")
                .setText(scoreTxt)
                .setAlign(LEFT)
                .setFont(scoreFont)
                .setPosition(20, 30)
                .setColor(Color.WHITE)
                .setStickToCamera(true);
        app.addEntity(scoreTxtE);

        long time = (long) app.getAttribute("time", 0);
        Font timeFont = wlcFont.deriveFont(16.0f);
        String timeTxt = String.format("%02d:%02d", (int) (time / 60 * 1000), (int) (time % 60 * 1000));
        TextEntity timeTxtE = (TextEntity) new TextEntity("time")
                .setText(timeTxt)
                .setAlign(CENTER)
                .setFont(scoreFont)
                .setPosition(app.config.screenWidth / 2, 30)
                .setColor(Color.WHITE)
                .setStickToCamera(true);
        app.addEntity(timeTxtE);

        Font lifeFont = new Font("Arial", Font.PLAIN, 16);
        TextEntity lifeTxt = (TextEntity) new TextEntity("life")
                .setText("5")
                .setAlign(LEFT)
                .setFont(lifeFont)
                .setPosition(app.config.screenWidth - 40, 30)
                .setColor(Color.RED)
                .setPriority(10)
                .setStickToCamera(true);
        app.addEntity(lifeTxt);

        GaugeEntity energyGauge = (GaugeEntity) new GaugeEntity("energy")
                .setMax(100.0)
                .setMin(0.0)
                .setValue((int) player.getAttribute("energy", 100.0))
                .setColor(Color.RED)
                .setSize(32, 6)
                .setPriority(10)
                .setPosition(app.config.screenWidth - 40 - 4 - 32, 25);
        app.addEntity(energyGauge);

        GaugeEntity manaGauge = (GaugeEntity) new GaugeEntity("mana")
                .setMax(100.0)
                .setMin(0.0)
                .setValue((int) player.getAttribute("mana", 100.0))
                .setColor(Color.BLUE)
                .setSize(32, 6)
                .setPriority(10)
                .setPosition(app.config.screenWidth - 40 - 4 - 32, 15);
        app.addEntity(manaGauge);

        // A welcome Text
        TextEntity welcomeMsg = (TextEntity) new TextEntity("welcome")
                .setText(Application.I18n.get("app.message.welcome"))
                .setAlign(CENTER)
                .setFont(wlcFont)
                .setPosition(app.config.screenWidth * 0.5, app.config.screenHeight * 0.8)
                .setColor(Color.WHITE)
                .setInitialDuration(5000)
                .setPriority(20)
                .setStickToCamera(true);
        app.addEntity(welcomeMsg);

        // mapping of keys actions:

        app.actionHandler.actionMapping = Map.of(
                // reset the scene
                KeyEvent.VK_Z, o -> {
                    app.reset();
                    return this;
                },
                // manage debug level
                KeyEvent.VK_D, o -> {
                    app.config.debug = app.config.debug + 1 < 5 ? app.config.debug + 1 : 0;
                    return this;
                },
                // I quit !
                KeyEvent.VK_ESCAPE, o -> {
                    app.requestExit();
                    return this;
                },
                KeyEvent.VK_K, o -> {
                    Entity p = app.entities.get("player");
                    p.setAttribute("energy", 0);
                    return this;
                }
        );
        return true;
    }

    private void reducePlayerEnergy(Application app, Entity player, Entity e) {
        int hurt = (int) e.getAttribute("hurt", 0);
        int energy = (int) player.getAttribute("energy", 0);
        energy -= hurt;
        if (energy < 0) {
            int life = (int) app.getAttribute("life", 0);
            life -= 1;
            if (life < 0) {
                app.setAttribute("endOfGame", true);
                player.setDuration(0);
            } else {
                app.setAttribute("life", life);
                player.setAttribute("energy", 100);
            }
        } else {
            player.setAttribute("energy", energy -= hurt);
        }
    }

    private void generatePlatforms(Application app, int nbPf) {
        java.util.List<Entity> platforms = new ArrayList<>();
        Entity pf;
        boolean found = false;
        for (int i = 0; i < nbPf; i++) {
            while (true) {
                pf = createPlatform(app, i);
                found = false;
                for (Entity p : platforms) {
                    if (p.cbox.intersects(pf.cbox.getBounds())) {
                        found = true;
                    }
                }
                if (!found) {
                    break;
                }
            }
            platforms.add(pf);
            app.addEntity(pf);
        }

    }

    private Entity createPlatform(Application app, int i) {
        double pfWidth = ((int) (Math.random() * 5) + 4);
        double maxCols = (app.world.area.getWidth() / 16.0);
        // 48=height of 1 pf + 1 player, -(3 + 3) to prevent create platform too low and too high
        double maxRows = (app.world.area.getHeight() / 48) - 6;
        double pfCol = (int) (Math.random() * maxCols);
        pfCol = pfCol < maxCols ? pfCol : maxRows - pfWidth;
        double pfRow = (int) ((Math.random() * maxRows) + 3);

        Entity pf = new Entity("pf_" + i)
                .setType(RECTANGLE)
                .setPhysicType(STATIC)
                .setColor(Color.LIGHT_GRAY)
                .setPosition(
                        pfCol * 16,
                        pfRow * 48)
                .setSize(pfWidth * 16, 16)
                .setCollisionBox(0, 0, 0, 0)
                .setElasticity(0.1)
                .setFriction(0.70)
                .setMass(10000)
                .setDuration(-1);
        return pf;
    }

    @Override
    public synchronized void update(Application app, double elapsed) {
        if (app.entities.containsKey("score") && app.entities.containsKey("player")) {
            Entity player = app.getEntity("player");

            // Update timer
            long time = (long) app.getAttribute("time", 0);
            time -= elapsed;
            time = time >= 0 ? time : 0;
            app.setAttribute("time", time);

            // display timer
            TextEntity timeTxt = (TextEntity) app.getEntity("time");
            String timeStr = String.format("%02d:%02d", (int) (time / (60 * 1000)), (int) ((time % (60 * 1000)) / 1000));
            timeTxt.setText(timeStr);

            // if time=0 => game over !
            if (time == 0) {
                player.activateAnimation("dead");
                app.addEntity(new TextEntity("YouAreDead")
                        .setText(I18n.get("app.player.dead"))
                        .setAlign(CENTER)
                        .setFont(wlcFont)
                        .setPosition(app.config.screenWidth * 0.5, app.config.screenHeight * 0.8)
                        .setColor(Color.WHITE)
                        .setInitialDuration(-1)
                        .setPriority(20)
                        .setStickToCamera(true));
            }

            // update score
            int score = (int) app.getAttribute("score", 0);
            TextEntity scoreEntity = (TextEntity) app.getEntity("score");
            scoreEntity.setText(String.format("%06d", score));

            int life = (int) app.getAttribute("life", 0);
            TextEntity lifeEntity = (TextEntity) app.getEntity("life");
            lifeEntity.setText(String.format("%d", life));


            int energy = (int) player.getAttribute("energy", 0);
            GaugeEntity energyEntity = (GaugeEntity) app.getEntity("energy");
            energyEntity.setValue(energy);

            int mana = (int) player.getAttribute("mana", 0);
            GaugeEntity manaEntity = (GaugeEntity) app.getEntity("mana");
            manaEntity.setValue(mana);

            if (energy <= 0 && life <= 0) {
                player.activateAnimation("dead");
                app.addEntity(new TextEntity("YouAreDead")
                        .setText(I18n.get("app.player.dead"))
                        .setAlign(CENTER)
                        .setFont(wlcFont)
                        .setPosition(app.config.screenWidth * 0.5, app.config.screenHeight * 0.8)
                        .setColor(Color.WHITE)
                        .setInitialDuration(-1)
                        .setPriority(20)
                        .setStickToCamera(true));
            }
        }
    }

    @Override
    public void input(Application app) {
        Entity p = app.getEntity("player");
        if (Optional.ofNullable(p).isPresent()) {
            double speed = (double) p.getAttribute("accStep", 0.05);
            double jumpFactor = (double) p.getAttribute("jumpFactor", 12.0);
            boolean action = (boolean) p.getAttribute("action", false);
            if (app.isCtrlPressed()) {
                speed *= 2;
            }
            if (app.isShiftPressed()) {
                speed *= 4;
            }
            p.activateAnimation("idle");
            if (app.getKeyPressed(KeyEvent.VK_LEFT)) {
                p.activateAnimation("walk");
                p.forces.add(new Application.Vec2d(-speed, 0.0));
                action = true;
            }
            if (app.getKeyPressed(KeyEvent.VK_RIGHT)) {
                p.activateAnimation("walk");
                p.forces.add(new Application.Vec2d(speed, 0.0));
                action = true;
            }
            if (app.getKeyPressed(KeyEvent.VK_UP)) {
                p.activateAnimation("jump");
                p.forces.add(new Application.Vec2d(0.0, -jumpFactor * speed));
                action = true;
            }
            if (app.getKeyPressed(KeyEvent.VK_DOWN)) {
                p.forces.add(new Application.Vec2d(0.0, speed));
                action = true;
            }

            if (!action) {
                p.vel.x *= p.friction;
                p.vel.x *= p.friction;
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Application.Behavior> getBehaviors() {
        return behaviors;
    }

    @Override
    public void dispose() {
        //todo release all resources captured by the scene.
    }

    private void generateEntity(Application app, String namePrefix, int nbEntity, double acc) {
        for (int i = 0; i < nbEntity; i++) {
            Entity e = new Entity(namePrefix + Application.getEntityIndex())
                    .setType(ELLIPSE)
                    .setSize(8, 8)
                    .setPosition(Math.random() * app.world.area.getWidth(),
                            Math.random() * (app.world.area.getHeight() - 48))
                    .setColor(Color.RED)
                    .setInitialDuration((int) ((Math.random() * 5) + 5) * 5000)
                    .setElasticity(0.65)
                    .setFriction(0.98)
                    .setMass(5.0)
                    .setPriority(2)
                    // player will loose 1 point of energy.
                    .setAttribute("hurt", 1)
                    // player can win 10 to 50 points
                    .setAttribute("points", (int) (10 + (Math.random() * 4)) * 10)
                    .addBehavior(new Behavior() {
                        @Override
                        public String filterOnEvent() {
                            return "onCollision";
                        }

                        @Override
                        public void onCollide(Application a, Entity e1, Entity e2) {
                            // If hurt a dead attribute platform => Die !
                            if ((boolean) e2.getAttribute("dead", false) && e1.isAlive()) {
                                int score = (int) a.getAttribute("score", 0);
                                int points = (int) e1.getAttribute("points", 0);
                                a.setAttribute("score", score + points);
                                e1.setDuration(0);
                            }
                        }

                        @Override
                        public void update(Application a, Entity e, double d) {

                        }

                        public void update(Application a, double d) {
                        }
                    });
            app.addEntity(e);
        }
    }

}
