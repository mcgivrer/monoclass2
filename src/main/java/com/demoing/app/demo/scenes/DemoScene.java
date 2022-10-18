package com.demoing.app.demo.scenes;

import com.demoing.app.core.Application;
import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.gfx.Window;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.scene.AbstractScene;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.material.Material;
import com.demoing.app.core.entity.*;
import com.demoing.app.demo.scenes.behaviors.EnemyOnCollisionBehavior;
import com.demoing.app.demo.scenes.behaviors.PlayerOnCollisionBehavior;
import com.demoing.app.demo.scenes.behaviors.RainParticleGeneratorBehavior;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.demoing.app.core.service.physic.PhysicType.STATIC;
import static com.demoing.app.core.entity.EntityType.*;

public class DemoScene extends AbstractScene {


    private boolean gameOver;

    public DemoScene(String name) {
        super(name);
    }

    @Override
    public void prepare() {
        super.prepare();
    }

    @Override
    public boolean create(Application app) throws IOException, FontFormatException {

        gameOver = false;

        // define default world friction (air resistance ?)
        world = app.getPhysicEngine().getWorld();
        world.setMaterial(
                new Material(
                        "world",
                        1.0,
                        0.001,
                        1.0));

        // define Game global variables
        app.setAttribute("life", 5);
        app.setAttribute("score", 0);
        app.setAttribute("time", (long) 3 * (60 * 1000));

        Material matFloor = new Material("floor_mat", 1.0, 0.05, 0.20);
        Entity floor = new Entity("floor")
                .setType(RECTANGLE)
                .setPhysicType(STATIC)
                .setColor(Color.LIGHT_GRAY)
                .setPosition(32, world.area.getHeight() - 16)
                .setSize(world.area.getWidth() - 64, 16)
                .setCollisionBox(0, 0, 0, 0)
                .setMaterial(matFloor)
                .setMass(10000);
        app.addEntity(floor);

        Entity opf1 = new Entity("outPlatform_1")
                .setType(RECTANGLE)
                .setPhysicType(STATIC)
                .setColor(Color.YELLOW)
                .setPosition(world.area.getWidth() - 48, world.area.getHeight() - 8)
                .setSize(48, 8)
                .setCollisionBox(0, 0, 0, 0)
                .setMaterial(matFloor)
                .setMass(10000)
                .setAttribute("dead", true);
        app.addEntity(opf1);

        Entity opf2 = new Entity("outPlatform_2")
                .setType(RECTANGLE)
                .setPhysicType(STATIC)
                .setColor(Color.YELLOW)
                .setPosition(0, world.area.getHeight() - 8)
                .setSize(48, 8)
                .setCollisionBox(0, 0, 0, 0)
                .setMaterial(matFloor)
                .setMass(10000)
                .setAttribute("dead", true);
        app.addEntity(opf2);

        generateAllPlatforms(app, 15);


        // Create an Influencer in the initialized app World
        Influencer i1 = (Influencer) new Influencer("iflu_1_water")
                .setForce(new Vec2d(0.0, -0.19))
                .setType(RECTANGLE)
                .setPriority(5)
                .setMaterial(new Material("water", 0.998, 0, 0.998))
                .setPosition(0.0, world.area.getHeight() - 200.0)
                .setSize(world.area.getWidth(), 200.0)
                .setPhysicType(PhysicType.NONE)
                .setColor(new Color(0.2f, 0.5f, 0.7f, .90f));
        app.addEntity(i1);

        // A main player Entity.
        Entity player = new Entity("player")
                .setType(IMAGE)
                .setPosition(
                        world.area.getWidth() * 0.5,
                        world.area.getHeight() * 0.5)
                .setSize(32.0, 32.0)
                .setMaterial(
                        new Material("player_mat", 1.0, 0.3, 0.98))
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
                .addBehavior(new PlayerOnCollisionBehavior(this));
        app.addEntity(player);

        // Test particle system to simulate rain.
        ParticleSystem ps = (ParticleSystem) new ParticleSystem("rain")
                .addParticleGenerator(new RainParticleGeneratorBehavior(world))
                .setColor(Color.WHITE)
                .setType(NONE);
        app.addEntity(ps);

        Camera cam = new Camera("cam01")
                .setViewport(new Rectangle2D.Double(0, 0, app.config.screenWidth, app.config.screenHeight))
                .setTarget(player)
                .setTweenFactor(0.005);
        app.render.addCamera(cam);

        // Add some AMBIENT light
        Light la01 = (Light) new Light("ambientLight")
                .setLightType(LightType.AMBIENT)
                .setEnergy(0.2)
                .setPriority(10)
                .setStickToCamera(true)
                .setPosition(0.0, 0.0)
                .setSize(cam.getViewport().getWidth(), cam.getViewport().getHeight())
                .setColor(new Color(0.4f, 1.0f, 0.1f, 0.9f));
        app.addEntity(la01);

        // Add some SPHERICAL light
        generateLights(app, 20,
                cam.getViewport().getWidth(),cam.getViewport().getHeight(),
                30.0,30.0,
                0.8,
                0.05);

        // Add some enemy's balls
        generateEntity(app, "ball_", 20, 1.5);


        // ---- Everything about HUD -----

        // Score Display
        createHUD(
            app,
            player,
            Map.of(
            "ball_", Color.RED,
            "player", Color.BLUE,
            "pf_", Color.LIGHT_GRAY,
            "floor", Color.GRAY,
            "outPlatform", Color.YELLOW,
            "default", Color.GRAY));
        return true;
    }

