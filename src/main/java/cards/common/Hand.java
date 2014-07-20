package cards.common;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

/**
 * The class represents the individual hand in a card game
 */
public class Hand {
    /**
     * The cards in the hand.
     */
    private List<Card> cards;
    
    public Hand() {
        cards = newArrayList();
    }
    
    /**
     * Gets the value of the hand by calculating the summation of all the cards available. 
     * @return the value
     */
    public int value() {
        int value = 0;
        for (Card card : getCards()) {
            value += card.getValue();
        }
        return value;
    }
    
    /* Getters and setters */
    public List<Card> getCards() {
        return cards;
    }

    public void addCard(Card card) {
        getCards().add(card);
    }
    
    /**
     * Formatting cards horizontally.
     */
    public String toString() {
        // Combine each card's print mimicking a zip with index operation
        // Now the map contains entries by row for all cards
        ListMultimap<Integer, String> multimap = ArrayListMultimap.create();
        for (Card card : cards) {
            List<String> cardRows = card.prettyPrint();
            for (int idx = 0; idx < cardRows.size(); idx++) {
                multimap.put(idx, cardRows.get(idx));
            }
        }
        StringBuilder buffer = new StringBuilder();
        for(Integer idx : multimap.keySet()) {
            buffer.append(String.format("%-7.7s %-7.7s %-7.7s %-7.7s%n", 
                    Iterables.toArray(multimap.get(idx), Object.class)));
        }
        return buffer.toString();
    }

    public static void main(String argv[]) {
        Hand hand = new Hand();
        Card one = new Card(Rank.ACE, Suit.DIAMOND);
        
        Card two = new Card(Rank.TWO, Suit.CLUB);
        
        Card three = new Card(Rank.THREE, Suit.HEART);
        
        Card four = new Card(Rank.FOUR, Suit.SPADE);
        hand.addCard(one);
        hand.addCard(two);
        hand.addCard(three);
        hand.addCard(four);
        
        System.out.println(hand);
    }

    public <T> T accept(Visitor<T, ? extends Game, ? extends Player> visitor) {
        return visitor.visit(this);
    }

    public List<String> prettyPrint() {
        List<String> rows = Lists.newArrayList();
        // Combine each card's print mimicking a zip with index operation
        // Now the map contains entries by row for all cards
        ListMultimap<Integer, String> multimap = ArrayListMultimap.create();
        for (Card card : cards) {
            List<String> cardRows = card.prettyPrint();
            for (int idx = 0; idx < cardRows.size(); idx++) {
                multimap.put(idx, cardRows.get(idx));
            }
        }

        for(Integer idx : multimap.keySet()) {
            rows.add(String.format(Strings.repeat("%-7.7s", cards.size()) + "%n", 
                    Iterables.toArray(multimap.get(idx), Object.class)));
        }
        return rows;        
    }
}
