# Render

All draw operations are delegated to the `Render` class to order, draw and update some pipeline's `Entity` attributes.

## JDK 17-18 advantages

Mosty in all services and processing class, I'll be using the latest Lambdas possibilities and the JEP-420 preview from
JDK17 and 18, the Pattern Matching Switch Capability, For rendering purpose, adapting the `Render#draw` method the the
type of Entity in the pipeline.

```java
class Render {
    public draw(double fps) {
        gPipeline.stream()
                .filter(e -> e.isAlive() || e.isNeverDying())
                .forEach(e -> {
                    //...
                    switch (e) {
                        case TextEntity te -> {
                            // draw Text entity
                        }
                        case Entity ee -> {
                            // draw a standard Entity
                        }
                    }
                    //...
                });
    }
}
```

And in the Entity draw operation, the rendering process is adapted to the enum type of Entity:

```java
class Render {
    //...
    public void draw(double fps) {
        //...
        switch (e) {
            //...
            case Entity ee -> {
                // draw a standard Entity
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
        //...
    }
    //...
}
```