    private void generateLights(Application app,
                                int nbLights,
                                double widthArea, double heightArea,
                                double widthSize, double heightSize,
                                double energy,
                                double glitterEffect) {
        boolean switchFront = false;
        for (int i = 0; i < nbLights; i++) {
            switchFront = !switchFront;
            Light l = (Light) new Light("sphericalLight_" + i)
                    .setLightType(LightType.SPHERICAL)
                    .setEnergy(Math.random() * energy)
                    .setGlitterEffect(Math.random() * glitterEffect)
                    .setStickToCamera(false)
                    .setColor(new Color(0.0f, 0.7f, 0.5f, 0.85f))
                    .setPosition(Math.random() * widthArea, Math.random() * heightArea)
                    .setSize(widthSize, heightSize)
                    .setPriority(switchFront ? 10 : 0);
            app.addEntity(l);
        }
    }

    private void generateAllPlatforms(Application app, int nbPf) {
        java.util.List<Entity> platforms = new ArrayList<>();
        Material matPF = new Material("matPF", 1.0, 0.2, 0.20);
        Entity pf = null;
        boolean found;
        int attempt = 0;
        for (int i = 0; i < nbPf; i++) {
            found = false;
            while (attempt < 10 && !found) {
                pf = createOnePlatform(app, i, matPF);
                for (Entity p : platforms) {
                    if (p.cbox.intersects(pf.cbox.getBounds())) {
                        found = true;
                    }
                }
                attempt += 1;
            }
            platforms.add(pf);
            app.addEntity(pf);
        }

    }

    private Entity createOnePlatform(Application app, int i, Material matPF) {
        double pfWidth = ((int) (Math.random() * 5) + 4);
        double maxCols = (world.area.getWidth() / 16.0);
        // 48=height of 1 pf + 1 player, -(3 + 3) to prevent create platform too low and
        // too high
        double maxRows = (world.area.getHeight() / 48) - 6;
        double pfCol = (int) (Math.random() * (maxCols * (Math.random() > 0.5 ? 1.0 : 0.75)));
        pfCol = pfCol < maxCols ? pfCol : maxRows - pfWidth;
        double pfRow = (int) ((Math.random() * maxRows) + 2);

        return new Entity("pf_" + i)
                .setType(RECTANGLE)
                .setPhysicType(STATIC)
                .setColor(new Color(0.7f, 0.7f, 0.7f, 1.0f))
                .setPosition(
                        pfCol * 16,
                        pfRow * 48)
                .setSize(pfWidth * 16, 16)
                .setCollisionBox(0, 0, 0, 0)
                .setMaterial(matPF)
                .setMass(10000)
                .setDuration(-1);
    }

