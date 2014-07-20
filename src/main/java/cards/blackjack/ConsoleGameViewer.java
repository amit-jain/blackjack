package cards.blackjack;

import java.util.List;

import cards.common.Card;
import cards.common.Hand;
import cards.common.Visitor;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

public class ConsoleGameViewer implements Visitor<List<String>, Blackjack, BlackjackPlayer> {
    @Override
    public List<String> visit(Blackjack game) {
        List<String> rows = Lists.newArrayList();
        rows.addAll(game.dealer().accept(this));
        
        // Combine each players's print row by row mimicking a zip with index operation
        // Now the map contains entries by row for all players
        ListMultimap<Integer, String> multimap = ArrayListMultimap.create();
        for (BlackjackPlayer player : Iterables.skip(game.players(), 1)) {
            List<String> playerRows = player.accept(this);
            for (int idx = 0; idx < playerRows.size(); idx++) {
                multimap.put(idx, playerRows.get(idx));
            }
        }

        rows.add(Strings.repeat(" ", game.dealer().getHand().getCards().size() * 5));
        for(Integer idx : multimap.keySet()) {
            rows.add(Joiner.on(Strings.repeat(" ", 20)).join(multimap.get(idx)));
        }
        return rows;                
    }

    @Override
    public List<String> visit(BlackjackPlayer player) {
        List<String> rows = Lists.newArrayList();
        List<String> hands = player.getHand().accept(this);
        int padLength = (hands.isEmpty() ? 30 : hands.get(0).length());

        rows.add(Strings.padEnd(player.getName(), padLength, ' '));
        rows.add(Strings.repeat("-", padLength));
        
        for(int idx = 0; idx < hands.size(); idx++) {
            String handRow = hands.get(idx);
            rows.add(Strings.padEnd(handRow, padLength, ' '));
        }
        rows.add(Strings.padEnd("Chips : " + player.getChips(), padLength, ' '));
        rows.add(Strings.padEnd("Bet : " + player.getBet(), padLength, ' '));
        if (player.isStateVisible()) {
            rows.add(Strings.padEnd("State : " + player.getState(), padLength, ' '));
        }
        rows.add(Strings.repeat("-", padLength));
        return rows;    
    }

    @Override
    public List<String> visit(Hand hand) {
        List<String> rows = Lists.newArrayList();
        
        // Combine each card's print mimicking a zip with index operation
        // Now the map contains entries by row for all cards
        ListMultimap<Integer, String> multimap = ArrayListMultimap.create();
        List<Card> cards = hand.getCards();
        for (Card card : cards) {
            List<String> cardRows = card.accept(this);
            for (int idx = 0; idx < cardRows.size(); idx++) {
                multimap.put(idx, cardRows.get(idx));
            }
        }

        for(Integer idx : multimap.keySet()) {
            rows.add(String.format(Strings.repeat("%-7.7s", cards.size()), 
                    Iterables.toArray(multimap.get(idx), Object.class)));
        }
        return rows; 
    }

    @Override
    public List<String> visit(Card card) {
        List<String> rows = Lists.newArrayList();
        rows.add("-----");
        if (card.isVisible()) {
            rows.add("| " + card.getRank() + " |");
            rows.add("| " + card.getSuit() + " |");
        } else {
            rows.add("- * -");
            rows.add("- * -");            
        }
        rows.add("-----");

        return rows;
    }

}
