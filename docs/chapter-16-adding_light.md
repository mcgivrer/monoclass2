# Lights

An new `Entity` inheriting component named `Light` must provide a way to simulate some light with different type:

- SPOT light,
- POINT light
- or AMBIANT light.

## Context

A `Scene` has one or more Entity objects to be processed. A new List mist be added to the scene , containing all the
Lights.

The `Render` must process those lights in a different rendering buffer to be able to apply color, lighten or darken
effect over the already rendered entities. A dedicated Light buffer will be used.

The `PhysicEngine` will process all the light like other `Entity` according to the `Light` declared `PhysicType`.

## Implementation proposition

### Light class

A new `Light` class inheriting from `Entity` will provide all the new and necessary light characteristics, over already
existing `Entity` attributes. the default `PhysicType` will be `STATIC`.

```java
public static class Light extends Entity {
    private double energy;
    private LightType lightType;

    public Light(String name) {
        super(name);
        setPhysicType(STATIC);
    }
    //...
    // add some getter and setter respecting the Fluent API
    //...
}
```

The Scene will contain a new List of lights.

### Scene

The Scene provides a new interface to retrieve the list of active Light:

```java
public interface Scene {
    //...
    List<Light> getLights();
}
```

### Render

In the rendering pipeline, we need a new `BufferedImage`, to support drawing lights. This buffer is the applied through
a Transparent transformation on the already existing rendering buffer.

First, detect all Light in the pipeline, and draw those lights after other objects :

```java
public class Render {
    public void draw(long realFps) {
        //...
        // render all objects but lights
        gPipeline.stream()
                .filter(e -> !(e instanceof Light) && e.isAlive() || e.isPersistent())
                .forEach(e -> {
                            //...
        });
        // Draw lights only
        gPipeline.stream().filter(e -> e instanceof Light).forEach(l -> {
            if (l.isNotStickToCamera()) {
                moveCamera(g, activeCamera, -1);
            }
            drawLight(g, (Light) l);
            if (l.isNotStickToCamera()) {
                moveCamera(g, activeCamera, 1);
            }
        });
    //...
    }
}
```

And now let's concentrate on the light drawing itself:

```java
public class Render {
    //...
        private void drawLight(Graphics2D g, Light l) {
            switch (l.lightType) {
                case SPOT      -> drawSpotLight(g, l);
                case SPHERICAL -> drawSphericalLight(g, l);
                case AMBIENT   -> drawAmbiantLight(g, l);
            }
        }
    //...
}
```

We need some specialization, first to draw a SPOT light:

```java
public class Render {
    //...
    private void drawSpotLight(Graphics2D g, LightObject l) {

        assert (l.target != null);
        double rotation = Math.acos(((l.position.x * l.target.x) + (l.position.y * l.target.y)) /
                (Math.sqrt((l.position.x * l.position.x) + (l.position.y * l.position.y))
                        * Math.sqrt((l.target.x * l.target.x) + (l.target.y * l.target.y))));
        g.rotate(rotation);
        //TODO draw filled triangle.
    }
    //...
}
```

And another specialization to draw an AMBIANT Light:

```java
public class Render {
    //...
    private void drawAmbiantLight(Graphics2D g, LightObject l) {
        Camera cam = app.render.activeCamera;
        Configuration conf = app.config;

        final Area ambientArea = new Area(
            new Rectangle2D.Double(
                cam.pos.x, 
                cam.pos.y, 
                conf.screenWidth, 
                conf.screenHeight));
        g.setColor(l.color);
        Composite c = g.getComposite();
        g.setComposite(
            AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 
                (float) l.energy));
        g.fill(ambientArea);
        g.setComposite(c);
    }
    //...
}
```

And finally a last specialization for a SPHERICAL light:

```java
public class Render {
    //...
    private void drawSphericalLight(Graphics2D g, LightObject l) {
        l.color = brighten(l.color, l.energy);
        Color medColor = brighten(l.color, l.energy * 0.5);
        Color endColor = new Color(0.0f, 0.0f, 0.0f, 0.2f);

        l.colors = new Color[]{l.color,
                medColor,
                endColor};
        l.dist = new float[]{0.0f, 0.1f, 1.0f};
        l.rgp = new RadialGradientPaint(
            new Point(
                (int) (l.pos.x + (10 * Math.random() * l.glitterEffect)),
                (int) (l.pos.y + (10 * Math.random() * l.glitterEffect))), 
                (int) l.width, 
                l.dist, 
                l.colors);
        g.setPaint(l.rgp);
        g.setComposite(
            AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 
                (float) l.energy));
        g.fill(
            new Ellipse2D.Double(
                l.pos.x + (l.width * 0.5), 
                l.pos.y + (l.width * 0.5), 
                l.width * 0.5, 
                l.width * 0.5));
    }
    //...
}
```
