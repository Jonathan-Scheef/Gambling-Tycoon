import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * RideTheBus - A high-stakes card game with escalating risk and reward
 * 
 * This implementation of "Ride the Bus" is a 4-round card prediction game where each round
 * becomes progressively more difficult but offers higher multipliers:
 * 
 * Round 1: Red or Black - Player predicts the color of the next card (2x multiplier)
 * Round 2: Higher or Lower - Player predicts if the next card is higher/lower than current (3x multiplier)  
 * Round 3: Inside or Outside - Player predicts if the next card falls between the two previous cards (4x multiplier)
 * Round 4: Suit Prediction - Player predicts the exact suit of the next card (20x multiplier)
 * 
 * Key gameplay mechanics:
 * - Players can cash out after any successful round to secure winnings
 * - Each round has a 10-second timer adding pressure
 * - Any wrong prediction results in losing the entire bet
 * - Uses a standard 52-card deck with Ace high (value 14)
 * - Ties in higher/lower rounds count as losses
 * 
 * The game manages its own deck, shuffling, UI state, and sound effects through
 * integration with the main casino application.
 */
public class RideTheBus {
    private static class Card {
        public enum Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
        public enum Rank { TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }
        
        private final Suit suit;
        private final Rank rank;
        
        public Card(Suit suit, Rank rank) {
            this.suit = suit;
            this.rank = rank;
        }
        
        /**
         * Returns the numerical value of this card (2-14, with Ace high)
         * 
         * @return Card value for comparison purposes
         */
        public int getValue() {
            return rank.ordinal() + 2;  // TWO=2, THREE=3, ..., ACE=14
        }
        
        /**
         * Returns a display-friendly representation of the card
         * 
         * @return String like "A♠" or "K♥"
         */
        public String getDisplayName() {
            String rankStr = switch(rank) {
                case ACE -> "A";
                case JACK -> "J";
                case QUEEN -> "Q";
                case KING -> "K";
                default -> String.valueOf(getValue());
            };
            
            String suitStr = switch(suit) {
                case HEARTS -> "♥";
                case DIAMONDS -> "♦";
                case CLUBS -> "♣";
                case SPADES -> "♠";
            };
            
            return rankStr + suitStr;
        }
        
        /**
         * Returns the color this card should be displayed in
         * 
         * @return Color.RED for hearts/diamonds, Color.BLACK for clubs/spades
         */
        public Color getColor() {
            return (suit == Suit.HEARTS || suit == Suit.DIAMONDS) ? Color.RED : Color.BLACK;
        }
        
        /**
         * Checks if this card is red (hearts or diamonds)
         * 
         * @return true if card is red, false if black
         */
        public boolean isRed() {
            return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
        }
    }
    
    // Game state variables
    private static ArrayList<Card> deck;              // Current shuffled deck
    private static ArrayList<Card> drawnCards;        // Cards drawn during current game
    private static int currentBet = 50;               // Current bet amount
    private static int currentRound = 0;              // Current round (0-3)
    private static int currentMultiplier = 1;         // Current win multiplier
    private static boolean gameActive = false;        // Whether game is in progress
    private static javax.swing.Timer roundTimer;     // Timer for each round
    private static int timeLeft = 10;                 // Seconds remaining in current round
    
    // UI components that need to be accessed from multiple methods
    private static JLabel roundLabel;                 // Shows current round and game status
    private static JLabel betLabel;                   // Shows current bet amount
    private static JLabel multiplierLabel;            // Shows current win multiplier
    private static JLabel instructionLabel;           // Shows instructions for current round
    private static JLabel timerLabel;                 // Shows countdown timer
    private static JTextField betField;               // Input field for bet amount
    private static JPanel cardsPanel;                 // Panel displaying drawn cards
    private static JPanel buttonsPanel;               // Panel containing game action buttons
    private static JButton startButton;               // Button to start a new game
    private static JButton cashOutButton;             // Button to cash out current winnings
    private static JButton redButton;                 // Round 1: Guess red
    private static JButton blackButton;               // Round 1: Guess black
    private static JButton higherButton;              // Round 2: Guess higher
    private static JButton lowerButton;               // Round 2: Guess lower
    private static JButton insideButton;              // Round 3: Guess inside range
    private static JButton outsideButton;             // Round 3: Guess outside range
    private static JButton heartsButton;              // Round 4: Guess hearts
    private static JButton diamondsButton;            // Round 4: Guess diamonds
    private static JButton clubsButton;               // Round 4: Guess clubs
    private static JButton spadesButton;              // Round 4: Guess spades
    
