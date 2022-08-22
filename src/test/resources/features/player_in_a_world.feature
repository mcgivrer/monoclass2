Feature: A player evolves freely in a World.

  A PLayer entity is created and can evolve freely in a limited World area.

  @player @world @scene
  Scenario: I can create a World into a Scene
    Given A new Game from configuration "test-player.properties"
    And I create a new Scene named "scene_1" as default
    And I create a World "world_0"
    And I add a default Material to world "world_0"
    And A add an area of 400 x 400 to world "world_0"
    Then The game has a world to play with.

  @player @world @scene @physic
  Scenario: I add an Entity named 'player' that can move into the world
    Given A new Game from configuration "test-player.properties"
    And I create a new Scene named "scene_1" as default
    And I create an Entity "player" with size of (16,16)
    And I set position to (100.0,100.0) to the Entity "player"
    And I set speed to (3.0,3.0) to the Entity "player"
    Then the Entity "player" can move vertically and horizontally in the World "world_0".

  @player @world @scene @physic @influencer
  Scenario: I add an Entity named 'player' that influenced by an `Influencer`
    Given A new Game from configuration "test-player.properties"
    And I create a new Scene named "scene_2" as default
    And I create an Entity "player" with size of (16,16)
    And I set position to (100.0,100.0) to the Entity "player"
    And I create an Influencer named "influencer_1" at (0.0,0.0) with size of (400.0,400.0)
    And I set a force of (10.0,10.0) on Influencer "influencer_1"
    Then the Entity "player" can move vertically and horizontally in the World "world_0".