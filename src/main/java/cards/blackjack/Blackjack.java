package cards.blackjack;

import java.io.StringWriter;
import java.util.List;

import cards.blackjack.BlackjackPlayer.PlayerState;
import cards.common.Card;
import cards.common.Deck;
import cards.common.Game;
import cards.common.Player;
import cards.common.Visitor;
import cards.common.Rank;
import cards.common.RankValue;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Blackjack implements Game {
    /**
     * Specifies if dealer hand is hard or soft
     */
    private static final boolean HARD_STAND = Boolean.getBoolean("hardStand");
    
    /**
     * Specifies blackjack win factor
     */
    private static final double BLACKJACK_WIN_FACTOR = getWinFactor();
    private static double getWinFactor() {
        double factor = 1.5;
        try {
            factor = Double.parseDouble(System.getProperty("blackjackWinFactor"));
        } catch (final Exception e) {
            // Ignore and use default
        }
        return factor;
    }
    
    /* Initialized game information. */
    
    /**
     * Number of players in the game.
     */
    private int numPlayers;
    
    /**
     * Number of decks to be used in the game.
     */
    private int numDecks;

    /**
     * Number of chips available to each player.
     */
    private int chips;
    
    /* Initialized game information. */    

    /* State information per game */
    
    /**
     * The deck(s) of cards for the game.
     */
    private Deck deck;
    
    /**
     * List of players. Includes the dealer at 1st (index 0) position.
     */
    private List<BlackjackPlayer> players;
    
    /**
     * Keeps track of the game state.
     */
    private GameState state;
    
    /**
     * State information of the current player.
     */
    private int currentPlayerIdx;
    
    /* State information per game */
    
    public GameState getState() {
        return state;
    }
    
    public Blackjack(int numPlayers, int numDecks, int chips) {
        this.numPlayers = numPlayers;
        this.numDecks = numDecks;
        this.chips = chips;
        init();
    }
    
     private void init() {
        /** Override the RankValue with the blackjack specific RankValue **/
        Rank.rankValue = new RankValue() {
            @Override
            public int getValue(Rank rank) {
                /* Override values for Jack, Queen and King */
                if (rank == Rank.JACK || rank == Rank.QUEEN || rank == Rank.KING) {
                    switch(rank) {
                        case JACK:
                        case QUEEN:
                        case KING:
                        default :
                            return 10;
                    }
                } else {
                    return super.getValue(rank);
                }
            }
        };
        deck = new Deck(numDecks);
        players = Lists.newArrayList();
        
        // Add the first player as the dealer
        BlackjackPlayer dealer = new BlackjackPlayer();
        dealer.setName("Dealer");
        dealer.setType(Operator.DEALER.toString());
        dealer.setStateVisible(false);
        players.add(dealer);
        
        // Add players
        for (int idx = 0; idx < numPlayers; idx++) {
            players.add((BlackjackPlayer) new BlackjackPlayer()
                            .setType(Operator.PLAYER.toString())
                            .setName("player " + (idx + 1))
                            .setChips(chips));
        }
        currentPlayerIdx = 0;
        state = GameState.START;
    }
    
    /**
     * Returns the current player
     * 
     * @return
     */
    private BlackjackPlayer currentPlayer() {
        return players.get(currentPlayerIdx);
    }
    
    /**
     * Returns the dealer.
     * 
     * @return      the dealer
     */
    BlackjackPlayer dealer() {
        return players.get(0);
    }
    
    /**
     * Returns the players.
     * 
     * @return      the players
     */
    protected List<BlackjackPlayer> players() {
        return players;
    }    
    
    /**
     * Main method which performs actions on the game.
     * 
     * @param action the action to take, defined by {@link Action}
     * @param params any parameters needed to perform action
     * @return      game state
     */
    public GameState action(String action, int[] params) {
        return Action.valueOf(action).perform(this, params);
    }
    
    /**
     * Cycle through each of the players to identify the next turn player
     */
    private void cycle() {
        currentPlayerIdx = (++currentPlayerIdx) % players.size();
    }
    
    /**
     * State machine for the actions and their behaviors available in the game.
     */
    enum Action {
        /**
         * Action to add a card to the player's hand.
         */
        HIT ("HIT") {
            @Override
            public GameState perform(Blackjack game, int[] params) {
                GameState stateAfterHit = Operator.valueOf(
                                game.currentPlayer().getType()).operate(game);
                
                // Goto the next player if the game is in play and the player state in not in play
                if ((stateAfterHit == GameState.INPLAY)
                        && (game.currentPlayer().getState() != PlayerState.INPLAY)) {
                    game.cycle();
                    
                    // If it is dealer's turn auto hit till allowed
                    if (game.currentPlayer() == game.dealer()) {
                        return Operator.valueOf(
                                        game.currentPlayer().getType()).operate(game);
                    }
                }
                return stateAfterHit;
            }
        }, 
        /**
         * Action to stand by without taking another hit.
         */
        STAND ("STAND") {
            @Override
            public GameState perform(Blackjack game, int[] params) {
                game.currentPlayer().setState(PlayerState.STAND_DOWN);
                // Goto the next player
                game.cycle();
                
                // If it is dealer's turn auto hit
                if (game.currentPlayer() == game.dealer()) {
                    return HIT.perform(game, params);
                }

                return GameState.INPLAY;
            }   
        },
        /**
         * Action to split the hand of same value cards.
         */
        SPLIT ("SPLIT") {
            @Override
            public GameState perform(Blackjack game, int[] params) {
                throw new UnsupportedOperationException("Split operation not yet supported");
            }   
        },
        /**
         * Action to double the bet and take hit once.
         */
        DOUBLEDOWN ("DOUBLEDOWN"){
            @Override
            public GameState perform(Blackjack game, int[] params) {
                game.currentPlayer().doubleBet();
                game.currentPlayer().setState(PlayerState.DOUBLE_DOWN);
                // Need a single hit
                return HIT.perform(game, params);                
            }   
        }, 
        /**
         * To begin a new round of the game.
         * <p>
         * Expects a 'bet' parameter to be passed for each player.
         */        
        DEAL ("DEAL <bets>") {
            @Override
            public GameState perform(Blackjack game, int[] bets) {
                game.deck = new Deck(game.numDecks);
                game.currentPlayerIdx = 0;
                game.dealer().setStateVisible(false);

                // Sets the bet and re-initializes each player
                for (int idx = 0; idx < game.players.size(); idx++) {
                    BlackjackPlayer player = game.players.get(idx);
                    player.init();
                    
                    // Dealer does not take bet
                    if (idx != 0) {
                        player.setBet(bets[idx - 1]);
                    }
                }
                
                // Deal 2 cards turn by turn
                for (int idx = 0; idx < 2; idx++) {
                    for (int playerIdx = 0; playerIdx < game.players.size(); playerIdx++) {
                        BlackjackPlayer player = game.players.get(playerIdx);
                        
                        Card card = game.deck.popCard();
                        player.getHand().addCard(card);
                        
                        // After dealing of the second card, update the player state
                        if (idx == 1) {
                            if (playerIdx == 0) {
                                card.setVisible(true);
                            }
                            Operator.valueOf(player.getType()).setPlayerState(player, true);
                        }
                    }  
                }
                
                // If all players other than Dealer are BLACKJACK then dealer auto hits
                if (Iterables.getLast(Operator.rules()).match(game)) {
                    // Dealer's turn auto hit till allowed
                    game.currentPlayerIdx = 0;
                    return HIT.perform(game, bets);
                } else {
                    game.cycle();
                }
                return GameState.INPLAY;                
            }

            @Override
            int paramsRequired(Blackjack game) {
                return game.numPlayers;
            }
        },
        /**
         * Initializes a new game.
         */
        RESET("RESET") {
            @Override
            public GameState perform(Blackjack game, int[] params) {
                game.init();
                return GameState.START;
            }
        };
        
        private String usage;

        Action(String usage) {
            this.usage = usage;
        }
        /**
         * Indicates the number of parameters expected.
         * 
         * @return  the number of parameters expected.
         */
        int paramsRequired(Blackjack game) {
            return 0;
        }
        
        /**
         * Take action specific behavior
         * 
         * @param game the current game instance
         * @param params any action specific parama to be passed
         * @return      Returns the state of the game after action
         */
        public abstract GameState perform(Blackjack game, int[] params);
        public String getUsage() {
            return usage;
        }
    }
    
    /**
     * State machine defining the roles of different players and their behavior 
     */
    enum Operator {
        DEALER {
            @Override
            public GameState operate(final Blackjack game) {
                // Set the visibility to the hidden card of the dealer
                Iterables.getLast(game.dealer().getHand().getCards()).setVisible(true);
                
                // Set visible the state
                game.dealer().setStateVisible(true);

                // Hit until hard or soft stand
                while ((HARD_STAND && game.dealer().value() < 17)
                        || (!HARD_STAND && game.dealer().softValue() <= 17)) {
                    game.dealer().getHand().addCard(game.deck.popCard());
                }
                // Update the state
                setPlayerState(game.currentPlayer(), false);
                
                // Apply filters to determine game state
                for (Rule rule : Iterables.skip(rules(), 1)) {
                    if (rule.match(game)) {
                        return rule.apply(game);
                    }
                }
                // Dealer wins as all the above rules are not applicable
                return GameState.WIN.setState(game, Iterables.limit(game.players, 1));
            }

        },
        PLAYER {        
            @Override
            public GameState operate(final Blackjack game) {
                if (game.currentPlayer().getState() != PlayerState.BLACKJACK
                                && game.currentPlayer().getState() != PlayerState.NON_BLACKJACK_21) {
                    // Add a new card
                    Card card = game.deck.popCard();
                    game.currentPlayer().getHand().addCard(card);
                    
                    // Update the state
                    setPlayerState(game.currentPlayer(), false);
                }
                
                // Apply busted rule
                return rules().get(0).apply(game);
            }

        };

        /**
         * Sets the winning or loosing player state
         * 
         * @param player to set the state
         * @param initial whether the inital round (i.e. after deal)
         */
        void setPlayerState(BlackjackPlayer player, boolean initial) {
            // If soft or hard == 21 then Blackjack/Nonblackjack21
            // If > 21 then bust
            if ((player.value() == 21 || player.softValue() == 21) && initial) {
                player.setState(PlayerState.BLACKJACK);
            } else if ((player.value() == 21  || player.softValue() == 21) 
                    && player.getState() != PlayerState.BLACKJACK) {
                player.setState(PlayerState.NON_BLACKJACK_21);
            } else if (player.value() > 21) {
                player.setState(PlayerState.BUST);
            }
        } 

        /** Ordered list of all the applicable rules. **/
        static List<Rule> rules() {
            return Lists.newArrayList(
                            Rule.BUST,
                            Rule.DEALER_BUST,
                            Rule.BLACKJACK_WIN,
                            Rule.BLACKJACK_PUSH,
                            Rule.NON_BLACKJACK_21_WIN,
                            Rule.NON_BLACKJACK_21_PUSH,
                            Rule.GREATER,
                            Rule.EQUAL,
                            Rule.ALL_PLAYER_BLACKJACK);
        }
        
        abstract GameState operate(Blackjack blackjack);
    }
    
    /**
     * Defines the game state 
     */
    enum GameState {
        /**
         * The game finished with some player/dealer winning.
         */
        WIN {
            @Override
            GameState setState(Blackjack game, Iterable<BlackjackPlayer> winners) {
                List<BlackjackPlayer> updatedWinners = Lists.newArrayList(Iterables.transform(winners, 
                        new Function<BlackjackPlayer, BlackjackPlayer>() {
        
                            @Override
                            public BlackjackPlayer apply(BlackjackPlayer player) {
                                double factor = 1;
        
                                if (player.getState() == PlayerState.BLACKJACK) {
                                    factor = BLACKJACK_WIN_FACTOR;
                                }
                                player.setChips((double) player.getChips() + 
                                        (double) factor * player.getBet());
                                if (player.getState() != PlayerState.BLACKJACK) {
                                    player.setState(PlayerState.WIN);
                                }
                                
                                if (!player.isStateVisible()) {
                                    player.setStateVisible(true);
                                }
                                return player;
                            }
                        }));
                @SuppressWarnings("unused")
                Iterable<BlackjackPlayer> loosers =
                        setLooserState(Iterables.filter(game.players, Predicates.not(
                                            Predicates.in(updatedWinners))));
                return this;
            }

            @Override
            List<Action> getActions() {
                return Lists.newArrayList(Action.DEAL, Action.RESET);
            }
        }, 
        /**
         * The game finished with some players being equal
         */
        PUSH {
            @Override
            GameState setState(Blackjack game, Iterable<BlackjackPlayer> winners) {                
                List<BlackjackPlayer> updatedPushers = Lists.newArrayList(Iterables.transform(winners, 
                        new Function<BlackjackPlayer, BlackjackPlayer>() {

                            @Override
                            public BlackjackPlayer apply(BlackjackPlayer player) {
                                player.setState(PlayerState.PUSH);
                                
                                if (!player.isStateVisible()) {
                                    player.setStateVisible(true);
                                }                                
                                return player;
                            }
                        }));
                @SuppressWarnings("unused")
                Iterable<BlackjackPlayer> loosers =
                    setLooserState(Iterables.filter(game.players, Predicates.not(
                            Predicates.in(updatedPushers))));
                return this;
            }

            @Override
            List<Action> getActions() {
                return Lists.newArrayList(Action.DEAL, Action.RESET);
            }
        },
        /**
         * Game is in progress.
         */
        INPLAY {
            @Override
            GameState setState(Blackjack game, Iterable<BlackjackPlayer> players) {
                return this;
            }

            @Override
            List<Action> getActions() {
                return Lists.newArrayList(Action.HIT, Action.STAND, Action.DOUBLEDOWN, Action.SPLIT);
            }
        },
        /**
         * Game Initialized.
         */
        START {
            @Override
            GameState setState(Blackjack game, Iterable<BlackjackPlayer> players) {
                return this;
            }

            @Override
            List<Action> getActions() {
                return Lists.newArrayList(Action.DEAL);
            }
        };
        
        Iterable<BlackjackPlayer> setLooserState(Iterable<BlackjackPlayer> loosers) {
            return Lists.newArrayList(Iterables.transform(loosers, 
                    new Function<BlackjackPlayer, BlackjackPlayer>() {

                        @Override
                        public BlackjackPlayer apply(BlackjackPlayer player) {
                            player.setChips((double) player.getChips() - (double) player.getBet());
                            if (player.getState() != PlayerState.BUST) {
                                player.setState(PlayerState.LOOSE);
                            }
                            
                            if (!player.isStateVisible()) {
                                player.setStateVisible(true);
                            }                            
                            return player;
                        }
                    }));            
        }
        abstract GameState setState(Blackjack game, Iterable<BlackjackPlayer> players);
        
        abstract List<Action> getActions();
    }
    
    /**
     * Interface for all rules for deciding the outcome of the game.
     * <p>
     * Contains default implementations of all the rules exposed as instance variables.
     * This allows implementation rules inherit from other rules if needed to reuse code.
     * Also, keeps the rules encapsulated at one place. For a description of the rules
     * @see http://en.wikipedia.org/wiki/Blackjack
     */
    interface Rule {
        /**
         * 
         * @param game the current game instance
         * @return      whether the current rule applies
         */
        boolean match(Blackjack game);
        
        /**
         * Applies this rule. This call should be preceded by a call to {@link #match(Blackjack)}
         * 
         * @param game the current game instance
         * @return      the state of the game after applying the rule
         */
        GameState apply(Blackjack game);

        /**
         * Default implementation Rule for player 'Bust'.
         */
        static class BustRule implements Rule {
            Predicate<BlackjackPlayer> predicate = new Predicate<BlackjackPlayer>() {

                @Override
                public boolean apply(BlackjackPlayer player) {
                    return player.getState() == PlayerState.BUST;
                }
            };
            
            @Override
            public boolean match(Blackjack game) {
                return Iterables.any(Iterables.skip(game.players, 1), predicate);
            }

            @Override
            public GameState apply(Blackjack game) {
                Iterable<BlackjackPlayer> unbusted = Iterables.filter(game.players, Predicates.not(predicate));
                if (Iterables.size(unbusted) == 1) {
                    return GameState.WIN.setState(game, unbusted);
                }
                return GameState.INPLAY;
            }
        }
        
        /**
         * Default implementation Rule for Dealer 'Bust'
         */
        static class DealerBustRule extends BustRule {
            @Override
            public boolean match(Blackjack game) {
                return (game.dealer().getState() == PlayerState.BUST);
            }      
        }
        
        /**
         * Default implementation Rule identifying player (including dealer) with a Blackjack 'Win'.
         */
        static class BlackjackWinRule implements Rule {
            Predicate<BlackjackPlayer> predicate = new Predicate<BlackjackPlayer>() {

                @Override
                public boolean apply(BlackjackPlayer player) {
                    return player.getState() == PlayerState.BLACKJACK;
                }
                
            };
            
            @Override
            public boolean match(Blackjack game) {
                return Iterables.size(Iterables.filter(game.players, predicate)) == 1;
            }

            @Override
            public GameState apply(Blackjack game) {
                return GameState.WIN.setState(game, Iterables.filter(game.players, predicate));
            }
            
        }
        
        /**
         * Default implementation Rule identifying multiple players with Blackjack for a 'Push'.
         */
        static class BlackjackPushRule extends BlackjackWinRule {

            @Override
            public boolean match(Blackjack game) {
                return Iterables.size(Iterables.filter(game.players, predicate)) > 1;
            }
            
            @Override
            public GameState apply(Blackjack game) {
                return GameState.PUSH.setState(game, Iterables.filter(game.players, predicate));
            }
        }
        /**
         * Default implementation Rule identifying all players without dealer with Blackjack.
         */
        static class AllPlayerBlackjackRule extends BlackjackWinRule {

            @Override
            public boolean match(Blackjack game) {
                return Iterables.size(
                        Iterables.filter(
                                Iterables.skip(game.players, 1), predicate)) 
                                    == (game.players.size() - 1);
            }
            
            @Override
            public GameState apply(Blackjack game) {
                return GameState.PUSH.setState(game, Iterables.filter(
                                        Iterables.skip(game.players, 1), predicate));
            }
        }

        /**
         * Default implementation Rule identifying player with a Non-Blackjack 21 'Win'. 
         */
        static class NonBlackjack21WinRule implements Rule {
            Predicate<BlackjackPlayer> predicate = new Predicate<BlackjackPlayer>() {

                @Override
                public boolean apply(BlackjackPlayer player) {
                    return player.getState() == PlayerState.NON_BLACKJACK_21;
                }
                
            };
            
            @Override
            public boolean match(Blackjack game) {
                return Iterables.size(Iterables.filter(game.players, predicate)) == 1;
            }

            @Override
            public GameState apply(Blackjack game) {
                return GameState.WIN.setState(game, Iterables.filter(game.players, predicate));
            }
            
        }
        
        /**
         * Default implementation Rule identifying all players with a Non-Blackjack 21 for a 'Push'.
         */
        static class NonBlackjack21PushRule extends NonBlackjack21WinRule {

            @Override
            public boolean match(Blackjack game) {
                return Iterables.size(Iterables.filter(game.players, predicate)) > 1;
            }
            
            @Override
            public GameState apply(Blackjack game) {
                return GameState.PUSH.setState(game, Iterables.filter(game.players, predicate));
            }        
        }
        
        /**
         * Default implementation Rule identifying all players with hand greater than the dealer for a 'Win'.
         */
        static class GreaterThanDealerRule implements Rule {
            @Override
            public boolean match(final Blackjack game) {
                return (Iterables.any(Iterables.skip(game.players, 1), 
                        new Predicate<BlackjackPlayer>() {
                            @Override
                            public boolean apply(BlackjackPlayer player) {
                                return Math.max(player.value(), player.softValue()) 
                                        > Math.max(game.dealer().softValue(), game.dealer().value());
                            }
                        }));
            }

            @Override
            public GameState apply(final Blackjack game) {
                return GameState.WIN.setState(game,
                            Iterables.filter(
                                    Iterables.skip(game.players, 1),  
                                    new Predicate<BlackjackPlayer>() {
                                        @Override
                                        public boolean apply(BlackjackPlayer player) {
                                            return Math.max(player.value(), player.softValue()) 
                                                    > Math.max(game.dealer().softValue(), game.dealer().value());
                                        }
                                    }));
            }
        }
        
        /**
         * Default implementation Rule identifying all players with hand equal to the dealer for a 'Push.
         */
        static class EqualToDealerRule extends GreaterThanDealerRule {
            @Override
            public boolean match(final Blackjack game) {
                return (Iterables.any(Iterables.skip(game.players, 1), 
                        new Predicate<BlackjackPlayer>() {
                            @Override
                            public boolean apply(BlackjackPlayer player) {
                                return Math.max(player.value(), player.softValue()) 
                                        == Math.max(game.dealer().softValue(), game.dealer().value());
                            }
                        }));
            }

            @Override
            public GameState apply(final Blackjack game) {
                return GameState.PUSH.setState(game,
                            Iterables.filter(
                                    game.players,  
                                        new Predicate<BlackjackPlayer>() {
                                            @Override
                                            public boolean apply(BlackjackPlayer player) {
                                                return Math.max(player.value(), player.softValue()) 
                                                        == Math.max(game.dealer().softValue(), game.dealer().value());
                                            }
                                        }));
            }
        }

        Rule BUST = new BustRule();
        Rule DEALER_BUST = new DealerBustRule();
        Rule BLACKJACK_WIN = new BlackjackWinRule();
        Rule BLACKJACK_PUSH = new BlackjackPushRule();
        Rule NON_BLACKJACK_21_WIN = new NonBlackjack21WinRule();
        Rule NON_BLACKJACK_21_PUSH = new NonBlackjack21PushRule();
        Rule GREATER = new GreaterThanDealerRule();
        Rule EQUAL = new EqualToDealerRule();
        Rule ALL_PLAYER_BLACKJACK = new AllPlayerBlackjackRule();
    }
    

    @Override
    public String toString() {
        StringWriter buffer = new StringWriter();
        buffer.append(dealer().toString());
        for (BlackjackPlayer player : players()) {
            buffer.append(player.toString());
        }
        return buffer.toString();        
    }
    
    /**
     * 
     * @param visitor
     * @return
     */
    public <T> T accept(Visitor<T, Blackjack, ? extends Player> visitor) {
        return visitor.visit(this);               
    }    
}
