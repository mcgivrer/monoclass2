Feature: The Material used on entity influence physic computation

  Giving some entity with defined materials, the different physic material attributes will make physic computation
  different behaviors.

  Scenario: Adding an inert material to a dynamic Entity prevent it from moving.
    Given A new Game from configuration "test-material.properties"
    And I create an Entity "player" with size of (16,16)
    And I set PhysicType to "dynamic" to the Entity "player"
    And I set position to (100.0,100.0) to the Entity "player"
    And I create Material "test" with density of 1.0, friction of 0.0 and elasticity of 0.0
    And I set Material "test" to Entity "player"
    Then the Entity named "player" will not move.


  Scenario: Adding an bouncing material to a dynamic Entity let it bounce on contact.
    Given A new Game from configuration "test-material.properties"
    And I create an Entity "player" with size of (16,16)
    And I set PhysicType to "dynamic" to the Entity "player"
    And I set position to (100.0,120.0) to the Entity "player"
    And I set mass to 100.0 to the Entity "player"
    And I create Material "test" with density of 1.0, friction of 1.0 and elasticity of 1.0
    And I set Material "test" to Entity "player"
    Then the Entity named "player" will bounce on contact.