# Delegation pattern

The application based on the one master class, have some sub objects as models, and some as services. To keep it simple
and light, the `Application` class contains the main mechanic. `I18n` provide helpers for translation, while `Entity`,
`Vec2D`, `Camera` are objects to support main activities and are more data oriented.

The configuration operation would be moved to a dedicated object (maybe in a second step), and also the _update_ and
_draw_ operations could be delegated to sub-objects.

```plantuml
@startuml

hide Application methods
hide Application attributes
class Application

hide Entity methods
hide Entity attributes
class Entity

hide Configuration attributes
class Configuration{
 +parseArgs(args:String[])
}
hide Render attributes
class Render{
 +draw(fps:double)
 +addToPipeline(e:Entity)
}
hide PhysicEngine attributes
class PhysicEngine{
 +update(elapsed:double)
}
hide I18n attributes
class I18n{
 + get(key:String)
 + get(key:String,...Object)
}

Application --> Entity:entities
Application --> Configuration:configuration
Application --> Render:render
Application --> PhysicEngine:physicEngine
Application -> I18n
@enduml
```

_figure $fig+ - the full Application class diagram_

