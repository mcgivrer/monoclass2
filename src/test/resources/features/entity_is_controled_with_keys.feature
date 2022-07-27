Feature: An Entity can be controlled with keyboard

  According to directional keys (UP,DOWN,LEFT and RIGHT), the entity player can be controlled.

  Scenario: The player is controlled with directional keys
    Given A new Game from configuration "test-player.properties"
    And I create a new Scene typed "KeyScene" named "scene_keys" as default
    And I create an Entity "player" with size of (16,16)
    And I set position to (100.0,100.0) to the Entity "player"
    And I push the "UP" key for 50 ms
    Then the Entity "player" move of 20 px vertically.
