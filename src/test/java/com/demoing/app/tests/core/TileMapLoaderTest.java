package com.demoing.app.tests.core;

import com.demoing.app.core.entity.Entity;
import com.demoing.app.core.gfx.Animation;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TileMapLoaderTest {
    Entity obj = new Entity("player");
    Map<Object, Object> resources = new HashMap<>();

    @BeforeEach
    void setup() {
        obj = new Entity("player");
        resources.put(1, "/images/tiles01.png");
        resources.put(2, "/images/backgrounds/forest.jpg");
        resources.put(3, "/images/sprites01.png");
    }


    @Test
    public void regExpToLoadAnimationsConfigTest() {

        obj.animations = new Animation();

        final String regexTemplate = "((?<name>\\S*)=\\{x=(?<x>\\d+),y=(?<y>\\d+),tw=(?<tw>\\d+),th=(?<th>\\d+),time=\\[(?<time>.*!\\])\\],resource=(?<resource>\\d+),loop=(?<loop>\\d+)\\}(,*))";

        final String data = "{idle={x=0,y=0,tw=32,th=32,time=[450, 60, 60, 250, 60, 60, 60, 450, 60, 60, 60, 250, 60],resource=3,loop=1},"
                + "walk={x=0,y=32,tw=32,th=32,time=[60, 60, 60, 150, 60, 60, 60, 150],resource=3,loop=1},"
                + "jump={x=0,y=160,tw=32,th=32,time=[60, 60, 250, 250, 60, 60],resource=3,loop=0},"
                + "dead={x=0,y=224,tw=32,th=32,time=[160, 160, 160, 160, 160, 160, 500],resource=3,loop=0}"
                + "}";

        final Pattern pattern = Pattern.compile(regexTemplate, Pattern.UNIX_LINES);
        final Matcher matcher = pattern.matcher(data.substring(1, data.length() - 1));
        int last = 0;

        Map<String, Map<String, String>> loadedData = new ConcurrentHashMap<>();
        while (matcher.find() && last <= data.length()) {
            Map<String, String> item = new HashMap<>();
            item.put("name", matcher.group("name"));
            item.put("x", matcher.group("x"));
            item.put("y", matcher.group("y"));
            item.put("tw", matcher.group("tw"));
            item.put("th", matcher.group("th"));
            item.put("time", matcher.group("time"));
            item.put("resource", matcher.group("resource"));
            item.put("loop", matcher.group("loop"));
            loadedData.put(matcher.group("name"), item);
            last = matcher.end();

            int[] timeFrames = Arrays.stream(matcher.group("time").split(",")).mapToInt(s -> Integer.parseInt(s.trim())).toArray();

            obj.animations.addAnimationSet(
                    matcher.group("name"),
                    (String) resources.get(Integer.valueOf(matcher.group("resource"))),
                    Integer.parseInt(matcher.group("x")),
                    Integer.parseInt(matcher.group("x")),
                    Integer.parseInt(matcher.group("tw")),
                    Integer.parseInt(matcher.group("th")),
                    timeFrames, Integer.parseInt(matcher.group("loop")));
        }

        Assertions.assertEquals(4, obj.animations.getAnimationSet().size());
        Assertions.assertTrue(obj.animations.getAnimationSet().containsKey("idle"));
        Assertions.assertTrue(obj.animations.getAnimationSet().containsKey("jump"));
        Assertions.assertTrue(obj.animations.getAnimationSet().containsKey("walk"));
        Assertions.assertTrue(obj.animations.getAnimationSet().containsKey("dead"));

        // idle animation
        Assertions.assertEquals(loadedData.get("idle").get("x"), "0");
        Assertions.assertEquals(loadedData.get("idle").get("y"), "0");
        Assertions.assertEquals(loadedData.get("idle").get("tw"), "32");
        Assertions.assertEquals(loadedData.get("idle").get("th"), "32");
        Assertions.assertEquals(loadedData.get("idle").get("time"), "450, 60, 60, 250, 60, 60, 60, 450, 60, 60, 60, 250, 60");
        Assertions.assertEquals(loadedData.get("idle").get("resource"), "3");
        Assertions.assertEquals(loadedData.get("idle").get("loop"), "1");


        // walk animation

        Assertions.assertEquals(loadedData.get("walk").get("name"), "walk");
        Assertions.assertEquals(loadedData.get("walk").get("x"), "0");
        Assertions.assertEquals(loadedData.get("walk").get("y"), "32");
        Assertions.assertEquals(loadedData.get("walk").get("tw"), "32");
        Assertions.assertEquals(loadedData.get("walk").get("th"), "32");
        Assertions.assertEquals(loadedData.get("walk").get("time"), "60, 60, 60, 150, 60, 60, 60, 150");
        Assertions.assertEquals(loadedData.get("walk").get("resource"), "3");
        Assertions.assertEquals(loadedData.get("idle").get("loop"), "1");

        // jump animation

        Assertions.assertEquals(loadedData.get("jump").get("name"), "jump");
        Assertions.assertEquals(loadedData.get("jump").get("x"), "0");
        Assertions.assertEquals(loadedData.get("jump").get("y"), "160");
        Assertions.assertEquals(loadedData.get("jump").get("tw"), "32");
        Assertions.assertEquals(loadedData.get("jump").get("th"), "32");
        Assertions.assertEquals(loadedData.get("jump").get("time"), "60, 60, 250, 250, 60, 60");
        Assertions.assertEquals(loadedData.get("jump").get("resource"), "3");
        Assertions.assertEquals(loadedData.get("jump").get("loop"), "0");

        // dead animation

        Assertions.assertEquals(loadedData.get("dead").get("name"), "dead");
        Assertions.assertEquals(loadedData.get("dead").get("x"), "0");
        Assertions.assertEquals(loadedData.get("dead").get("y"), "224");
        Assertions.assertEquals(loadedData.get("dead").get("tw"), "32");
        Assertions.assertEquals(loadedData.get("dead").get("th"), "32");
        Assertions.assertEquals(loadedData.get("dead").get("time"), "160, 160, 160, 160, 160, 160, 500");
        Assertions.assertEquals(loadedData.get("dead").get("resource"), "3");
        Assertions.assertEquals(loadedData.get("dead").get("loop"), "0");

    }
}
