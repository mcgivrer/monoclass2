package com.demoing.app.core.utils;

import com.demoing.app.core.Application;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;

/**
 * {@link Utils} is an utilities class to provide some basic operations
 *
 * @author Frédéric Delorme
 * @since 1.0.4
 */
public class Utils {

    /**
     * Retrieve the root path for the current .class or .jar.
     *
     * @return a String path for the current .class or JAR file.
     */
    public static String getJarPath() {
        String jarDir = null;
        CodeSource codeSource = Application.class.getProtectionDomain().getCodeSource();
        try {
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            jarDir = jarFile.getParentFile().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return jarDir;
    }
}
