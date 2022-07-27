package com.demoing.app.tests.features;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.service.physic.PhysicType;
import com.demoing.app.core.service.physic.material.Material;
import com.demoing.app.tests.core.AbstractApplicationTest;
import io.cucumber.java8.An;
import io.cucumber.java8.En;
import io.cucumber.java8.StepDefinitionBody;
import io.cucumber.messages.internal.com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InfluencerPhysicMyStepdefs extends AbstractApplicationTest implements En {

    private Entity entity;
    private static Map<String, Material> materials = new ConcurrentHashMap<>();

    public InfluencerPhysicMyStepdefs() {
        And("I set PhysicType to {string} to the Entity {string}", (String physicType, String entityName) -> {
            Entity player = getApp().getEntity(entityName);
            entity = player.setPhysicType(PhysicType.valueOf(physicType.toUpperCase()));
        });
        And("I create Material {string} with density of {double}, friction of {double} and elasticity of {double}",
                (String materialName, Double density, Double friction, Double elasticity) -> {
                    Material m = new Material(materialName, density, elasticity, friction);
                    materials.put(materialName, m);
                });
        And("I set Material {string} to Entity {string}", (String materialName, String entityName) -> {
            Entity player = getApp().getEntity(entityName);
            player.setMaterial(materials.get(materialName));
        });
        Then("the Entity named {string} will not move.", (String entityName) -> {

            Entity player = getApp().getEntity(entityName);
            Vec2d initialPosition = (Vec2d) cache.get("initialPosition");

            for (int i = 0; i < 60; i++) {
                getApp().getPhysicEngine().update(16);
            }
            assertAll("Entity player has not moved",
                    () -> assertTrue(
                            player.pos.x == initialPosition.x,
                            String.format("The Player entity has not move on x (=%f)", player.pos.x)),
                    () -> assertTrue(
                            player.pos.y == initialPosition.y,
                            String.format("The Player entity has not move on y (=%f)", player.pos.y))
            );
        });
        And("I set mass to {double} to the Entity {string}", (Double mass, String entityName) -> {
            Entity player = getApp().getEntity(entityName);
            player.setMass(mass);
        });
        Then("the Entity named {string} will bounce on contact.", (String entityName) -> {
            Entity player = getApp().getEntity(entityName);
            for (int i = 0; i < 120; i++) {
                getApp().getPhysicEngine().update(16);
                if(player.collide){
                    break;
                }
            }
            assertAll("The Entity " + entityName + " bounce on play area",
                    () -> assertTrue(player.collide, "The Entity " + entityName + " has not collided"),
                    () -> assertTrue(player.vel.y > 0, "The Entity " + entityName + " has not bounced")
            );

        });
    }
}
