package com.demoing.app.core.config;

import com.demoing.app.core.Application;
import com.demoing.app.core.utils.I18n;
import com.demoing.app.core.utils.Logger;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

/**
 * The Configuration class provide default attributes values provision from a <code>app.properties</code> file.
 * Based on a simple {@link Properties} java class, it eases the initialization of the {@link Application}.
 */
public class Configuration {
    Properties appProps = new Properties();
    /**
     * default width of the screen
     */
    public double screenWidth = 320.0;
    /**
     * default height of the screen
     */
    public double screenHeight = 200.0;
    /**
     * display pixel scale at start.
     */
    public double displayScale = 2.0;
    /**
     * the required Frame Per Second to update game mechanic and render to screen.
     */
    public double fps = 0.0;
    /**
     * The internal display debug level to display infirmation at rendering time on screen
     * (level from 0 =No debug to 5 max level debug info).
     */
    public int debug;

    /**
     * Level for logger output.
     * (level from 0=none, to 5=max details)
     *
     * @see Logger
     */
    public int logLevel = 0;
    public long frameTime = 0;

    /**
     * Default World play area width
     */
    public double worldWidth = 0;
    /**
     * Default World play area height
     */
    public double worldHeight = 0;
    /**
     * Default World play area gravity
     */
    public double worldGravity = 1.0;
    /**
     * Flag to define fullscreen mode.  true=> full screen.
     */
    public boolean fullScreen = false;

    public int numberOfBuffer = 2;

    /**
     * Default minimum speed for PhysicEngine. under this value, considere 0.
     */
    public double speedMinValue = 0.1;
    /**
     * Default maximum speed for PhysicEngine, fixing upper threshold.
     */
    public double speedMaxValue = 4.0;
    /**
     * Default minimum acceleration for PhysicEngine. under this value, considere 0.
     */
    public double accMinValue = 0.1;
    /**
     * Default maximum acceleration for PhysicEngine, fixing upper threshold.
     */
    public double accMaxValue = 0.35;

    /**
     * Default minimum speed for CollisionDetector. under this value, considere 0.
     */
    public double colSpeedMinValue = 0.1;
    /**
     * Default maximum speed for CollisionDetector, fixing upper threshold.
     */
    public double colSpeedMaxValue = 2.0;

    /**
     * The default Scenes list.
     * format "[code1]:[path_to_class1];[code1]:[path_to_class1];"
     */
    public String scenes;
    /**
     * Default scene to be activated at start e.g.: 'code1'.
     */
    public String defaultScene;

    /**
     * Default language to be activated at start (e.g. en_EN).
     *
     * @see I18n
     */
    public String defaultLanguage;


    /**
     * Initialize configuration with the filename properties file.
     *
     * @param fileName the path and name of the properties file to be loaded.
     */
    public Configuration(String fileName) {
        try {
            InputStream is = this.getClass().getResourceAsStream(fileName);
            appProps.load(is);
            loadConfig();
        } catch (Exception e) {
            System.err.printf("ERR: Unable to read the configuration file %s : %s\n", fileName, e.getLocalizedMessage());
        }
    }

    /**
     * Map Properties attributes values to Configuration attributes.
     */
    private void loadConfig() {
        debug = parseInt(appProps.getProperty("app.debug.level", "0"));
        logLevel = parseInt(appProps.getProperty("app.logger.level", "0"));

        screenWidth = parseDouble(appProps.getProperty("app.screen.width", "320.0"));
        screenHeight = parseDouble(appProps.getProperty("app.screen.height", "200.0"));
        displayScale = parseDouble(appProps.getProperty("app.screen.scale", "2.0"));
        numberOfBuffer = parseInt(appProps.getProperty("app.render.buffers", "2"));

        worldWidth = parseDouble(appProps.getProperty("app.world.area.width", "640.0"));
        worldHeight = parseDouble(appProps.getProperty("app.world.area.height", "400.0"));
        worldGravity = parseDouble(appProps.getProperty("app.world.gravity", "400.0"));

        speedMinValue = parseDouble(appProps.getProperty("app.physic.speed.min", "0.1"));
        speedMaxValue = parseDouble(appProps.getProperty("app.physic.speed.max", "8.0"));
        accMinValue = parseDouble(appProps.getProperty("app.physic.acceleration.min", "0.01"));
        accMaxValue = parseDouble(appProps.getProperty("app.physic.acceleration.max", "3.0"));

        colSpeedMinValue = parseDouble(appProps.getProperty("app.collision.speed.min", "0.1"));
        colSpeedMaxValue = parseDouble(appProps.getProperty("app.collision.speed.max", "8.0"));

        fps = parseInt(appProps.getProperty("app.screen.fps", "" + Application.FPS_DEFAULT));
        frameTime = (long) (1000 / fps);
        convertStringToBoolean(appProps.getProperty("app.window.mode.fullscreen", "false"));

        scenes = appProps.getProperty("app.scene.list");
        defaultScene = appProps.getProperty("app.scene.default");

        defaultLanguage = appProps.getProperty("app.language.default", "en_EN");
    }

