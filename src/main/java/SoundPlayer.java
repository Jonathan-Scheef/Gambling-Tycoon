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

public class SoundPlayer {
    private static boolean soundEnabled = true;
    private static boolean mp3SupportWarningShown = false;
      public static void playSound(String soundFile) {
        if (!soundEnabled) return;
        
        try {
            File audioFile = new File(soundFile);
            if (!audioFile.exists()) {
                System.err.println("Sound file not found: " + soundFile);
                return;
            }
            
            // Versuche WAV-Version zuerst
            String wavFile = soundFile.replace(".mp3", ".wav");
            File wavAudioFile = new File(wavFile);
            
            File fileToPlay = wavAudioFile.exists() ? wavAudioFile : audioFile;
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileToPlay);
            
            // Check if the audio format is supported
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Audio format not supported for: " + soundFile);
                System.err.println("Format: " + format);
                audioInputStream.close();
                
                // Try fallback sound for card deals
                if (soundFile.contains("single card deal var.wav")) {
                    playSound("assets/single card deal.wav");
                    return;
                }
                return;
            }
            
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            
            // Play the sound
            clip.start();
            
            // Clean up resources when sound finishes
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
            if (!mp3SupportWarningShown && soundFile.endsWith(".mp3")) {
                System.out.println("MP3 files are not supported by Java's default audio system.");
                System.out.println("Please convert " + soundFile + " to WAV format for sound support.");
                mp3SupportWarningShown = true;
            }
        } catch (IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + soundFile + " - " + e.getMessage());
        }
    }    public static void playLeverPull() {
        playSound("assets/LeverPull.wav");
    }
    
    public static void playWin() {
        playSound("assets/Win.wav");
    }
    
    public static void playButtonClick() {
        playSound("assets/Button Click.wav");
    }
    
    public static void playCardDeal() {
        playSound("assets/single card deal.wav");
    }
      public static void playCardDealVariant() {
        // Fallback to standard card deal sound since var.wav has format issues
        playSound("assets/single card deal.wav");
    }
      public static void playFiveCardDeal() {
        playSound("assets/five card deal.mp3"); // Will try WAV version first if available
    }
    
    public static void playError() {
        playSound("assets/error.aiff");
    }
    
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }
    
    public static boolean isSoundEnabled() {
        return soundEnabled;
    }
}
