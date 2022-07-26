package com.demoing.app.core.utils;

import com.demoing.app.core.config.Configuration;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A Translating service to adapt user graphics text to tits preferred language
 *
 * @author Frédéric Delorme
 * @since 1.0.2
 */
public class I18n {
    private static ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");

    private I18n() {
    }

    /**
     * Set the preferred language to the default one from the configuration object..
     *
     * @param config the parent configuration.
     */
    public static void setLanguage(Configuration config) {
        String[] langCountry = (config.defaultLanguage != null ? config.defaultLanguage : "en_EN").split("_");
        messages = ResourceBundle.getBundle("i18n.messages", new Locale(langCountry[0], langCountry[1]));
    }

    /**
     * Return the translated message for key.
     *
     * @param key the  key of the message to retrieved
     * @return the translated value for the key message.
     */
    public static String get(String key) {
        return messages.getString(key);
    }

    /**
     * Return the translated parametric message for key.
     *
     * @param key  the key of the message to retrieved
     * @param args the list of parameters to be applied to the translated message.
     * @return the translated value for the key message.
     */
    public static String get(String key, Object... args) {
        return String.format(messages.getString(key), args);
    }
}
