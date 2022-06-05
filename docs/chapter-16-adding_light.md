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

The `PhysicEngine` will process all the light like other  `Entity` according to the `Light` declared `PhysicType`.

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

```java
public class Render {
    //...
    public void drawLight(Graphics2D g, Light l) {
        switch (l.lightType) {
            case SPHERICAL:
                drawSphericalLight(g, l);
                break;
            case SPOT:
                drawSpotLight(g, l);
                break;
            case AMBIANT:
                drawAmbiantLight(g, l);
                break;
            default:
                break;
        }
        l.rendered = true;
    }
    //...
}
```

And now a specialization to draw a SPOT light:

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

Another specialization to draw an AMBIANT Light:

```java
public class Render {
    //...
    private void drawAmbiantLight(Graphics2D g, LightObject l) {
        Camera cam = renderer.getGame().getSceneManager().getCurrent().getActiveCamera();
        Configuration conf = renderer.getGame().getConfiguration();

        final Area ambientArea = new Area(new Rectangle2D.Double(cam.position.x, cam.position.y, conf.width, conf.height));
        g.setColor(l.foregroundColor);
        Composite c = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, l.intensity.floatValue()));
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
        l.color = brighten(l.foregroundColor, l.intensity);
        Color medColor = brighten(l.foregroundColor, l.intensity / 1.6f);
        Color endColor = new Color(0.0f, 0.0f, 0.0f, 0.01f);

        l.colors = new Color[]{l.color,
                medColor,
                endColor};
        l.dist = new float[]{0.01f, 0.2f, 0.5f};
        l.rgp = new RadialGradientPaint(new Point((int) (l.position.x + (10 * Math.random() * l.glitterEffect)),
                (int) (l.position.y - (10 * Math.random() * l.glitterEffect))), (int) l.width, l.dist, l.colors);
        g.setPaint(l.rgp);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, l.intensity.floatValue()));
        g.fill(new Ellipse2D.Double(l.position.x - l.width / 2, l.position.y - l.width / 2, l.width, l.width));
    }
    //...
}
```