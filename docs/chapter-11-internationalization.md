# I18n and default Language

If no configuration is set, the System language will be set. But if the configuration key `app.language.default` is
provided, the value will be applied to the I18n service:

```java
public static class I18n {
    //...
    public static void setLanguage(Configuration config) {
        String[] langCountry = config.defaultLanguage.split("_");
        messages = ResourceBundle
                .getBundle(
                        "i18n.messages",
                        new Locale(langCountry[0], langCountry[1]));
    }
    //...
}
```

And, necessary need the new defaultLanguage attribute in the Configuration:

```java
public static class Configuration {
    //...
    public String defaultLanguage;

    //...
    private void loadConfig() {
        //...
        defaultLanguage = appProps.getProperty(
                "app.language.default",
                "en_EN");
        //...
    }

    private Configuration parseArgs(String[] args) {
        Arrays.asList(args).forEach(arg -> {
            String[] values = arg.split("=");
            switch (values[0].toLowerCase()) {
                //...
                case "l", "language", "lang" -> defaultLanguage = values[1];
            }
        });
    }
}
```

And now starting the jar with `lang=fr_FR` will start the game with a French translation:

```shell
$ java --enable-preview -jar target/monoclass2-1.0.3.jar \
   lang=fr_FR
```

The current supported values in this demo are:

- `fr_FR` for French language,
- `en_EN` for English language,
- `es_ES` for Spanish language,
- `de_DE` for gGerman language.
