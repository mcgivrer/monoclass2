# Adding some Particles

Any modern game now even 2D and 3D, offers the opportunity to animate things with particles. A `Particle` is a small
object, animated through a group of itself, where applying the same animation mechanism, to simulate often natural
effects, like rain, snow, wind, fire, or also falling feathers, sparkling and so on !

The `Particle` are often grouped into one master entity, named `ParticleSystem`. The main entity contains all child
particles, and is the root for the animation mechanism. This `PartileSystem` must also provide a way to generate
new `Particle` through a `ParticleGenerator`.

We already have some in place feature that match parts of this requirement:

- `Entity` can have `Behavior`, corresponding to the master entity `ParticleSystem`,
- `Entity` can be also the `Particle` because it can support animation, physic computation, and also have a life
  duration.
- The `Entity` can provide a `ParticleGenerator` through a specific `Behavior`, but this new `Behavior` may have a new
  method to create sub elements.

So let's adapt our `Entity` class to support that new requirement !

## Evolving Entity

The current entity already have `Behavior`, so this match the particle animation update need.
It also has a life duration to simulate particle life duration.
`Particle` rendering will rely on the `Entity` rendering system, no need to add new thing here.

But to satisfy the `ParticleSystem`'s `Partcile` list need, we will add some required new attribute to `Entity`: a child
list, and the mandatory tools to add, remove and update those child entities.

```java
public class Entity {
    //...
    private List<Entity> child = new ArrayList<>();

    //...
    public Entity addChild(Entity a) {
        child.add(e);
        return this;
    }

    public Entity removeChild(Entity e) {
        child.remove(e);
        return this;
    }
    //...
}
```

Creating a `ParticleSystem` may add some helpers to the existing `Entity`. Let's create this new entity.

## ParticleSystem is an Entity

The `ParticleSystem` class is a extended `Entity`. It supports new helpers to add Particle specific Behaviors, and a
particle generator.

The `Particle` animation will be provided by a Behavior `ON_UPDATE_ENTITY`.

The generator will be supported by a same `Behavior` event: `ON_UPDATE_ENTITY`.

```java
public class ParticleSystem extends Entity {
    //...
    public ParticleSystem addParticleUpdate(Behavior pub) {
        this.behaviors.put(Behavior.ON_UPDATE_ENTITY, pub);
        return this;
    }

    //...
    public ParticleSystem addParticleGenerator(Behavior pgb) {
        this.behaviors.put(Behavior.ON_UPDATE_ENTITY, pgb);
        return this;
    }
    //...
}
```

The `addParticleUpdate` will provide the Behavior to be applied on all particles to update their animation and moves,
while the `addParticleGenerator` will provide the Behavior to be used as a Particle generator, to create initiale
particles and generate new ones, like drop in the rain, of snowflake in the snow, etc...

THe rendering for a PArticle System must take in account all particles, but this will be implemented at the `Entity`
level to render child entities.

So the `Render` service must be adapted to let it draw child elements from any entity having child.

### Rendering PArticles

As said just before, we need to render child element of any `Entity` having ones.

So at the draw Entity level on the Render service, we will add a call to the rendering to all child elements:

```java
public class Render implements System {

}
```
