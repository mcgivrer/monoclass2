# Introduction

A bunch of discovery on the Java JDK latest release into a fun and entertaining sample of code. Let's discover the
JEP420 Pattern Matching `switch`, `@FunctionalInterface` and some enhancement on the
`[List/Map].of()` constructors.

# The project

The [`Application`](https://github.com/mcgivrer/monoclass2/blob/feature/add-camera-entity/src/main/java/com/demoing/app/Application.java#L17)
class (we can not talk about a project for a 1500 LoCs class) is composed of some attribtues and methods (sic) and a
limited number of subclasses.

So, [`I18n`](https://github.com/mcgivrer/monoclass2/blob/feature/add-camera-entity/src/main/java/com/demoing/app/Application.java#L69)
, `Entity`, `TextEntity` and `Camera` are subclasses of `Application` master class, if you does not understand :P.

## Project organization & structure

We are going to organize our project like the standard maven one, but without the `pom.xml`.

```txt
[Project]
 |_ docs               # Project documention structure as book's chapters
 |_ lib                # libraries use as tooling only
 |_ scripts            # scripts for this project
 |  |_ build.sh        # the bash build script
 |_ src                # sources code for the project
 |  |_ main            # main sources
 |  |  |_ java         #  |_ java source code
 |  |  |_ resources    #  |_ resources like properties and images
 |  |_ test            # test source code
 |    |_ java          #  |_ java test implementation
 |    |_ resources     #  |_ Mandatory resources for test execution
 |_ target             # where all the compilation phases will output things
 |_ .gitignore         # the mandatory git ignore file to not track unnecessary ones
 |_ CODE_OF_CONDUCT.md # the way we work on  this project
 |_ CONTRIBUTING.md    # Some rules to follow to contribute to the effort
 |_ README.md          # The mandatory README to understand where you are
 |_ LICENSE            # My MIT license letting you using this piece of software
 --- Extra files --------------------------------------------------------------------
 |_ Dockerfile         # A brand new way to build (using docker for env build).
 |_ .gitpod.yml        # An experiment on the GitPod service (workspace def.)
 |_ .gitpod.Dockefile  # An experiment on the GitPod service (env def.)
 ```

## Some mandatory requirements

1. Only one player can interact with the application at the same time,
2. The application will manage some entities which are internal objects and graphical elements,
3. Each of these entities can be the player, or any other part/element interacting in the game play,
4. The input interface is the keyboard:

    1. the player will moves according to directional keys strokes,
       (<kbd>UP</kbd>,<kbd>DOWN</kbd>,<kbd>LEFT</kbd>,<kbd>RIGHT</kbd>)

    2. some other keys (<kbd>P</kbd>,<kbd>PAUSE</kbd>) to switch game to pause mode,

    3. some key (<kbd>D</kbd>) will rotate the debug display level,
    4. A key(<kbd>F3</kbd>) will take a game window screenshot and save it to a specific directory in the
       java [JAR](https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jarGuide.html "see the official ORACLE definition for a JAR file")
       directory or in the class path,
    5. some key (<kbd>D</kbd>) will rotate the debug display level,

    6. In the debug mode, a key to reset (<kbd>Z</kbd>) the current scene (you will discover the nature of a scene
       later),
    7. the game can be exited at any time with a specific key (<kbd>ESC</kbd>).
    8. For physic engine computation, the gravity will be switch on or off on demand with the <kbd>G</kbd> key.

5. Providing configuration through standard properties file,

6. Offering some translation capabilities with a set a supported languages and a default one,

7. At rendering time, entities must be drawn according to a control sort order,

8. Displayed graphical elements will be extracted from standard PNG images.

9. Rendering some text to screen will be achieved by using standard TTF fonts.

10. Some graphical entities must stick to the view to show score, energy, mana, life, gameplay messages, etc... This is
    commonly named
    the [Head-Up-Display](<https://en.wikipedia.org/wiki/HUD_(video_gaming)> "let's see what Wikipedia knows about HUD") (
    HUD).

11. The rendering process must support a 60 FPS[^1] rate without diminishing performance in a normal mode (not while
    debug display mode is activated),

12. Some collision detection must be supported to interact between entities, (a
    basic [AABB collision detection algorithm]() will match with this requirement)

13. The Entity moves must be based the basic physic mathematical with forces, speed and acceleration.

14. An Entity must support the Sprite animation principle to display dynamic and animated graphics.

[^1]: Frame Per Second: the number of images computed during one second of time
