import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * SoundPlayer - Centralized audio system for the Gambling Tycoon casino games
 * 
 * This utility class manages all sound effects throughout the casino application.
 * It provides a unified interface for playing various game sounds including:
 * - Button clicks and UI interactions
 * - Card dealing and game actions
 * - Win/loss notifications
 * - Error alerts and warnings
 * 
 * Key features:
 * - Global sound enable/disable toggle
 * - Automatic audio format detection (WAV preferred over MP3)
 * - Resource management with automatic cleanup
 * - Fallback mechanisms for unsupported formats
 * - Convenience methods for common casino sound effects
 * 
 * The class handles Java's audio system limitations gracefully and provides
 * consistent audio playback across all casino games.
 */
public class SoundPlayer {
    // Global sound system control variables
    private static boolean soundEnabled = true;           // Master volume control for all game sounds
    private static boolean mp3SupportWarningShown = false; // Prevents spam of MP3 format warnings
    
    /**
     * Core sound playback method with comprehensive format support and error handling
     * 
     * This method orchestrates the entire sound playback process:
     * - Validates sound system is enabled
     * - Checks file existence and accessibility
     * - Prefers WAV format over MP3 for better Java compatibility
     * - Handles audio stream creation and resource management
     * - Provides detailed error reporting for debugging
     * - Automatically cleans up audio resources after playback
     * 
     * @param soundFile Absolute or relative path to the audio file to play
     */
    public static void playSound(String soundFile) {
        if (!soundEnabled) return;
        
        try {
            File audioFile = new File(soundFile);
            if (!audioFile.exists()) {
                System.err.println("Sound file not found: " + soundFile);
                return;
            }
            
            // Smart file selection: prefer WAV over MP3 for better Java audio support
            String wavFile = soundFile.replace(".mp3", ".wav");
            File wavAudioFile = new File(wavFile);
            
            File fileToPlay = wavAudioFile.exists() ? wavAudioFile : audioFile;
            
            // Create audio input stream from the selected file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileToPlay);
            
            // Validate that the system can handle this audio format
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Audio format not supported for: " + soundFile);
                System.err.println("Format: " + format);
                audioInputStream.close();
                
                // Fallback mechanism for known problematic files
                if (soundFile.contains("single card deal var.wav")) {
                    playSound("assets/single card deal.wav");
                    return;
                }
                return;
            }
            
            // Create and configure the audio clip
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            
            // Begin sound playback
            clip.start();
            
            // Set up automatic resource cleanup when playback completes
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    try {
                        audioInputStream.close();
                    } catch (IOException e) {
                        System.err.println("Error closing audio stream: " + e.getMessage());
                    }
                }
            });
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file format: " + soundFile);
            // Show MP3 compatibility warning only once to avoid console spam
            if (!mp3SupportWarningShown && soundFile.endsWith(".mp3")) {
                System.out.println("MP3 files are not supported by Java's default audio system.");
                System.out.println("Please convert " + soundFile + " to WAV format for sound support.");
                mp3SupportWarningShown = true;
            }
        } catch (IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + soundFile + " - " + e.getMessage());
        }
    }
    
    /**
     * Plays the slot machine lever pull sound effect
     * Triggered when the player activates the slot machine spin mechanism
     */
    public static void playLeverPull() {
        playSound("assets/LeverPull.wav");
    }
    
    /**
     * Plays the victory sound effect for winning outcomes
     * Used across all games when the player wins money or completes a successful round
     */
    public static void playWin() {
        playSound("assets/Win.wav");
    }
    
    /**
     * Plays the standard button click sound for UI interactions
     * Provides audio feedback for button presses and menu selections
     */
    public static void playButtonClick() {
        playSound("assets/Button Click.wav");
    }
    
    /**
     * Plays the single card dealing sound effect
     * Used when dealing individual cards in Blackjack and Ride the Bus
     */
    public static void playCardDeal() {
        playSound("assets/single card deal.wav");
    }
    
    /**
     * Plays the variant card dealing sound effect
     * Falls back to standard card deal sound due to format compatibility issues
     */
    public static void playCardDealVariant() {
        // Use standard card deal sound as fallback since var.wav has format issues
        playSound("assets/single card deal.wav");
    }
    
    /**
     * Plays the five-card dealing sound effect
     * Used for dealing multiple cards at once (will try WAV version first)
     */
    public static void playFiveCardDeal() {
        playSound("assets/five card deal.mp3"); // Auto-converts to WAV if available
    }
    
    /**
     * Plays the error sound effect
     * Used when player makes invalid inputs or actions
     */
    public static void playError() {
        playSound("assets/error.aiff");
    }
    
    /**
     * Enables or disables sound globally across the application
     * 
     * @param enabled true to enable sounds, false to disable
     */
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }
    
    /**
     * Returns the current sound enabled state
     * 
     * @return true if sounds are enabled, false if disabled
     */
    public static boolean isSoundEnabled() {
        return soundEnabled;
    }
}