    /**
     * Parse a String value to a Double one.
     *
     * @param stringValue the string value to be converted to double.
     * @return
     */
    private double parseDouble(String stringValue) {
        return Double.parseDouble(stringValue);
    }

    /**
     * Part s String value to an Integer value.
     *
     * @param stringValue the string value to be converted to int.
     * @return
     */
    private int parseInt(String stringValue) {
        return Integer.parseInt(stringValue);
    }

    /**
     * Parse a list of arguments (typically produced from command line interface) and extract arguments values
     * to configuration attributes values.
     *
     * @param args the main arguments list
     * @return the updated Configuration object.
     */
    public Configuration parseArgs(String[] args) {
        // args not null and not empty ? parse it !
        if (Optional.ofNullable((args)).isPresent() && args.length > 0) {
            Arrays.asList(args).forEach(arg -> {
                String[] argSplit = arg.split("=");
                Logger.log(Logger.ERROR, this.getClass(), "arg: %s=%s", argSplit[0], argSplit[1]);
                switch (argSplit[0].toLowerCase()) {
                    case "w", "width" -> screenWidth = parseDouble(argSplit[1]);
                    case "h", "height" -> screenHeight = parseDouble(argSplit[1]);
                    case "s", "scale" -> displayScale = parseDouble(argSplit[1]);
                    case "b", "buffers" -> numberOfBuffer = parseInt(argSplit[1]);
                    case "d", "debug" -> debug = parseInt(argSplit[1]);
                    case "t", "log" -> logLevel = parseInt(argSplit[1]);
                    case "ww", "worldWidth" -> worldWidth = parseDouble(argSplit[1]);
                    case "wh", "worldHeight" -> worldHeight = parseDouble(argSplit[1]);
                    case "wg", "worldGravity" -> worldGravity = parseDouble(argSplit[1]);
                    case "spmin" -> speedMinValue = parseDouble(argSplit[1]);
                    case "spmax" -> speedMaxValue = parseDouble(argSplit[1]);
                    case "accmin" -> accMinValue = parseDouble(argSplit[1]);
                    case "accmax" -> accMaxValue = parseDouble(argSplit[1]);
                    case "cspmin" -> colSpeedMinValue = parseDouble(argSplit[1]);
                    case "cspmax" -> colSpeedMaxValue = parseDouble(argSplit[1]);
                    case "fps" -> fps = parseDouble(argSplit[1]);
                    case "f", "fullScreen" -> convertStringToBoolean(argSplit[1]);
                    case "scene" -> defaultScene = argSplit[1];
                    case "l", "language", "lang" -> defaultLanguage = argSplit[1];
                    default -> Logger.log(Logger.ERROR, this.getClass(), "ERR : Unknown argument %s\n", arg);
                }
            });
        }
        return this;
    }

    /**
     * Convert a String value to a boolean value. Will transform "ON", "on", "true", "TRUE", "1" or "True" to a true bollean value.
     *
     * @param value the String value to be converted to boolean.
     */
    private void convertStringToBoolean(String value) {
        fullScreen = "on|ON|true|True|TRUE|1".contains(value);
    }

}
