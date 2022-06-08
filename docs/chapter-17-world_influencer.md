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

The `Influencer` will take benefit of all already existing `Entity` attribute to support influencer's goals:
Change the `Entity` contained in the area defined by the Influencer position and size,
and the attributes will support the `attributes` to be applied to these contained entities.

The `World` object will contain the list of `Influencer` to be managed by the `PhysicEngine`.

