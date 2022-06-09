# World's Influencer

## Context

The World object describes the environment into all the Entity will evolve, through the rules of PhysicEngine.

The Influencer entity will let entity to be temporarily changed in its attributes, changing forces, friction,
elasticity, color, speed, acceleration, gravity.

The World object will support a map of Influencer, and the PhysicEngine, will apply the Influence attributes changes to
the Entity under Influence.

This would be used to simulate water, wind, magnet, Ice, or anything that can influence any Entity's physical or
graphical attributes.

## Influencer entity

The `Influencer` will take benefit of all already existing `Entity` attribute to support `Influencer`'s goals:
Change the `Entity` contained in the area defined by the Influencer position and size, and the attributes will support
the `attributes` to be applied to these contained entities.

```java 
public class Influencer extends Entity {
    public Influencer(String name){
        super(name);
    }
    //...
}
```

The `World` object will contain the list of `Influencer` to be managed by the `PhysicEngine`.

```java 
public class World {
    //...
    Map<String,Influencer> influencers = new ConcurrentHashMap<>();
    //...
}
```

## Material

To easily adapt any `Entity` in a `Influencer`'s zone, we need a way to define physic attributes atomic object that can
be reused and adapted.

Here we will introduce the `Material` object.

It's a java class having the mandatory physic attribtues like `elasticity`, `densit` and `friction` values, and some
graphical attribute as `color` and `transparency` values.

```java
public class Material {
    //---- physic attributes
    public double elasticity;
    public double friction;
    public double density;
    // ---- graphic attributes
    public Color color;
    public double transparency;
}
```