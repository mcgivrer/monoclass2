package com.demoing.app.tests.features;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.math.Vec2d;
import com.demoing.app.core.scene.Scene;
import com.demoing.app.tests.core.AbstractApplicationTest;
import com.demoing.app.tests.scenes.KeyScene;
import io.cucumber.java8.En;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PlayerWithKeysMyStepdefs extends AbstractApplicationTest implements En {
    static Robot robot;
    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    private static Map<String, Integer> keyMapping = Map.of(
            "UP", KeyEvent.VK_UP,
            "DOWN", KeyEvent.VK_DOWN,
            "LEFT", KeyEvent.VK_LEFT,
            "RIGHT", KeyEvent.VK_RIGHT);

    public PlayerWithKeysMyStepdefs() {
        And("I create a new Scene typed {string} named {string} as default", (String sceneType, String sceneName) -> {
            if (sceneType.toUpperCase().equals("KEYSCENE")) {
                Scene kScn = new KeyScene(sceneName);
                getApp().getSceneManager().addScene(sceneName, kScn);
                getApp().getSceneManager().activateScene(sceneName);
            } else {
                fail("Unable to create request scene type " + sceneType);
            }
        });
        And("I push the {string} key for {int} ms", (String keyCode, Integer duration) -> {
            robot.keyPress(keyMapping.get(keyCode.toUpperCase()));
            robot.delay(duration);
            robot.keyRelease(keyMapping.get(keyCode.toUpperCase()));
        });
        Then("the Entity {string} move of {double} px vertically.", (String entityName, Double verticalMoveInPx) -> {
            Vec2d initialPosition = (Vec2d) cache.get("initialPosition");
            Entity player = getApp().getEntity(entityName);
            double deltaY = Math.abs(player.pos.y - initialPosition.y);
            assertEquals(verticalMoveInPx, deltaY, 4,
                    "The Entity " + entityName + " has not moved vertically for " + verticalMoveInPx);
        });
    }
}
