package cards.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of the deck of playing cards.
 * 
 * @author amitjain
 *
 */
public class Deck {
    /**
     * List of cards in the deck.
     */
    private List<Card> cards;
    
    public Deck(int numDecks) {
        cards = new ArrayList<Card>();
        /* Initialize the number of decks required. */
        for (int deck = 0; deck < numDecks; deck++) {
            /* Initialize the deck to contain all the 52 cards */
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(rank, suit));
                }
            }
        }
        shuffle();
    }
    
    /**
     * Shuffle the card deck randomly.
     */
    public Deck shuffle() {
        int length = cards.size();
        for (int idx = 0; idx < length; idx++) {
            int shuffleIdx = idx + (int) (Math.random() * (length - idx));
            Card swap = cards.get(shuffleIdx);
            cards.set(shuffleIdx, cards.get(idx));
            cards.set(idx, swap);
        }
        return this;
    }
    
    /**
     * Return the card at the top of the deck.
     * To be a little efficient the bottom of the deck is logically treated as the top.
     * @return the card
     */
    public Card popCard() {
        if (!cards.isEmpty()) {
            return cards.remove(cards.size() -1);
        }
        return null;
    }
}
