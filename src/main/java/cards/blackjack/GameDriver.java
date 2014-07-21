package cards.blackjack;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cards.blackjack.Blackjack.Action;
import cards.blackjack.Blackjack.GameState;
import cards.common.Visitor;

import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * Main class to instantiate the game.
 * Is a simple CLI driver for the blackjack game. 
 * An extension of this class can be used for a graphics version of the game.
 */
public class GameDriver {    
    /** Constants for named user inputs **/
    
    public static final String PLAYERS = "players";
    
    public static final String DECKS = "decks";
    
    public static final String CHIPS = "chips";

    public static void main(String args[]) {
        System.out.println("To customize number of players, decks and chips (default 1, 1, 100) " + 
                                "- java GameDriver players=<integer> decks=<integer> chips=<integer>");
        
        Blackjack game = null;
        if (args.length == 3) {
            String line = args[0] + " " + args[1] + " " + args[2];
            Map<String, String> options = 
                    Splitter.on(" ").omitEmptyStrings().
                        trimResults().withKeyValueSeparator("=").split(line);
            game = new Blackjack(Integer.parseInt(options.get(PLAYERS)), 
                    Integer.parseInt(options.get(DECKS)),
                    Integer.parseInt(options.get(CHIPS)));
        } else {
            game = new Blackjack(1, 1, 100);
        }
        
        Scanner scanner = new Scanner(System.in);
        try {
            // Initial actions for the game
            List<Action> actions = game.getState().getActions();
            GameState state = GameState.START;
            
            while (true) {
                Map<Integer, Action> actionIdMap = createActionIdetifierMap(actions);
                printGame(game, state);
                printApplicableActions(actionIdMap);
                
                /* Choose action */
                int chosenActionInt = chooseAction(scanner, actionIdMap);
                
                /* Get the chosen action */
                Action chosenAction = actionIdMap.get(chosenActionInt);
                
                /* Get any parameters applicable for the chosen action */
                int[] params = getChosenActionParameters(scanner, chosenAction.paramsRequired(game));
                
                /* Execute action and get actions applicable after that */
                state = game.action(
                        chosenAction.toString(), params);
                
                /* Set available actions */
                actions = state.getActions();
            }
        } finally {
            scanner.close();
        }
    }
  
    private static int chooseAction(Scanner scanner, Map<Integer, Action> actionIdMap) {
        int chosenActionInt = Integer.MAX_VALUE;
        
        while (true) {
            String token = scanner.next();
            
            if (token.matches("\\d")) {
                chosenActionInt = Integer.parseInt(token);
            
                if (!actionIdMap.containsKey(chosenActionInt)) {
                    System.err.println("Choose a valid action from the above");
                } else {
                    break;
                }
            } else {
                System.err.println("Choose a valid action from the above");                
            }
        }
        return chosenActionInt;
    }

    /**
     * Create a mapping from the action identifier to the action.
     *  
     * @param actions the actions valid for the current state of the game
     * @return      the map
     */
    private static Map<Integer, Action> createActionIdetifierMap(List<Action> actions) {
        Map<Integer, Action> actionMap = Maps.newHashMap();
        
        for (Action action : actions) {
            actionMap.put(action.ordinal(), action);
        }
        
        return actionMap;
    }

    /**
     * Returns the applicable parameters for the chosen action
     * 
     * @param scanner the command line scanner
     * @param game the game instance
     * @param chosenAction the chosen action by the player
     * @return      the action parameters
     */
    private static int[] getChosenActionParameters(Scanner scanner, int numParams) {
        int count = 0;
        int[] params = new int[numParams];
        
        if (numParams > 0) {
            Arrays.fill(params, 1);
            
            String line = scanner.nextLine();
            if (line.matches("\\s([0-9]\\s?)+")) {
                Iterable<String> tokens = 
                        Splitter.on(" ").trimResults().omitEmptyStrings().split(line);
                
                // Only honor bets if the numbers entered are for all players
                if (Iterables.size(tokens) == numParams) {
                    for (String token : tokens) {
                        params[count++] = Integer.parseInt(token);
                    }
                }
            }
        }
        return params;
    }
    
    /**
     * Print the current applicable actions for the user to choose
     * 
     * @param actions the actions valid for the current state of the game
     */
    private static void printApplicableActions(Map<Integer, Action> actions) {        
        StringWriter writer = new StringWriter();
        
        writer.append("Choose the number corresponding to the following actions : ");
        writer.append(StandardSystemProperty.LINE_SEPARATOR.value());
        
        for (Integer action : actions.keySet()) {
            writer.append(actions.get(action).getUsage() + " - " + action);
            writer.append(StandardSystemProperty.LINE_SEPARATOR.value());
        }
        System.out.println(writer);
    }

    /**
     * Prints the game state to the console
     * 
     * @param game the game instance
     * @param state 
     */
    private static void printGame(Blackjack game, GameState state) {
        Visitor<List<String>, Blackjack, BlackjackPlayer> viewer = new ConsoleGameViewer();
        for (String row : game.accept(viewer)) {
            System.out.println(row);
        }
        System.out.println();
        System.out.println("Game State : " + state.toString());
        System.out.println();
    }
}