    /**
     * Creates and returns the main Ride the Bus game panel
     * Sets up the complete game interface with betting controls, card display, and action buttons
     * 
     * @param onBack Callback function to return to main menu
     * @return JPanel containing the complete Ride the Bus game interface
     */
    public static JPanel createRideTheBusPanel(Runnable onBack) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 50, 0)); // Dark green casino felt background
        
        // Top panel contains back button and game information
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.addActionListener(e -> onBack.run());
        topPanel.add(backButton, BorderLayout.WEST);
        
        // Game information panel shows bet, multiplier, and round status
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setOpaque(false);
        
        roundLabel = new JLabel("RIDE THE BUS - Ready to Start");
        roundLabel.setFont(new Font("Arial", Font.BOLD, 36));
        roundLabel.setForeground(Color.YELLOW);
        
        betLabel = new JLabel("Bet: $50");
        betLabel.setFont(new Font("Arial", Font.BOLD, 28));
        betLabel.setForeground(Color.WHITE);
        
        multiplierLabel = new JLabel("Multiplier: 1x");
        multiplierLabel.setFont(new Font("Arial", Font.BOLD, 28));
        multiplierLabel.setForeground(Color.CYAN);
        
        JLabel betTextLabel = new JLabel("Bet Amount ($10-$500):");
        betTextLabel.setFont(new Font("Arial", Font.BOLD, 24));
        betTextLabel.setForeground(Color.WHITE);
        
        betField = new JTextField("50", 8);
        betField.setFont(new Font("Arial", Font.PLAIN, 24));
        
        infoPanel.add(betTextLabel);
        infoPanel.add(betField);
        infoPanel.add(betLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(multiplierLabel);
        
        topPanel.add(roundLabel, BorderLayout.CENTER);
        topPanel.add(infoPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Main game area contains instructions, timer, and cards
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setOpaque(false);
        
        // Instructions and timer panel
        JPanel instructionPanel = new JPanel(new BorderLayout());
        instructionPanel.setOpaque(false);
        
        instructionLabel = new JLabel("Welcome to Ride the Bus! Set your bet and press Start to begin.", JLabel.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 32));
        instructionLabel.setForeground(Color.WHITE);
        
        timerLabel = new JLabel("", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 48));
        timerLabel.setForeground(Color.RED);
        
        instructionPanel.add(instructionLabel, BorderLayout.CENTER);
        instructionPanel.add(timerLabel, BorderLayout.SOUTH);
        
        // Cards display area
        cardsPanel = new JPanel(new FlowLayout());
        cardsPanel.setOpaque(false);
        
        gamePanel.add(instructionPanel, BorderLayout.NORTH);
        gamePanel.add(cardsPanel, BorderLayout.CENTER);
        
        panel.add(gamePanel, BorderLayout.CENTER);
        
        // Button panel contains all game action buttons
        buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setOpaque(false);
        
        createButtons();
        setupInitialButtons();
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Initialize the deck for play
        initializeDeck();
        
        return panel;
    }
    /**
     * Creates all the game action buttons with appropriate styling and event handlers
     * Buttons are organized by game round: control buttons, color buttons, comparison buttons, etc.
     */
    private static void createButtons() {
        Font buttonFont = new Font("Arial", Font.BOLD, 24);
        Dimension buttonSize = new Dimension(180, 60);
        
        // Game control buttons
        startButton = new JButton("Start Game");
        startButton.setFont(buttonFont);
        startButton.setPreferredSize(buttonSize);
        startButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            startNewGame();
        });
        
        cashOutButton = new JButton("Cash Out");
        cashOutButton.setFont(buttonFont);
        cashOutButton.setPreferredSize(buttonSize);
        cashOutButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            cashOut();
        });
        
        // Round 1 buttons: Red or Black prediction
        redButton = new JButton("RED");
        redButton.setFont(buttonFont);
        redButton.setPreferredSize(buttonSize);
        redButton.setBackground(Color.RED);
        redButton.setForeground(Color.WHITE);
        redButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeColorChoice(true);  // true = red
        });
        
        blackButton = new JButton("BLACK");
        blackButton.setFont(buttonFont);
        blackButton.setPreferredSize(buttonSize);
        blackButton.setBackground(Color.BLACK);
        blackButton.setForeground(Color.WHITE);
        blackButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeColorChoice(false); // false = black
        });
        
        // Round 2 buttons: Higher or Lower than previous card
        higherButton = new JButton("HIGHER");
        higherButton.setFont(buttonFont);
        higherButton.setPreferredSize(buttonSize);
        higherButton.setBackground(Color.GREEN);
        higherButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeHigherLowerChoice(true);  // true = higher
        });
        
        lowerButton = new JButton("LOWER");
        lowerButton.setFont(buttonFont);
        lowerButton.setPreferredSize(buttonSize);
        lowerButton.setBackground(Color.ORANGE);
        lowerButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeHigherLowerChoice(false); // false = lower
        });
        
        // Round 3 buttons: Inside or Outside the range of previous two cards
        insideButton = new JButton("INSIDE");
        insideButton.setFont(buttonFont);
        insideButton.setPreferredSize(buttonSize);
        insideButton.setBackground(Color.BLUE);
        insideButton.setForeground(Color.WHITE);
        insideButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeInsideOutsideChoice(true);  // true = inside
        });
        
        outsideButton = new JButton("OUTSIDE");
        outsideButton.setFont(buttonFont);
        outsideButton.setPreferredSize(buttonSize);
        outsideButton.setBackground(Color.MAGENTA);
        outsideButton.setForeground(Color.WHITE);
        outsideButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeInsideOutsideChoice(false); // false = outside
        });
        
        // Round 4 buttons: Exact suit prediction (25% chance each)
        heartsButton = new JButton("♥");
        heartsButton.setFont(new Font("Arial", Font.BOLD, 36));
        heartsButton.setPreferredSize(buttonSize);
        heartsButton.setBackground(Color.WHITE);
        heartsButton.setForeground(Color.RED);
        heartsButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeSuitChoice(Card.Suit.HEARTS);
        });
        
        diamondsButton = new JButton("♦");
        diamondsButton.setFont(new Font("Arial", Font.BOLD, 36));
        diamondsButton.setPreferredSize(buttonSize);
        diamondsButton.setBackground(Color.WHITE);
        diamondsButton.setForeground(Color.RED);
        diamondsButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeSuitChoice(Card.Suit.DIAMONDS);
        });
        
        clubsButton = new JButton("♣");
        clubsButton.setFont(new Font("Arial", Font.BOLD, 36));
        clubsButton.setPreferredSize(buttonSize);
        clubsButton.setBackground(Color.WHITE);
        clubsButton.setForeground(Color.BLACK);
        clubsButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeSuitChoice(Card.Suit.CLUBS);
        });
        
        spadesButton = new JButton("♠");
        spadesButton.setFont(new Font("Arial", Font.BOLD, 36));
        spadesButton.setPreferredSize(buttonSize);
        spadesButton.setBackground(Color.WHITE);
        spadesButton.setForeground(Color.BLACK);
        spadesButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            makeSuitChoice(Card.Suit.SPADES);
        });
    }
    
    /**
     * Sets up the initial button state showing only the Start Game button
     */
    private static void setupInitialButtons() {
        buttonsPanel.removeAll();
        buttonsPanel.add(startButton);
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
    }
    
    /**
     * Initializes a fresh, shuffled deck of 52 cards
     * Also resets the drawn cards list for a new game
     */
    private static void initializeDeck() {
        deck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);
        drawnCards = new ArrayList<>();
    }
    
    /**
     * Starts a new game after validating the bet amount
     * Deducts the bet from player's money and begins Round 1
     */
    private static void startNewGame() {
        try {
            currentBet = Integer.parseInt(betField.getText());
            if (currentBet < 10 || currentBet > 500) {
                SoundPlayer.playError();
                instructionLabel.setText("Bet must be between $10 and $500!");
                return;
            }
            if (currentBet > GamblingTycoon.getMoney()) {
                SoundPlayer.playError();
                instructionLabel.setText("Not enough money!");
                return;
            }
        } catch (NumberFormatException ex) {
            SoundPlayer.playError();
            instructionLabel.setText("Please enter a valid bet amount!");
            return;
        }
        
        // Deduct bet from player's money
        GamblingTycoon.updateMoney(-currentBet);
        betLabel.setText("Bet: $" + currentBet);
        
        // Initialize game state
        if (deck.size() < 10) {
            initializeDeck();  // Reshuffle if deck is running low
        }
        
        drawnCards.clear();
        currentRound = 1;
        currentMultiplier = 1;
        gameActive = true;
        
        updateDisplay();
        startRound1();
    }
    
    /**
     * Starts Round 1: Red or Black prediction
     * Player must guess the color of the next card (50/50 chance)
     * Success doubles the initial bet (2x multiplier)
     */
    private static void startRound1() {
        currentRound = 1;
        currentMultiplier = 2; // Round 1 success multiplier
        roundLabel.setText("ROUND 1: COLOR - Red or Black?");
        instructionLabel.setText("Predict the color of the next card!");
        multiplierLabel.setText("Multiplier: " + currentMultiplier + "x");
        
        buttonsPanel.removeAll();
        buttonsPanel.add(redButton);
        buttonsPanel.add(blackButton);
        
        // Enable buttons for the new round
        redButton.setEnabled(true);
        blackButton.setEnabled(true);
        
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
        
        startTimer();
    }
    
    /**
     * Processes the player's color choice for Round 1
     * Draws a card and checks if the prediction was correct
     * 
     * @param chooseRed true if player chose red, false if chose black
     */
    private static void makeColorChoice(boolean chooseRed) {
        if (!gameActive || currentRound != 1) return;
        
        // Immediately disable buttons to prevent double-clicks
        redButton.setEnabled(false);
        blackButton.setEnabled(false);
        
        stopTimer();
        Card drawnCard = deck.remove(deck.size() - 1);
        drawnCards.add(drawnCard);
        
        boolean isRed = drawnCard.isRed();
        boolean correct = (chooseRed && isRed) || (!chooseRed && !isRed);
        
        updateCardDisplay();
        
        if (correct) {
            SoundPlayer.playSound("assets/Button Click.wav");
            instructionLabel.setText("Correct! The card was " + (isRed ? "RED" : "BLACK") + "!");
            proceedToRound2();
        } else {
            SoundPlayer.playError();
            instructionLabel.setText("Wrong! The card was " + (isRed ? "RED" : "BLACK") + ". You lose!");
            endGame(false);
        }
    }
    
    /**
     * Advances to Round 2 after successful Round 1
     * Sets up Higher/Lower prediction with increased multiplier
     */
    private static void proceedToRound2() {
        javax.swing.Timer delay = new javax.swing.Timer(2000, e -> {
            currentRound = 2;
            currentMultiplier = 3; // Round 2 success multiplier
            roundLabel.setText("ROUND 2: HIGHER or LOWER?");
            
            Card lastCard = drawnCards.get(drawnCards.size() - 1);
            instructionLabel.setText("Is the next card higher or lower than " + lastCard.getDisplayName() + "?");
            multiplierLabel.setText("Multiplier: " + currentMultiplier + "x");
            
            buttonsPanel.removeAll();
            buttonsPanel.add(higherButton);
            buttonsPanel.add(lowerButton);
            buttonsPanel.add(cashOutButton);  // Player can now cash out
            
            // Enable buttons for the new round
            higherButton.setEnabled(true);
            lowerButton.setEnabled(true);
            
            buttonsPanel.revalidate();
            buttonsPanel.repaint();
            
            startTimer();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        delay.setRepeats(false);
        delay.start();
    }
    
    /**
     * Processes the player's higher/lower choice for Round 2
     * Compares new card value with the previous card
     * 
     * @param chooseHigher true if player chose higher, false if chose lower
     */
    private static void makeHigherLowerChoice(boolean chooseHigher) {
        if (!gameActive || currentRound != 2) return;
        
        // Immediately disable buttons to prevent double-clicks
        higherButton.setEnabled(false);
        lowerButton.setEnabled(false);
        
        stopTimer();
        Card previousCard = drawnCards.get(drawnCards.size() - 1);
        Card drawnCard = deck.remove(deck.size() - 1);
        drawnCards.add(drawnCard);
        
        boolean isHigher = drawnCard.getValue() > previousCard.getValue();
        boolean correct = (chooseHigher && isHigher) || (!chooseHigher && !isHigher);
        
        // Handle ties (same value)
        if (drawnCard.getValue() == previousCard.getValue()) {
            correct = false; // Ties count as incorrect
        }
        
        updateCardDisplay();
        
        // Check if player's prediction was correct
        if (correct) {
            SoundPlayer.playSound("assets/Button Click.wav");
            instructionLabel.setText("Correct! " + drawnCard.getDisplayName() + " was " + 
                                   (isHigher ? "higher" : "lower") + " than " + previousCard.getDisplayName() + "!");
            proceedToRound3();
        } else {
            SoundPlayer.playError();
            // Provide specific feedback for ties vs wrong predictions
            if (drawnCard.getValue() == previousCard.getValue()) {
                instructionLabel.setText("Same value! " + drawnCard.getDisplayName() + " = " + previousCard.getDisplayName() + ". You lose!");
            } else {
                instructionLabel.setText("Wrong! " + drawnCard.getDisplayName() + " was " + 
                                       (isHigher ? "higher" : "lower") + " than " + previousCard.getDisplayName() + "!");
            }
            endGame(false);
        }
    }
    
    /**
     * Transitions the game to Round 3 (Inside/Outside prediction)
     * Sets up the UI for the player to choose whether the next card will fall
     * between the two previously drawn cards or outside that range
     */
    private static void proceedToRound3() {
        javax.swing.Timer delay = new javax.swing.Timer(2000, e -> {
            currentRound = 3;
            currentMultiplier = 4; // Round 3 multiplier
            roundLabel.setText("ROUND 3: INSIDE or OUTSIDE?");
            
            Card card1 = drawnCards.get(0);
            Card card2 = drawnCards.get(1);
            // Show the range to the player
            instructionLabel.setText("Will the next card be INSIDE or OUTSIDE " + 
                                   card1.getDisplayName() + " and " + card2.getDisplayName() + "?");
            multiplierLabel.setText("Multiplier: " + currentMultiplier + "x");
              buttonsPanel.removeAll();
            buttonsPanel.add(insideButton);
            buttonsPanel.add(outsideButton);
            buttonsPanel.add(cashOutButton);
            
            // Enable buttons for Round 3 choices
            insideButton.setEnabled(true);
            outsideButton.setEnabled(true);
            
            buttonsPanel.revalidate();
            buttonsPanel.repaint();
            
            startTimer();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        delay.setRepeats(false);
        delay.start();
    }
    
    /**
     * Processes the player's inside/outside choice for Round 3
     * Determines if the new card falls between the two previous cards (inside)
     * or outside that range, then evaluates the player's prediction
     * 
     * @param chooseInside true if player chose inside, false if chose outside
     */
    private static void makeInsideOutsideChoice(boolean chooseInside) {
        if (!gameActive || currentRound != 3) return;
        
        // Immediately disable buttons to prevent double-clicks
        insideButton.setEnabled(false);
        outsideButton.setEnabled(false);
        
        stopTimer();
        
        // Get the two previous cards to establish the range
        Card card1 = drawnCards.get(0);
        Card card2 = drawnCards.get(1);
        Card drawnCard = deck.remove(deck.size() - 1);
        drawnCards.add(drawnCard);
        
        // Calculate the inside range (exclusive of boundary values)
        int low = Math.min(card1.getValue(), card2.getValue());
        int high = Math.max(card1.getValue(), card2.getValue());
        int cardValue = drawnCard.getValue();
        
        boolean isInside = cardValue > low && cardValue < high;
        boolean correct = (chooseInside && isInside) || (!chooseInside && !isInside);
        
        updateCardDisplay();
          if (correct) {
            SoundPlayer.playSound("assets/Button Click.wav");
            instructionLabel.setText("Correct! " + drawnCard.getDisplayName() + " was " + 
                                   (isInside ? "inside" : "outside") + " the range!");
            proceedToRound4();        } else {
            SoundPlayer.playError();
            instructionLabel.setText("Wrong! " + drawnCard.getDisplayName() + " was " + 
                                   (isInside ? "inside" : "outside") + " the range!");
            endGame(false);
        }
    }
    
    /**
     * Transitions the game to Round 4 (Suit prediction) - the final and most rewarding round
     * Sets up the UI for the player to choose the exact suit of the final card
     * Offers the highest multiplier (20x) but requires perfect prediction
     */
    private static void proceedToRound4() {
        javax.swing.Timer delay = new javax.swing.Timer(2000, e -> {
            currentRound = 4;
            currentMultiplier = 20; // Round 4 multiplier - Maximum payout!
            roundLabel.setText("ROUND 4: SUIT - Final Round!");
            instructionLabel.setText("Predict the SUIT of the final card for 20x multiplier!");
            multiplierLabel.setText("Multiplier: " + currentMultiplier + "x");
            
            // Replace previous buttons with suit selection buttons
            buttonsPanel.removeAll();
            buttonsPanel.add(heartsButton);
            buttonsPanel.add(diamondsButton);
            buttonsPanel.add(clubsButton);
            buttonsPanel.add(spadesButton);
            buttonsPanel.add(cashOutButton);
            
            // Enable all suit buttons for the final choice
            heartsButton.setEnabled(true);
            diamondsButton.setEnabled(true);
            clubsButton.setEnabled(true);
            spadesButton.setEnabled(true);
            
            buttonsPanel.revalidate();
            buttonsPanel.repaint();
            
            startTimer();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        delay.setRepeats(false);
        delay.start();
    }
    
    /**
     * Processes the player's suit choice for Round 4 (final round)
     * Draws the final card and determines if the exact suit prediction was correct
     * Results in either maximum payout (20x) or complete loss
     * 
     * @param chosenSuit The suit the player predicted for the final card
     */
    private static void makeSuitChoice(Card.Suit chosenSuit) {
        if (!gameActive || currentRound != 4) return;
        
        // Buttons sofort deaktivieren um Doppel-Klicks zu verhindern
        heartsButton.setEnabled(false);
        diamondsButton.setEnabled(false);
        clubsButton.setEnabled(false);
        spadesButton.setEnabled(false);
        
        stopTimer();
        Card drawnCard = deck.remove(deck.size() - 1);
        drawnCards.add(drawnCard);
        
        boolean correct = drawnCard.suit == chosenSuit;
        
        updateCardDisplay();
          if (correct) {
            SoundPlayer.playSound("assets/Button Click.wav");
            instructionLabel.setText("JACKPOT! " + drawnCard.getDisplayName() + " - You won 20x your bet!");
            endGame(true);        } else {
            SoundPlayer.playError();
            instructionLabel.setText("Wrong suit! " + drawnCard.getDisplayName() + " - You lose everything!");
            endGame(false);
        }
    }    /**
     * Allows player to cash out their current winnings and end the game safely
     * Available from Round 2 onwards - player keeps winnings based on completed rounds
     */
    private static void cashOut() {
        if (!gameActive || currentRound < 2) return;
        
        stopTimer();
        
        // Calculate winnings based on number of completed rounds
        int completedRounds = drawnCards.size();
        int completedMultiplier = switch(completedRounds) {
            case 1 -> 2; // After completing Round 1 (color)
            case 2 -> 3; // After completing Round 2 (higher/lower)
            case 3 -> 4; // After completing Round 3 (inside/outside)
            default -> 1; // Fallback safety
        };
        
        int winnings = currentBet * completedMultiplier;
        GamblingTycoon.updateMoney(winnings);
        
        instructionLabel.setText("Cashed out! You won $" + winnings + " (Bet: $" + currentBet + " x " + completedMultiplier + ")!");
        endGameCashOut(); // Use separate method to avoid double money transfer
    }
    
    /**
     * Ends the game after successful cash out
     * Different from regular endGame to avoid double money handling
     */
    private static void endGameCashOut() {
        gameActive = false;
        stopTimer();
        
        roundLabel.setText("Well played! You cashed out safely!");
        
        // Reset to initial state
        buttonsPanel.removeAll();
        buttonsPanel.add(startButton);
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
        
        // Game Over check
        if (GamblingTycoon.getMoney() <= 0) {
            GamblingTycoon.showGameOverScreen();
        }
    }
    
    /**
     * Starts the 10-second countdown timer for the current round
     * Creates tension and forces quick decisions from the player
     */
    private static void startTimer() {
        timeLeft = 10;
        timerLabel.setText("Time: " + timeLeft);
        
        if (roundTimer != null) {
            roundTimer.stop();
        }
        
        roundTimer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);
            
            if (timeLeft <= 0) {
                stopTimer();
                instructionLabel.setText("Time's up! You lose!");
                endGame(false);
            }
        });
        roundTimer.start();
    }
    
    /**
     * Stops the countdown timer and clears the timer display
     */
    private static void stopTimer() {
        if (roundTimer != null) {
            roundTimer.stop();
        }
        timerLabel.setText("");
    }
    
    /**
     * Ends the game with win/loss processing
     * Handles final round completion (20x payout) and game over logic
     * 
     * @param won true if player won the current round, false if lost
     */
    private static void endGame(boolean won) {
        gameActive = false;
        stopTimer();
        
        if (won && currentRound == 4) {
            // Maximum payout for completing all 4 rounds (rare achievement)
            int winnings = currentBet * 20; // 20x multiplier for completing all rounds
            GamblingTycoon.updateMoney(winnings);
            roundLabel.setText("🎉 CHAMPION! You completed all 4 rounds! 🎉");
        } else if (!won) {
            roundLabel.setText("Game Over - Better luck next time!");
        } else {
            // This should not happen anymore since cashOut uses its own method
            roundLabel.setText("Game ended!");
        }
        
        // Reset to initial state
        buttonsPanel.removeAll();
        buttonsPanel.add(startButton);
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
        
        // Game Over check
        if (GamblingTycoon.getMoney() <= 0) {
            GamblingTycoon.showGameOverScreen();
        }
    }
    
    /**
     * Refreshes the visual card display - convenience method for updateCardDisplay()
     */
    private static void updateDisplay() {
        updateCardDisplay();
    }
    
    /**
     * Updates the visual representation of all drawn cards on screen
     * Shows drawn cards in sequence with proper spacing, and displays a card back
     * placeholder for the next card if the game is still active
     */
    private static void updateCardDisplay() {
        cardsPanel.removeAll();
        
        // Display all drawn cards with proper spacing
        for (int i = 0; i < drawnCards.size(); i++) {
            Card card = drawnCards.get(i);
            JLabel cardLabel = createCardLabel(card);
            cardsPanel.add(cardLabel);
            
            // Add spacing between cards (but not after the last card)
            if (i < drawnCards.size() - 1) {
                cardsPanel.add(Box.createHorizontalStrut(10));
            }
        }
        
        // Show placeholder for next card if game is still active and more cards can be drawn
        if (gameActive && drawnCards.size() < 4) {
            cardsPanel.add(Box.createHorizontalStrut(10));
            JLabel nextCardLabel = createCardBackLabel();
            cardsPanel.add(nextCardLabel);
        }
        
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
    
    /**
     * Creates a styled label to display a revealed playing card
     * Shows the card's display name with appropriate color formatting
     * 
     * @param card The card to create a visual representation for
     * @return JLabel formatted to display the card attractively
     */
    private static JLabel createCardLabel(Card card) {
        JLabel label = new JLabel(card.getDisplayName(), JLabel.CENTER);
        label.setPreferredSize(new Dimension(120, 180));
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        label.setForeground(card.getColor()); // Red for hearts/diamonds, black for clubs/spades
        label.setFont(new Font("Arial", Font.BOLD, 28));
        return label;
    }
    
    /**
     * Creates a card back placeholder to represent unknown/upcoming cards
     * Uses a card back symbol with dark blue background to maintain card layout
     * 
     * @return JLabel formatted as a face-down playing card
     */
    private static JLabel createCardBackLabel() {
        JLabel label = new JLabel("🂠", JLabel.CENTER);
        label.setPreferredSize(new Dimension(120, 180));
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        label.setBackground(new Color(0, 50, 150)); // Dark blue card back
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 36));
        return label;
    }
}
