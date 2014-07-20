package cards.blackjack;

import java.io.StringWriter;

import cards.common.Card;
import cards.common.Game;
import cards.common.Hand;
import cards.common.Player;
import cards.common.Rank;
import cards.common.Visitor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * An extension of the common card {@link Player} providing {@link Blackjack} specific functionality.
 */
public class BlackjackPlayer extends Player {
    private PlayerState state;
    
    private String type;
    
    private boolean stateVisible = true;
    
    public BlackjackPlayer() {
        init();
    }
    
    /**
     * Initialize the players state
     */
    void init() {
        setHand(new Hand());
        setState(PlayerState.INPLAY);
    }

    public PlayerState getState() {
        return state;
    }

    BlackjackPlayer setState(PlayerState state) {
        this.state = state;
        return this;
    }
    
    public String getType() {
        return type;
    }

    public BlackjackPlayer setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isStateVisible() {
        return stateVisible;
    }

    public void setStateVisible(boolean stateVisible) {
        this.stateVisible = stateVisible;
    }

    /**
     * Value of the players hand.
     * 
     * @return the value
     */
    public int value() {
        return getHand().value();
    }
    
    /**
     * Value of the player's hand counting an 'Ace' as 11.
     * This will only return a different value if the soft value is valid i.e. <= 21. Otherwise, 
     * it returns the hard value().
     * 
     * @return the value
     */
    public int softValue() {
        int numAces = Iterables.size(Iterables.filter(getHand().getCards(), 
                                new Predicate<Card>() {
                        
                                    @Override
                                    public boolean apply(Card card) {
                                        return card.getRank() == Rank.ACE;
                                    }
                                    
                                }));
        // Gets the max value <= 21
        int max = value();
        for (int idx = 0; idx <= numAces; idx++) {
            int hardAce = idx * 10 + value();
            if (hardAce <= 21 && hardAce > max) {
                max = hardAce;
            }
        }
        return max;
    }
    
    public void doubleBet() {
        setBet(getBet() * 2);
    }
    
    /**
     * Defines constants for player state.
     */
    enum PlayerState {
        BLACKJACK, BUST, INPLAY, NON_BLACKJACK_21, DOUBLE_DOWN, STAND_DOWN, LOOSE, PUSH, WIN;
    }
    
    public <T, BP extends Player> T accept(Visitor<T, ? extends Game, BlackjackPlayer> visitor) {
        return visitor.visit(this);
    }
    
    @Override
    public String toString() {
        StringWriter buffer = new StringWriter();
        buffer.append(getName());
        buffer.append(getHand().toString());
        buffer.append("Chips : " + getChips());
        buffer.append("Bet : " + getBet());
        if (stateVisible) {
            buffer.append("State : " + getState());
        }
        
        return buffer.toString();
    }
}
