# card-game


## Description

### Starting the game

#### Player connects to server

Players can connect to server. There is no requirement to implement any authentication.

#### Server sends account balance to player

There is an account balance (as "tokens") associated with the player, and the server returns the current
balance to the player upon connection. Account balances can be negative. It is acceptable to implement
transient (non-persistent) account balances (e.g., issue 1000 tokens to each player upon connecting).

#### Player chooses game type

Player informs the server about the game they want to play:
* `single-card-game`
* `double-card-game`

#### Server starts game

The server matches two players who have selected the same game type and starts a new game of this type.

### 'Single-card game'

#### Server deals cards

The server deals a single card from the deck to each player and sends the card to the player.

#### Player makes a decision

Each player independently of each other sends the server a decision which is one of:
* `play`
* `fold`

#### Showdown and results

* In case both players picked `fold` each player loses 1 token.
* In case one player picked `play` and the other player picked `fold` then the player who picked `play` wins
  3 tokens, and the player who picked `fold` loses 3 tokens.
* In case both players picked `play` then the cards are compared (see below) - the winning player wins 10
  tokens, and the losing player loses 10 tokens.

The cards are compared as follows:
* If both cards are equal in rank then the game returns to "Server deals cards" stage.
* Otherwise, the player whose card has the highest rank is considered the winning player, and the other player
  is considered the losing player.

##### Example 1

* `Player A` has `Jack of Clubs` and selects `fold`.
* `Player B` has `Ten of Hearts` and selects `play`.
* `Player A` loses 3 tokens, and `Player B` wins 3 tokens.

##### Example 2

* `Player A` has `Queen of Diamonds` and selects `play`.
* `Player B` has `Nine of Spades` and selects `play`.
* `Player A` wins 10 tokens, and `Player B` loses 10 tokens.

### 'Double-card game'

#### Server deals cards

The server deals two cards from the deck to each player and sends the cards to the player. These two cards
are henceforth referred to as "the hand".

#### Player makes a decision

Each player independently of each other sends the server a decision which is one of:
* `play`
* `fold`

#### Showdown and results

* In case both players picked `fold` each player loses 2 tokens.
* In case one player picked `play` and the other player picked `fold` then the player who picked `play` wins
  5 tokens, and the player who picked `fold` loses 5 tokens.
* In case both players picked `play` then the cards are compared (see below) - the winning player wins 20
  tokens, and the losing player loses 20 tokens.

First, the cards with the highest rank from each hand are compared. If they differ, the player whose card has
the highest rank is considered the winner.

If they are equal, then the cards with the lowest rank from each hand are compared.

If those are also equal, then the game returns to "Server deals cards" stage (new hands are dealt).

##### Example 3

* `Player A` has `Jack of Clubs` and `Nine of Hearts` and selects `play`.
* `Player B` has `Jack of Diamonds` and `Ten of Diamonds` and selects `play`.
* `Player A` loses 20 tokens, and `Player B` wins 20 tokens.

### Finishing the game

Upon finishing of the game, the server applies the game results to player balances, and informs the players
about the game result as well as their respective updated balances.

After this, both players are returned to "Player chooses game type" stage.