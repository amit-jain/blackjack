
package cards.common;


/**
 * Visitor interface for visiting the game instance.
 *  
 * @param <T> the return type
 * @param <G> the card Game type
 * @param <P> the card Player type
 */
public interface Visitor <T, G extends Game, P extends Player> {
    public T visit (G game);
    public T visit (P player);
    public T visit (Hand hand);
    public T visit (Card card);
}
