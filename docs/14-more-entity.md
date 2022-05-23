# More Entity !

As we are designing our engine, we know that each gameplay or feature to be added will need more models. Here is a
bundle of new entities to achieve some standard 2D platform things.

## Adding a Map Entity

A `MapEntity` is a small level map displayed on a border screen to show globally the current player, enemies regarding
the all scene.

### MapEntity

The `MapEntity` will be a simple container identified and stick to screen (in fact to active `Camera`), where all the
declared active and alive Entity will be drawn as simple color pixels, depending on a color mapping.

```plantuml
class MapEntity extends Entity{
  - entitiesRef:List<Entity>
  - colorEntityMapping:Map<String,Color>
  - backgroundColor:Color
  - world:World
}
```

The attributes are:

- `entititesRef` the existing Entity list in the current Scene,
- `colorEntityMapping` a list of Color to mapped with key name,
- `backgroundColor` the color for the map background drawing,
- `world` the parent World to match the map size to.

### Render

The `Render` must be adapted to render this new MapEntity:

```java
public static class Render {
    //...
    public void draw(long realFps) {
        //...
        switch (e) {
            //...
            case MapEntity me -> {
                drawMapEntity(g, me);
            }
        }
        //...
    }
}
```

All the Entities which have a corresponding matching key entry oin the color mapping, will be drawn as a simple colored
rectangle at its corresponding position, rescaling the World area size to the map displayed size.

```java
public static class Render {
    //...
    public void drawMapEntity(Graphics2D g, MapEntity me) {
        // draw the map background
        g.setColor(me.color);
        g.drawRect((int) me.pos.x, (int) me.pos.y, (int) me.width, (int) me.height);
        g.setColor(me.backgroundColor);
        g.fillRect((int) me.pos.x, (int) me.pos.y, (int) me.width, (int) me.height);
        // draw all the entities according to their color mapping if exists.
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
    //...
}
```
