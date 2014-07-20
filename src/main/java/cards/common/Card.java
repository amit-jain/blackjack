package cards.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a playing card. 
 */
public class Card {
    private Rank rank;

    private Suit suit;
    
    private boolean visible = true;
    
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public int getValue() {
        return rank.getValue();
    }

    public Suit getSuit() {
        return suit;
    }

    public Card setSuit(Suit suit) {
        this.suit = suit;
        return this;
    }
    
    public boolean isVisible() {
        return visible;
    }

    public Card setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Card) {
            Card otherCard = (Card) other;
            if (otherCard.getRank() == getRank()
                            && otherCard.getSuit() == getSuit()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        List<String> rows = prettyPrint();
        
        for (String row : rows) {
            builder.append(row)
            .append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }

    public <T> T accept(Visitor<T, ? extends Game, ? extends Player> visitor) {
        return visitor.visit(this);
    }
    
    public List<String> prettyPrint() {
        List<String> rows = new ArrayList<String>();
        rows.add("-----");
        if (isVisible()) {
            rows.add("| " + getRank() + " |");
            rows.add("| " + getSuit() + " |");
        } else {
            rows.add("- * -");
            rows.add("- * -");            
        }
        rows.add("-----");

        return rows;
    }
    
    public static void main(String argv[]) {
        Card one = new Card(Rank.ACE, Suit.DIAMOND);
        
        Card two = new Card(Rank.TWO, Suit.CLUB);
        
        Card three = new Card(Rank.THREE, Suit.HEART);
        
        Card four = new Card(Rank.FOUR, Suit.SPADE);
        four.setVisible(false);
        
        for (int i = 0; i < 4; i++) {
            System.out.printf("%-7.5s %-7.5s %-7.5s %-7.5s%n", 
                            one.prettyPrint().get(i), two.prettyPrint().get(i),
                            three.prettyPrint().get(i), four.prettyPrint().get(i));
        }
    }
}
