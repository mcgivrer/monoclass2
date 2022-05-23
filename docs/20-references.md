# References

## Project File structure

Here is the file structure fot the project, as a reference.

```text
.
├── Dockerfile
├── docs
│   ├── diagrams
│   │   └── class-diagram.txt
│   ├── elk
│   │   └── Docker-compose.yaml
│   ├── images
│   │   ├── animation-lifebar.png
│   │   ├── behaviors-and-gameplay.png
│   │   ├── collision-detection-and-platform.png
│   │   ├── illustration-sprites.png
│   │   ├── jconsole-accept-connection.png
│   │   ├── jconsole-basic-process-info.png
│   │   ├── jconsole-mbean-attributes.png
│   │   ├── jconsole-mbean-dyn-value.png
│   │   ├── jconsole-starting-connection.png
│   │   └── physic-engine-with-gravity.png
│   └── mission-control
│       └── config.xml
│   ├── 00-index.md
│   ├── 01-introduction.md
│   ├── 02-master_class.md
│   ├── 03-delegation.md
│   ├── 04-configuration.md
│   ├── 05-render.md
│   ├── 06-physic_engine.md
│   ├── 07-animations.md
│   ├── 08-collision_detection.md
│   ├── 09-behaviors.md
│   ├── 10-gameplay.md
│   ├── 11-internationalization.md
│   ├── 12-jmx_and_metrics.md
│   ├── 13-monitoring.md
│   ├── 100-index.md
├── lib
│   ├── options.txt
│   ├── stub.sh
│   ├── test
│   │   ├── checkstyle-10.2-all.jar
│   │   └── junit-platform-console-standalone-1.8.2.jar
│   └── tools
│       └── markdown2html-0.3.1.jar
├── monoclass2.iml
├── README.md
├── scripts
│   └── build.sh
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── demoing
    │   │           └── app
    │   │               ├── core
    │   │               │   └── Application.java
    │   │               └── scenes
    │   │                   └── DemoScene.java
    │   └── resources
    │       ├── app.properties
    │       ├── fonts
    │       │   └── FreePixel.ttf
    │       ├── i18n
    │       │   ├── messages_de_DE.properties
    │       │   ├── messages_en_EN.properties
    │       │   ├── messages_es_ES.properties
    │       │   ├── messages_fr_FR.properties
    │       │   └── messages.properties
    │       └── images
    │           ├── sg-logo-image.png
    │           ├── sprites01.png
    │           ├── sprites01.xcf
    │           └── tiles01.png
    └── test
        ├── java
        │   └── com
        │       └── demoing
        │           └── app
        │               ├── core
        │               │   └── ApplicationAnimationTest.java
        │               │   └── ApplicationConfigurationTest.java
        │               └── tests
        │                   └── TestScene.java
        └── resources
            ├── app.properties
            ├── fonts
            │   └── FreePixel.ttf
            └── i18n
                ├── messages_de_DE.properties
                ├── messages_en_EN.properties
                ├── messages_es_ES.properties
                ├── messages_fr_FR.properties
                └── messages.properties
```
