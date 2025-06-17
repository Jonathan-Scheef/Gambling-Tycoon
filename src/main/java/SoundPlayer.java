import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

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
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            
            // Play the sound
            clip.start();
            
            // Clean up resources when sound finishes
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        try {
                            audioInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
            System.err.println("Error playing sound: " + soundFile);
            e.printStackTrace();
        }
    }    public static void playLeverPull() {
        playSound("assets/LeverPull.wav");
    }
    
    public static void playWin() {
        playSound("assets/Win.wav");
    }
    
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }
    
    public static boolean isSoundEnabled() {
        return soundEnabled;
    }
}
