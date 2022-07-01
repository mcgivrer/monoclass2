package com.demoing.app.core.service.physic;

import com.demoing.app.core.entity.Entity;

/**
 * The {@link PhysicType} is used by the PhysicEngine to compute physic behavior for the object.
 * It can be STATIC for static object like static platform, or DYNAMIC for moving objects.
 */
public enum PhysicType {
    /**
     * An {@link Entity} with a {@link PhysicType#DYNAMIC} physic type will be managed by the {@link PhysicEngine} as a dynamic object,
     */
    DYNAMIC,
    /**
     * An {@link Entity} with a {@link PhysicType#STATIC} physic type will not be modified by the {@link PhysicEngine}.
     */
    STATIC,
    /**
     * An Entity with a {@link PhysicType#NONE} physic type will not be manage in any way by the physic engine.
     */
    NONE
}