    @Override
    public synchronized void update(Application app, double elapsed) {
        if (app.getEntities().containsKey("score") && app.getEntities().containsKey("player")) {
            Entity player = app.getEntity("player");

            // Update timer
            long time = (long) app.getAttribute("time", 0);
            time -= elapsed;
            time = time >= 0 ? time : 0;
            app.setAttribute("time", time);

            // display timer
            ValueEntity timeTxt = (ValueEntity) app.getEntity("time");
            timeTxt.setValue((int) (time / 1000));

            // if time=0 => game over !
            if (time == 0 && !gameOver) {
                gameOver(app, player);
            }

            // update score
            int score = (int) app.getAttribute("score", 0);
            ValueEntity scoreEntity = (ValueEntity) app.getEntity("score");
            scoreEntity.setValue(score);

            int life = (int) app.getAttribute("life", 0);
            ValueEntity lifeEntity = (ValueEntity) app.getEntity("life");
            lifeEntity.setValue(life);

            int energy = (int) player.getAttribute("energy", 0);
            GaugeEntity energyEntity = (GaugeEntity) app.getEntity("energy");
            energyEntity.setValue(energy);

            int mana = (int) player.getAttribute("mana", 0);
            GaugeEntity manaEntity = (GaugeEntity) app.getEntity("mana");
            manaEntity.setValue(mana);

            if (energy <= 0 && life <= 0 && !gameOver) {
                gameOver(app, player);
            }
        }
    }

    private void gameOver(Application app, Entity player) {
        player.activateAnimation("dead");
        TextEntity youAreDead = (TextEntity) app.getEntities().get("YouAreDead");
        youAreDead.setInitialDuration(-1);
        gameOver = true;
    }

    @Override
    public void input(Application app) {
        Window win = app.getWindow();
        Entity p = app.getEntity("player");
        if (Optional.ofNullable(p).isPresent()) {
            double speed = (double) p.getAttribute("accStep", 0.05);
            double jumpFactor = (double) p.getAttribute("jumpFactor", 12.0);
            boolean action = (boolean) p.getAttribute("action", false);
            if (win.isCtrlPressed()) {
                speed *= 2;
            }
            if (win.isShiftPressed()) {
                speed *= 4;
            }
            p.activateAnimation("idle");
            if (win.isKeyPressed(KeyEvent.VK_LEFT)) {
                p.activateAnimation("walk");
                p.forces.add(new Vec2d(-speed, 0.0));
                action = true;
            }
            if (win.isKeyPressed(KeyEvent.VK_RIGHT)) {
                p.activateAnimation("walk");
                p.forces.add(new Vec2d(speed, 0.0));
                action = true;
            }
            if (win.isKeyPressed(KeyEvent.VK_UP)) {
                p.activateAnimation("jump");
                p.forces.add(new Vec2d(0.0, -jumpFactor * speed));
                action = true;
            }
            if (win.isKeyPressed(KeyEvent.VK_DOWN)) {
                p.forces.add(new Vec2d(0.0, speed));
                action = true;
            }

            if (!action) {
                p.vel.x *= (p.friction);
                p.vel.x *= (p.friction);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Behavior> getBehaviors() {
        return behaviors;
    }

    @Override
    public List<Light> getLights() {
        return lights;
    }

    @Override
    public void dispose() {
        // todo release all resources captured by the scene.
    }

    private void generateEntity(Application app, String namePrefix, int nbEntity, double acc) {

        Material matEnt = new Material("matEnt", 1.0, 0.65, 0.98);

        for (int i = 0; i < nbEntity; i++) {
            Entity e = new Entity(namePrefix + Entity.getEntityIndex())
                    .setType(ELLIPSE)
                    .setSize(8, 8)
                    .setPosition(Math.random() * world.area.getWidth(),
                            Math.random() * (world.area.getHeight() - 48))
                    .setColor(Color.RED)
                    .setInitialDuration((int) ((Math.random() * 5) + 5) * 5000)
                    .setMaterial(matEnt)
                    .setMass(30.0)
                    .setPriority(2)
                    // player will loose 1 point of energy.
                    .setAttribute("hurt", 1)
                    // player can win 10 to 50 points
                    .setAttribute("points", (int) (10 + (Math.random() * 4)) * 10)
                    .addBehavior(new EnemyOnCollisionBehavior());
            app.addEntity(e);
        }
    }

}
