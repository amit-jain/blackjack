package cards.common;

import cards.common.Rank;

/**
 * Defines the conventional value of the rank.
 * Implementations for games where the value is different should override {@link #getValue(Rank)}.
 * 
 * @author amitjain
 *
 */
public class RankValue {

    public int getValue(Rank rank) {
        switch(rank) {
            case ACE:
                return 1;
            case TWO:
                return 2;
            case THREE:
                return 3;
            case FOUR:
                return 4;
            case FIVE:
                return 5;
            case SIX:
                return 6;
            case SEVEN:
                return 7;
            case EIGHT:
                return 8;
            case NINE:
                return 9;
            case TEN:
                return 10;
            case JACK:
                return 11;
            case QUEEN:
                return 12;
            default: /* King*/
                return 13;
        }
    }

}
