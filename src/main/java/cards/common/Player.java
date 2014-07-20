package cards.common;

public class Player {
    
    /**
     * The player name.
     */
    private String name;
    
    /**
     * The current bet played by the player
     */
    private int bet;
    
    /**
     * The current chips available with the player
     */
    private double chips;

    /**
     * The current player hand
     */
    private Hand hand;
    
    public Player() {
        this.hand = new Hand();
    }
    
    public Player(String name, int bet, int chips) {
        this.name = name;
        this.bet = bet;
        this.chips = chips;
        this.hand = new Hand();
    }
    
    /* Getters and setters */
    
    public String getName() {
        return name;
    }

    public Player setName(String name) {
        this.name = name;
        return this;
    }

    public int getBet() {
        return bet;
    }

    public Player setBet(int bet) {
        this.bet = bet;
        return this;
    }

    public double getChips() {
        return chips;
    }

    public Player setChips(double chips) {
        this.chips = chips;
        return this;
    }

    public Hand getHand() {
        return hand;
    }

    public Player setHand(Hand hand) {
        this.hand = hand;
        return this;
    }
}
