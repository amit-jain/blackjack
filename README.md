# Blackjack
Blackjack is card game usually played at casinos. More information about the game and its rules can be found at http://en.wikipedia.org/wiki/Blackjack.

## Features
* Multi Player - (Number of players can be set when game being initialized)
* Multi Deck - (Number of decks can be set when game being initialized)
* DoubleDown
* Stand
* *Split - Not supported yet*

# Usage
## Setup
### Pre-requisities
* JDK 1.7
* Maven
* [Guava Library](https://code.google.com/p/guava-libraries/)

The project is a maven java project which is a java build tool. It needs to be installed first. It can be downloaded from http://maven.apache.org/. 

JDK can be downloaded from http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html.

The project also has a dependency on the Google's Guava library. The usage of guava is restricted to use of collection and string utilities, including the functional `Function` and `Predicate` utilities.

### Build

The project can be compiled by executing the command at the project root - `mvn clean install`. This will produce the runnable jar in the `target` directory.

## Execution
The game can be executed by running the command
`java -jar blackjack-0.0.1-SNAPSHOT.jar [players=<integer>] [decks=<integer>] [chips=<integer>]`

The options are not mandatory and can be omitted. The default values are :
* players - 1
* decks - 1
* chips - 100

Also, there are a few java system properties that can be set to configure the game.
The properties that can be configured are :
* -DhardStand - Defines whether the dealer stands hard (default is soft)
* -DblackjackWinFactor - Defines the winning factor on the bet in case of a 'Blackjack'.

# Code Structure
#### [API Docs](http://amit-jain.github.io/blackjack/)

The source code is structure into 2 packages:
* *cards.common* - Contains classes which are common to any card game.
  * **Rank** - Enum defining the rank of the card
    * **RankValue** - Class which governs the value of each rank for e.g. an 'Ace' may be values as 1 and 11 in another game.
                  This lets the value to be overridden.
  * **Suit** - Enum defining the Suit of the cards.
  * **Card** - Class representing a playing card which has a Rank and a Suit.
  * **Hand** - Class representing the set of cards held by a player.
  * **Deck** - Class representing the card deck (52 - 4 Suit * 13) for the game. Cards can be pooped out of the deck.
  * **Player** - Class representing a game player
  * **Game** - Marker interface representing a card game.
  * **Visitor** - Generic interface for a Visitor whose implementations can be used to print out the game state.
* *cards.blackjack* - Contains classes which are specific to Blackjack.
  * **BlackjackPlayer** - Blackjack specific player which extends from `cards.common.Player`.
  * **Blackjack** - Represents the blackjack game.
    * **Action** - Inner enum representing the actions for the game and the actions behavior.
    * **Operator** - Inner enum representing the different kind of players and their behavior.
    * **GameState** - Inner enum representing the various game states and their behaviors.
    * **Rule** - Interface for rules of the game. Also, exposes various default implementations for the rules of the game.
  * **ConsoleGameViewer** - An implementation of the `cards.common.Visitor` to print the game on the console.
  * **GameDriver** - Main class for initializing the game.

