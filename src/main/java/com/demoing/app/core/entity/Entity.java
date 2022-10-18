package com.demoing.app.core.entity;

import com.demoing.app.core.behavior.Behavior;
import com.demoing.app.core.service.collision.CollisionDetector;
import com.demoing.app.core.gfx.Animation;
import com.demoing.app.core.service.render.Renderer;
import com.demoing.app.core.math.*;
import com.demoing.app.core.service.physic.material.DefaultMaterial;
import com.demoing.app.core.service.physic.material.Material;
import com.demoing.app.core.service.physic.PhysicEngine;
import com.demoing.app.core.service.physic.PhysicType;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.demoing.app.core.entity.EntityType.IMAGE;
import static com.demoing.app.core.entity.EntityType.RECTANGLE;

/**
 * Definition for all {@link Entity} managed by the small game framework.
 * A lot of attributes and
 * <a href="https://en.wikipedia.org/wiki/Fluent_interface#Java">Fluent API</a>
 * methods
 * to ease the Entity initialization.
 * <p>
 * Support some {@link PhysicEngine}, {@link Renderer}, {@link Animation}, and
 * {@link CollisionDetector} information,
 * mandatory attributes for multiple services.
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Entity {
    /**
     * internal counter for entity id.
     */
    public static long entityIndex = 0;
    public boolean collide;
    // id & naming attributes
    public long id = entityIndex++;
    public String name = "entity_" + id;

    public List<Entity> colliders = new CopyOnWriteArrayList<>();

    // Rendering attributes
    private int layer;
    public int priority;
    public EntityType type = RECTANGLE;
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
    public PhysicType physicType = PhysicType.DYNAMIC;
    public Vec2d vel = new Vec2d(0.0, 0.0);
    public Vec2d acc = new Vec2d(0.0, 0.0);
    public double mass = 1.0;

    public Material material = DefaultMaterial.DEFAULT.get();

    public double elasticity = 1.0, friction = 1.0;

    // internal attributes
    public int startDuration = -1;
    public int duration = -1;
    public Map<String, Object> attributes = new ConcurrentHashMap<>();

    public Map<String, List<Behavior>> behaviors = new ConcurrentHashMap<>();
    private Color shadowColor = Color.BLACK;

    private List<Entity> child = new ArrayList<>();


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
        this.type = IMAGE;
        setSize(img.getWidth(), img.getHeight());
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

    public Entity setMaterial(Material m) {
        this.material = m;
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

    public Entity addAnimation(String key, int x, int y, int tw, int th, int[] durations, String pathToImage,
                               int loop) {
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

    /**
     * Add a behavior to this entity.
     *
     * @param b the behavior to be added to the Entity
     * @return the updated Entity.
     * @since 1.0.4
     */
    public Entity addBehavior(Behavior b) {
        this.addBehavior(b.filterOnEvent(), b);
        b.initialization(this);
        return this;
    }

    /**
     * Retrieve the internal entity Index current value.
     *
     * @return
     */
    public static long getEntityIndex() {
        return entityIndex;
    }

    /**
     * Add a child entity to this entity.
     *
     * @param e
     * @return
     */
    public Entity addChild(Entity e) {
        child.add(e);
        return this;
    }

    /**
     * Return the child entities
     *
     * @return a list of Entity.
     */
    public List<Entity> getChild() {
        return child;
    }

    /**
     * Add a behavior on a specific Behavior event type to the Entity.
     *
     * @param behaviorEventType one the of possible event type {@link Behavior#ON_UPDATE_ENTITY}, {@link Behavior#ON_UPDATE_SCENE}, {@link Behavior#ON_COLLISION}
     * @param b                 the required Behavior to be applied according to the behavior event type (see {@link Behavior}).
     * @return the updated Entity.
     */
    public Entity addBehavior(String behaviorEventType, Behavior b) {
        behaviors.compute(behaviorEventType, (s, behaviorList) -> behaviorList == null ? new ArrayList<Behavior>() : behaviorList).add(b);

        return this;
    }

    /**
     * Return the list of Behavior corresponding to the requested <code>eventType</code>.
     *
     * @param eventType one the of possible event type {@link Behavior#ON_UPDATE_ENTITY}, {@link Behavior#ON_UPDATE_SCENE}, {@link Behavior#ON_COLLISION}
     * @return the updated Entity.
     */
    public List<Behavior> getBehaviors(String eventType) {
        return behaviors.get(eventType);
    }

    /**
     * Add a force to the Entity.
     *
     * @param dx
     * @param dy
     * @return the updated Entity.
     */
    public Entity addForce(double dx, double dy) {
        this.forces.add(new Vec2d(dx, dy));
        return this;
    }

    /**
     * Set  layer for this Entity.
     *
     * @param l an integer value defining the layer number to attach this entity to.
     * @return the updated Entity.
     */
    public Entity setLayer(int l) {
        this.layer = l;
        return this;
    }
}
