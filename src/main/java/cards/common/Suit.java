package cards.common;

/**
 * Enum defining the suit of the playing cards.
 * 
 * @author amitjain
 *
 */
public enum Suit {
    SPADE("\u2660"), 
    CLUB("\u2663"),
    HEART("\u2665"), 
    DIAMOND("\u2666");
    
    private String display;
    
    Suit(String display) {
        this.display = display;
    }
    
    public String toString() {
        return display;
    }
}