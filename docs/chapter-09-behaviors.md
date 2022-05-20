# Behavior's

One way to implement quickly some enhancement in a game is to provide the opportunity to add dynamically some new
behaviors to some game component. The solution is `Behavior`.

We are going to add behavior to be triggered on some new events or process.

Adding the `Behavior` on `Entity` will allow us to bring some basic gameplay. Particularly in the collision
event (`onCollision`), where sme new thing may happen:

- increase the player score each time a ball is destroyed,
- impact player energy each time a ball collide the player, and manage energy and life of the player.

Adding new Platforms that contains a new attribute named "dead" and set to true. If a ball collide such platform, it
disappear and raise the player score.

Let's begin with the `Behavior` interface:

```java
public interface Behavior {
    public String getName();

    public void update(Application a, Entity e);

    public void onCollide(Application a, Entity e1, Entity e2);
}
```

The 2 possible processing are `onCollide` to process collision, and `update`, to manage entity at `Scene` level.

The Entity behaviors are a new `Map<String,Behavior>`:

```java
public static class Entity {
    //...
    public Map<String, Behavior> behaviors = new HashMap<>();

    //...
    public Entity addBehavior(Behavior b) {
        this.behaviors.put(b.getName(), b);
        return this;
    }
    //...
}
```

And in the `CollisionDetector` class :

```java
public static class CollisionDetector {
    //...
    private void detect() {
        List<Entity> targets = colliders.values().stream().filter(e -> e.isAlive() || e.isInfiniteLife()).toList();
        for (Entity e1 : colliders.values()) {
            e1.collide = false;
            for (Entity e2 : targets) {
                e2.collide = false;
                if (e1.id != e2.id && e1.cbox.getBounds().intersects(e2.cbox.getBounds())) {
                    resolve(e1, e2);
                    e1.behaviors.values().stream()
                            .filter(b -> b.getName().equals("onCollision"))
                            .collect(Collectors.toList())
                            .forEach(b -> b.onCollide(app, e1, e2));
                }
            }
        }
    }
    //...
}
```

The at entity creation, you can add dynamically a new behavior:

```java
public static class DemoScene {
    //...
    Entity player = new Entity("player")
            //...
            .addBehavior(new Behavior() {
                @Override
                public String getName() {
                    return "onCollision";
                }

                @Override
                public void onCollide(Application a, Entity e1, Entity e2) {
                    if (e2.name.contains("ball_")) {
                        reducePlayerEnergy(a, e1, e2);
                    }
                }

                @Override
                public void update(Application a, Entity e) {
                }
            });
    //...
}
```

Now if a collision happen between the player and a ball entity, the player energy will decrease (
see `reducePlayerEnergy(a, e1, e2);` for details)
And where a ball collide with anew platform having the "dead" attribute, the ball is disabled and the score is increase
of the value of the attribute "points":

```java
public static class DemoScene {
    //...
    private void generateEntity(Application app, String namePrefix, int nbEntity, double acc) {
        for (int i = 0; i < nbEntity; i++) {
            Entity e = new Entity(namePrefix + entityIndex)
                    //...
                    .addBehavior(new Behavior() {
                        @Override
                        public String getName() {
                            return "onCollision";
                        }

                        @Override
                        public void onCollide(
                                Application a,
                                Entity e1,
                                Entity e2) {
                            // If hurt a dead attribute platform => Die !
                            if ((boolean) e2.getAttribute("dead", false)
                                    && e1.isAlive()) {
                                // increase score
                                int score = (int) a.getAttribute("score", 0);
                                int points = (int) e1.getAttribute("points", 0);
                                a.setAttribute("score", score + points);
                                // kill ball entity
                                e1.setDuration(0);
                            }
                        }
                    });
            //...
        }
    }
    //...
}
```

Some internal mechanism for score, life and energy won't be detailed here, but just parsing the code will let you
discover it.

> _**NOTE**_ > _You must notice that score an,d life are Application attributes, and energy is player's Entity attribute._
